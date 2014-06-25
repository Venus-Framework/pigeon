/**
 * 
 */
package com.dianping.pigeon.console.servlet.json;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.pigeon.console.domain.Statistics;
import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.remoting.invoker.route.statistics.CapacityBucket;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;

public class StatisticsJsonServlet extends ServiceServlet {

	private static final long serialVersionUID = -3000545547453006628L;

	public StatisticsJsonServlet(ServerConfig serverConfig, int port) {
		super(serverConfig, port);
	}

	@Override
	public String getView() {
		return "StatisticsJson.ftl";
	}

	public String getContentType() {
		return "application/json; charset=UTF-8";
	}

	protected void initServicePage(HttpServletRequest request) {
		Statistics stat = new Statistics();
		Map<String, CapacityBucket> buckets = ServiceStatisticsHolder.getCapacityBuckets();
		for (String addr : buckets.keySet()) {
			int requests = buckets.get(addr).getLastSecondRequest();
			stat.getRequestsInLastSecond().put(addr, requests);
		}
		Map<String, Server> servers = ProviderBootStrap.getServersMap();
		Map<String, String> serverProcessorStatistics = stat.getServerProcessorStatistics();
		for (Server server : servers.values()) {
			RequestProcessor processor = server.getRequestProcessor();
			if (processor != null) {
				serverProcessorStatistics.put(server.getProtocol() + "-" + server.getPort(),
						processor.getProcessorStatistics());
			}
		}
		this.model = stat;
	}
}
