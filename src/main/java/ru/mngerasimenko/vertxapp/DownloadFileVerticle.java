package ru.mngerasimenko.vertxapp;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;

import java.net.URL;

public class DownloadFileVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(DownloadFileVerticle.class);

	@Override
	public void start() throws Exception {
		EventBus eventBus = vertx.eventBus();

		eventBus.consumer("download.file", message -> {
			URL url = (URL) message.body();
			logger.info("starting download file " + url);
			downloadFileFromUrl(url);
		});
	}

	private void downloadFileFromUrl(URL url) {
		WebClient client = WebClient.create(vertx);
		client
				.get(80, url.getHost(), url.getPath())
				.send()
				.onSuccess(response -> {
					logger.info("received response with status code " + response.statusCode());
					if (response.statusCode() == HttpResponseStatus.OK.code()
							&& response.body() != null) {
						DeliveryOptions options = new DeliveryOptions();
						options.addHeader("file.name", Utils.getFileNameFromUrl(url));

						vertx.eventBus().send("save.file", response.bodyAsBuffer(), options);

						logger.info("file was downloaded " + Utils.getFileNameFromUrl(url));
					} else {
						logger.warn("something went wrong");
					}
				}).onFailure(err -> logger.warn("something went wrong " + err.getMessage()));
	}
}
