package com.dianping.pigeon.registry.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.component.HostInfo;

public final class ServiceCache {

	public static Map<String, String> serviceNameToGroup = new HashMap<String, String>();

	public static Map<String, Integer> serviceNameAndWeights = new HashMap<String, Integer>();

	public static Map<String, Set<HostInfo>> serviceNameToHostInfos = new ConcurrentHashMap<String, Set<HostInfo>>();

}
