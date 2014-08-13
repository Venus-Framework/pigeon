package com.dianping.pigeon.config.lion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentConfigLoader {
	private static Logger logger = LoggerFactory.getLogger(EnvironmentConfigLoader.class);

	private static String FILENAME = "appenv";
	private static String DEFAULT_LOCATION = "/data/webapps/appenv";

	private static String KEY_ENV = "deployenv";
	private static String KEY_CONFIGSERVER = "zkserver";
	private static String KEY_SWIMLANE = "swimlane";
	private static String DEFAULT_ENV = "dev";
	private static String DEFAULT_CONFIGSERVERSERVER = "dev.lion.dp:2181";
	private static String DEFAULT_SWIMLANE = "";

	private static String PATH_JAR = "jar:file:";
	private static String PATH_WAR = "WEB-INF/lib";
	private static String JARPATHSUF = "/com/dianping/pigeon/config/lion/EnvironmentConfigLoader.class";

	private static String env;
	private static String configServerAddress;
	private static String swimlane = DEFAULT_SWIMLANE;

	static {
		File envFile = null;
		FileReader fileReader = null;
		Properties props = null;
		try {
			envFile = getEnvFile();
			if (envFile == null) {
				logger.warn("failed to find appenv file at " + DEFAULT_LOCATION + ", use default settings");
				env = DEFAULT_ENV;
				configServerAddress = DEFAULT_CONFIGSERVERSERVER;
				swimlane = DEFAULT_SWIMLANE;
			} else {
				fileReader = new FileReader(envFile);
				props = new Properties();
				props.load(fileReader);

				env = props.getProperty(KEY_ENV);
				configServerAddress = props.getProperty(KEY_CONFIGSERVER);

				if (env != null) {
					env = env.trim();
				} else {
					env = DEFAULT_ENV;
				}
				logger.info("env:" + env);
				if (configServerAddress != null) {
					configServerAddress = configServerAddress.trim();
				} else {
					configServerAddress = DEFAULT_CONFIGSERVERSERVER;
				}
				logger.info("server address:" + configServerAddress);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
				}
			}
		}
		readSwimlane();
	}

	private static void readSwimlane() {
		InputStream in = null;
		try {
			Properties props = new Properties();
			in = new FileInputStream("/data/webapps/appenv");
			props.load(in);
			swimlane = props.getProperty(KEY_SWIMLANE);
			if (StringUtils.isBlank(swimlane)) {
				swimlane = DEFAULT_SWIMLANE;
			}
			logger.info("swimlane:" + swimlane);
		} catch (IOException e) {
			logger.info("No appenv file /data/webapps/appenv");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	private static File getEnvFile() {
		String path = EnvironmentConfigLoader.class.getClassLoader()
				.getResource("com/dianping/pigeon/config/lion/EnvironmentConfigLoader.class").toString();

		int indexOfJar = path.indexOf(PATH_JAR);
		int indexOfWar = path.indexOf(PATH_WAR);

		if (indexOfWar == -1) {
			indexOfWar = path.indexOf(JARPATHSUF);
			path = path.substring(PATH_JAR.length(), indexOfWar);
			int indexOfLast = path.lastIndexOf('/');
			path = path.substring(0, indexOfLast + 1);
		} else if (indexOfJar != -1) {
			path = path.substring(PATH_JAR.length(), indexOfWar);
		}

		File file = new File(path + FILENAME);
		if (!file.exists())
			file = new File(DEFAULT_LOCATION);

		if (!file.exists())
			return null;

		return file;
	}

	public static String getEnv() {
		return env;
	}

	public static String getConfigServerAddress() {
		return configServerAddress;
	}

	public static String getSwimlane() {
		return swimlane;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(KEY_ENV + "\t: " + EnvironmentConfigLoader.getEnv());
		System.out.println(KEY_CONFIGSERVER + "\t: " + EnvironmentConfigLoader.getConfigServerAddress());
		System.out.println(KEY_SWIMLANE + "\t: " + EnvironmentConfigLoader.getSwimlane());
	}

}
