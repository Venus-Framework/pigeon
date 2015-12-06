/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.util;

import java.io.File;
import java.net.URL;
import java.util.Properties;

public class AppUtils {

	private static String appName = null;

	public static String getAppName() {
		if (appName == null) {
			try {
				URL appProperties = AppUtils.class.getResource("/META-INF/app.properties");
				if (appProperties == null) {
					appProperties = new URL("file:" + AppUtils.class.getResource("/").getPath()
							+ "/META-INF/app.properties");
					if (!new File(appProperties.getFile()).exists()) {
						appProperties = new URL("file:/data/webapps/config/app.properties");
					}
				}
				Properties properties = null;
				if (appProperties != null) {
					properties = FileUtils.readFile(appProperties.openStream());
					appName = properties.getProperty("app.name");
				}
			} catch (Throwable e) {
			}
			if (appName == null) {
				return "NULL";
			}
		}
		return appName;
	}
}
