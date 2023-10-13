package ru.mngerasimenko.vertxapp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;


public class SaveFileVerticle extends AbstractVerticle {
	private static final Logger logger = LoggerFactory.getLogger(SaveFileVerticle.class);

	@Override
	public void start() throws Exception {
		EventBus eventBus = vertx.eventBus();
		eventBus.consumer("save.file", message -> {
			Buffer buffer = (Buffer) message.body();
			String fileName = message.headers().get("file.name");
			logger.info("LOG: saving file " + fileName);

			saveFile(buffer, fileName);
		});
	}

	private void saveFile(Buffer buffer, String fileName) {
		FileSystem fileSystem = vertx.fileSystem();
		String downloadDir = config().getString("download.path", "downloadedFiles\\");
		fileSystem.mkdir(downloadDir);
		String filePath = downloadDir + fileName;
		fileSystem.writeFile(filePath, buffer, result -> {
			if (result.succeeded()) {
				logger.info("LOG: file saved successfully " + filePath);
			} else {
				logger.warn("WARN: failed to save file " + filePath);
			}
		});
	}
}
