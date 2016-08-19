package com.dianping.pigeon.config.file;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.config.AbstractConfigManager;
import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.LocalConfigLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.util.FileUtils;

/**
 * @author xiangwu
 * @Sep 22, 2013
 * 
 */
public class PropertiesFileConfigManager extends AbstractConfigManager {

	private static Logger logger = LoggerLoader.getLogger(PropertiesFileConfigManager.class);

	private static final String ENV_FILE = "/data/webapps/appenv";

	private String env = null;
	private String group = null;
	private String ip = null;

	public void init() {
		Map<String, Object> config = new HashMap<String, Object>();
		try {
			LocalConfigLoader.loadProperties(config, FileUtils.readFile(new FileInputStream(ENV_FILE)));
		} catch (Exception e) {
		}
		env = (String) config.get("environment");
		if (StringUtils.isBlank(env)) {
			logger.warn("the config 'environment' is undefined in " + ENV_FILE);
		}
		group = (String) config.get("group");
		ip = (String) config.get("ip");
	}

	@Override
	public String getConfigServerAddress() {
		return "";
	}

	@Override
	public String doGetProperty(String key) throws Exception {
		return null;
	}

	@Override
	public String doGetLocalProperty(String key) throws Exception {
		return null;
	}

	@Override
	public String doGetEnv() throws Exception {
		return env;
	}

	@Override
	public String doGetLocalIp() throws Exception {
		return ip;
	}

	@Override
	public String doGetGroup() throws Exception {
		return group;
	}

	@Override
	public void doSetStringValue(String key, String value) throws Exception {

	}

	@Override
	public void doDeleteKey(String key) throws Exception {

	}

	@Override
	public void doRegisterConfigChangeListener(ConfigChangeListener configChangeListener) throws Exception {

	}

	public String toString() {
		return "PropertiesFileConfigManager";
	}
}
