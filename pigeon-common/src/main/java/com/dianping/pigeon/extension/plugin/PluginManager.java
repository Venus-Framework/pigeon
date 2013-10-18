/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginManager {

	public enum Phase {
		PHASE_UNDEFINED, PHASE_ONE, PHASE_TWO
	};

	private static Map<Phase, ConcurrentHashMap<String, Plugin>> plugins = new ConcurrentHashMap<PluginManager.Phase, ConcurrentHashMap<String, Plugin>>();

	public static void registerPlugin(Phase phase, Plugin plugin) {
		if (plugins.get(phase) == null) {
			ConcurrentHashMap<String, Plugin> subplugins = new ConcurrentHashMap<String, Plugin>();
			subplugins.put(plugin.getComponent(), plugin);
			plugins.put(phase, subplugins);
		} else {

			plugins.get(phase).putIfAbsent(plugin.getComponent(), plugin);
		}
	}

	public static ConcurrentHashMap<String, Plugin> loadPlugins(Phase phase) {

		ConcurrentHashMap<String, Plugin> phasePlugins = plugins.get(phase);
		if (phasePlugins == null) {
			return new ConcurrentHashMap<String, Plugin>();
		}
		return phasePlugins;

	}
}
