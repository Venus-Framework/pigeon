package com.dianping.pigeon.console.domain;

import java.util.HashMap;
import java.util.Map;

import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager.WeightFactor;

public class Statistics {

	public Map<String, Integer> requestsInLastSecond = new HashMap<String, Integer>();

	public Map<String, String> appRequests = new HashMap<String, String>();

	Map<String, String> serverProcessorStatistics = new HashMap<String, String>();

	Map<String, WeightFactor> weightFactors = new HashMap<String, WeightFactor>();

	public Map<String, String> others = new HashMap<String, String>();

	public Map<String, WeightFactor> getWeightFactors() {
		return weightFactors;
	}

	public void setWeightFactors(Map<String, WeightFactor> weightFactors) {
		this.weightFactors = weightFactors;
	}

	public Map<String, String> getServerProcessorStatistics() {
		return serverProcessorStatistics;
	}

	public void setServerProcessorStatistics(Map<String, String> serverProcessorStatistics) {
		this.serverProcessorStatistics = serverProcessorStatistics;
	}

	public Map<String, Integer> getRequestsInLastSecond() {
		return requestsInLastSecond;
	}

	public void setRequestsInLastSecond(Map<String, Integer> requestsInLastSecond) {
		this.requestsInLastSecond = requestsInLastSecond;
	}

	public Map<String, String> getAppRequests() {
		return appRequests;
	}

	public void setAppRequests(Map<String, String> appRequests) {
		this.appRequests = appRequests;
	}

	public Map<String, String> getOthers() {
		return others;
	}

	public void setOthers(Map<String, String> others) {
		this.others = others;
	}

}
