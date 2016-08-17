package com.dianping.pigeon.console.domain;

import java.util.HashMap;
import java.util.Map;

import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager.WeightFactor;

public class Statistics {

	public Map<String, Integer> requestsInLastSecondOfInvoker = new HashMap<String, Integer>();

	public Map<String, String> appRequestsOfProvider = new HashMap<String, String>();

	public Map<String, String> appRequestsOfInvoker = new HashMap<String, String>();

	private Map<String, String> serverProcessorStatistics = new HashMap<String, String>();

	private Map<String, WeightFactor> weightFactors = new HashMap<String, WeightFactor>();

	private Map<String, InvokerConfig> invokerConfigs = new HashMap<String, InvokerConfig>();

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

	public Map<String, String> getOthers() {
		return others;
	}

	public void setOthers(Map<String, String> others) {
		this.others = others;
	}

	public Map<String, Integer> getRequestsInLastSecondOfInvoker() {
		return requestsInLastSecondOfInvoker;
	}

	public void setRequestsInLastSecondOfInvoker(Map<String, Integer> requestsInLastSecondOfInvoker) {
		this.requestsInLastSecondOfInvoker = requestsInLastSecondOfInvoker;
	}

	public Map<String, String> getAppRequestsOfProvider() {
		return appRequestsOfProvider;
	}

	public void setAppRequestsOfProvider(Map<String, String> appRequestsOfProvider) {
		this.appRequestsOfProvider = appRequestsOfProvider;
	}

	public Map<String, String> getAppRequestsOfInvoker() {
		return appRequestsOfInvoker;
	}

	public void setAppRequestsOfInvoker(Map<String, String> appRequestsOfInvoker) {
		this.appRequestsOfInvoker = appRequestsOfInvoker;
	}

	public Map<String, InvokerConfig> getInvokerConfigs() {
		return invokerConfigs;
	}

	public void setInvokerConfigs(Map<String, InvokerConfig> invokerConfigs) {
		this.invokerConfigs = invokerConfigs;
	}

}
