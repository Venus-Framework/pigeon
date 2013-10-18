/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension.plugin;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.dianping.pigeon.component.xmap.XMap;
import com.dianping.pigeon.extension.plugin.PluginManager.Phase;

public final class PluginContainer {

	private static final Logger logger = Logger.getLogger(PluginContainer.class);
	public static final String PLUGIN_PATH = "META-INF/pigeon/plugin.xml";
	private static XMap xmap = new XMap();

	public static void start() {
		xmap.register(Plugin.class);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Enumeration<URL> urls = loader.getResources(PLUGIN_PATH);

			while (urls.hasMoreElements()) {
				loadPluginFromStream(urls.nextElement());
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private static void loadPluginFromStream(URL url) throws Exception {

		if (logger.isInfoEnabled()) {
			logger.info(".....plugin xml url path:" + url.getPath());
		}

		InputStream stream = url.openStream();
		Object[] plugins = (Object[]) xmap.loadAll(stream);
		for (Object p : plugins) {
			Plugin plugin = (Plugin) p;
			/**
			 * if (plugin.getPoint() == null || plugin.getDescriptor() == null
			 * || plugin.getContent() == null) { throw new
			 * IllegalStateException(
			 * "missing phase/descriptor/content in plugin declaration"); }
			 **/
			if (logger.isInfoEnabled()) {
				logger.info("PLUGIN " + plugin.getComponent());
			}

			if (plugin.getDescriptor() != null) {
				Class<?> descriptorClass = Class.forName(plugin.getDescriptor());
				xmap.register(descriptorClass);

				InputStream content = new ByteArrayInputStream(plugin.getContent().getBytes());
				Object descriptorObject = xmap.load(content);

				plugin.setDescriptorObject(descriptorObject);
			}

			Phase phase = Phase.PHASE_UNDEFINED;
			if ("one".equalsIgnoreCase(plugin.getPhase())) {
				phase = Phase.PHASE_ONE;
			} else if ("two".equalsIgnoreCase(plugin.getPhase())) {
				phase = Phase.PHASE_TWO;
			}
			PluginManager.registerPlugin(phase, plugin);
		}
	}

	public static void initPlugins() {

	}
}
