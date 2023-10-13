package ru.mngerasimenko.vertxapp;

import java.net.URL;

public abstract class Utils {

	public static String getFileNameFromUrl(URL url) {
		String[]  arrPartsUrl = url.getFile().split("/");
		return arrPartsUrl[arrPartsUrl.length - 1];
	}
}
