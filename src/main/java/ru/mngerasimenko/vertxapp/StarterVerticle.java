package ru.mngerasimenko.vertxapp;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

public class StarterVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(StarterVerticle.class);

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		ConfigStoreOptions fileStore = new ConfigStoreOptions()
				.setType("file")
				.setOptional(true)
				.setConfig(new JsonObject().put("path", "config.json"));
		ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");
		ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore).addStore(sysPropsStore);
		ConfigRetriever configRetriever = ConfigRetriever.create(vertx, options);
		configRetriever.getConfig(ar -> {
			if (ar.succeeded()) {
				JsonObject config = ar.result();

				deploy(HttpServerVerticle.class, new DeploymentOptions().setConfig(config.getJsonObject("http")));
				deploy(DownloadFileVerticle.class, new DeploymentOptions());
				deploy(SaveFileVerticle.class, new DeploymentOptions().setConfig(config.getJsonObject("file")));
			} else {
				deploy(HttpServerVerticle.class, new DeploymentOptions());
				deploy(DownloadFileVerticle.class, new DeploymentOptions());
				deploy(SaveFileVerticle.class, new DeploymentOptions());
			}
			logger.info("Module(s) and/or verticle(s) deployment...DONE");
			startPromise.complete();
		});
	}

	private void deploy(final Class<? extends AbstractVerticle> clazz, final DeploymentOptions options) {
		vertx.deployVerticle(clazz.getName(), options, handler -> {
			if (handler.succeeded()) {
				logger.debug(clazz.getSimpleName() + " started successfully (deployment identifier: )" + handler.result());
			} else {
				logger.error(clazz.getSimpleName() + " deployment failed due to: " + handler.cause());
			}
		});
	}
}
