package ru.mngerasimenko.vertxapp;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.*;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.net.MalformedURLException;
import java.net.URL;

public class HttpServerVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);
		router.get("/status").handler(this::getStatus);
		router.post("/download").handler(BodyHandler.create());
		router.post("/download").handler(this::downloadFile);

		vertx.createHttpServer()
				.requestHandler(router)
				.listen(config().getInteger("port", 8080))
				.onSuccess(server -> logger.info("HTTP server started on port " + server.actualPort()));
	}

	private void getStatus(RoutingContext routingContext) {
		JsonObject status = new JsonObject().put("status", "running");
		routingContext.response()
				.putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
				.end(status.encodePrettily());
		logger.info("status was sending");
	}

	private void downloadFile(RoutingContext routingContext) {
		JsonObject bodyJson = routingContext.body().asJsonObject();
		System.out.println(bodyJson);
		if (bodyJson != null) {
			String stringUrl = bodyJson.getString("url");
			if (stringUrl == null) {
				routingContext.response()
						.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
						.end("Unable to find URL in JSON");
			} else {
				try {
					URL url = new URL(stringUrl);

					vertx.eventBus().send("download.file", url);

					routingContext.response()
							.setStatusCode(HttpResponseStatus.OK.code())
							.end("Downloading file was started");
				} catch (MalformedURLException e) {
					routingContext.response()
							.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
							.end("Invalid URL in JSON");
				}
			}
		} else {
			routingContext.response()
					.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
					.end("Unable to find JSON in body");
		}
	}
}
