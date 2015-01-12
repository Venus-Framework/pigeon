package com.dianping.pigeon.remoting.invoker.cluster;

import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.remoting.common.util.Constants;

public class ClusterFactory {

	private final static ConcurrentHashMap<String, Cluster> clusters = new ConcurrentHashMap<String, Cluster>();

	static {
		init();
	}

	public static void init() {
		clusters.put(Constants.CLUSTER_FAILFAST, new FailfastCluster());
		clusters.put(Constants.CLUSTER_FAILOVER, new FailoverCluster());
		clusters.put(Constants.CLUSTER_FAILSAFE, new FailsafeCluster());
		clusters.put(Constants.CLUSTER_FORKING, new ForkingCluster());
	}

	public static void registerCluster(String clusterType, Cluster cluster) {
		clusters.put(clusterType, cluster);
	}

	public static Cluster selectCluster(String clusterType) {
		Cluster cluster = clusters.get(clusterType);
		if (cluster == null) {
			return clusters.get(Constants.CLUSTER_FAILFAST);
		}
		return cluster;
	}
}
