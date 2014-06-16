package com.dianping.pigeon.console.domain;

import java.util.HashMap;
import java.util.Map;

public class Statistics {

	public Map<String, Integer> requestsInLastSecond = new HashMap<String, Integer>();

	Map<String, String> serverProcessorStatistics = new HashMap<String, String>();

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

}
