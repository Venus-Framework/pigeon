package com.dianping.pigeon.registry.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.pigeon.exception.PigeonRuntimeException;

public class WeightCache {

	private Map<String, List<Integer>> weightCache = new ConcurrentHashMap<String, List<Integer>>();

	private Map<String, Map<String, Integer>> weights = new ConcurrentHashMap<String, Map<String, Integer>>();

	private Map<String, Map<String, Integer>> weights_ = new ConcurrentHashMap<String, Map<String, Integer>>();

	private Map<String, Set<String>> groupMap = new ConcurrentHashMap<String, Set<String>>();

	private static WeightCache instance = new WeightCache();

	public static WeightCache getInstance() {
		return instance;
	}

	/**
	 * @return the weights
	 */
	public Map<String, Map<String, Integer>> getWeights() {
		return weights;
	}

	public Set<String> getClientsOfGroup(String group) {
		return groupMap.get(group);
	}

	public Integer getWeight(String serviceName, String address) {
		Map<String, Integer> connectToWeight = weights.get(serviceName);
		Integer weight = null;
		if (connectToWeight != null) {
			weight = connectToWeight.get(address);
		}
		return weight;
	}

	public int getWeightWithDefault(String serviceName, String address) {
		Integer weight = getWeight(serviceName, address);
		return weight != null ? weight : 1;
	}

	public boolean setWeight(String address, int wt) {
		boolean flag = false;
		for (Entry<String, Map<String, Integer>> we : this.weights.entrySet()) {
			if (we.getValue().containsKey(address)) {
				we.getValue().put(address, wt);
				flag = true;
			}
		}
		this.weightCache.clear();
		return flag;
	}

	/**
	 * 调整host对应所有服务的weight
	 * 
	 * @param host
	 * @param wt
	 */
	public void setHostWeight(String host, int wt) {
		for (Entry<String, Map<String, Integer>> we : this.weights.entrySet()) {
			for (Entry<String, Integer> connectWeightEntry : we.getValue().entrySet()) {
				String connect = connectWeightEntry.getKey();
				int colonIdx = connect.indexOf(":");
				String curHost = connect;
				if (colonIdx > 0) {
					curHost = curHost.substring(0, colonIdx);
				}
				if (curHost.equals(host)) {
					we.getValue().put(connect, wt);
				}
			}
		}
		this.weightCache.clear();
	}

	public void registerWeight(String serviceName, String group, String connect, int weight) {
		Set<String> connectSet = this.groupMap.get(group);
		if (connectSet == null) {
			connectSet = new HashSet<String>();
			this.groupMap.put(group, connectSet);
		}
		connectSet.add(connect);

		Map<String, Integer> w = this.weights.get(serviceName);
		if (w == null) {
			w = new ConcurrentHashMap<String, Integer>();
			this.weights.put(serviceName, w);
		}
		w.put(connect, weight);

		Map<String, Integer> w_ = this.weights_.get(serviceName);
		if (w_ == null) {
			w_ = new ConcurrentHashMap<String, Integer>();
			this.weights_.put(serviceName, w_);
		}
		// Integer value = w_.put(connect, weight);
		// if(value != null && value != weight){
		// throw new
		// DPSFRuntimeException("the same service and the same host can not have many weight :"+weight+"-->>"+value);
		// }
		if (weight > 10) {
			throw new PigeonRuntimeException("weight must be not over 10");
		}
	}

	public boolean disableGroupRouteToHost(String group, String connect) {
		if (group == null || connect == null) {
			return false;
		}
		Set<String> connectSet = groupMap.get(group);
		if (connectSet != null) {
			connectSet.remove(connect);
			return true;
		} else {
			return false;
		}
	}

	public void setGroupRoute(String group, Set<String> connectSet) {
		groupMap.put(group, connectSet);
	}

	public boolean enableGroupRouteToHost(String group, String connect) {
		if (group == null || connect == null) {
			return false;
		}
		Set<String> connectSet = groupMap.get(group);
		if (connectSet != null) {
			connectSet.add(connect);
			return true;
		} else {
			return false;
		}
	}

	public void doDefault() {
		Map<String, Map<String, Integer>> _weights = new ConcurrentHashMap<String, Map<String, Integer>>();
		for (Entry<String, Map<String, Integer>> we : this.weights_.entrySet()) {
			Map<String, Integer> w = _weights.get(we.getKey());
			if (w == null) {
				w = new ConcurrentHashMap<String, Integer>();
				_weights.put(we.getKey(), w);
			}
			for (Entry<String, Integer> subwe : we.getValue().entrySet()) {
				w.put(subwe.getKey(), subwe.getValue());
			}
		}
		this.weights = _weights;
		this.weightCache = new ConcurrentHashMap<String, List<Integer>>();
	}

}
