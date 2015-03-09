/**
 * 
 */
package com.dianping.pigeon.console.servlet.json;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.pigeon.console.domain.Statistics;
import com.dianping.pigeon.console.listener.StatusListener;
import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.console.status.StatusInfo;
import com.dianping.pigeon.remoting.invoker.process.statistics.InvokerCapacityBucket;
import com.dianping.pigeon.remoting.invoker.process.statistics.InvokerStatisticsHolder;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.remoting.invoker.route.statistics.CapacityBucket;
import com.dianping.pigeon.remoting.invoker.route.statistics.ServiceStatisticsHolder;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;
import com.dianping.pigeon.remoting.provider.process.RequestProcessor;
import com.dianping.pigeon.remoting.provider.process.statistics.ProviderCapacityBucket;
import com.dianping.pigeon.remoting.provider.process.statistics.ProviderStatisticsHolder;

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
			stat.getRequestsInLastSecondOfInvoker().put(addr, requests);
		}
		Map<String, InvokerCapacityBucket> invokerCapacityMap = InvokerStatisticsHolder.getCapacityBuckets();
		for (String app : invokerCapacityMap.keySet()) {
			InvokerCapacityBucket appCapacity = invokerCapacityMap.get(app);
			if (appCapacity != null) {
				stat.getAppRequestsOfInvoker().put(app, appCapacity.toString());
			}
		}
		Map<String, ProviderCapacityBucket> providerCapacityMap = ProviderStatisticsHolder.getCapacityBuckets();
		for (String app : providerCapacityMap.keySet()) {
			ProviderCapacityBucket appCapacity = providerCapacityMap.get(app);
			if (appCapacity != null) {
				stat.getAppRequestsOfProvider().put(app, appCapacity.toString());
			}
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
		stat.setWeightFactors(LoadBalanceManager.getWeightFactors());

		List<StatusInfo> infoList = StatusListener.getStatusInfoList();
		for (StatusInfo info : infoList) {
			stat.getOthers().put(info.getSource(), "" + info.getStatusInfo());
		}
		this.model = stat;
	}
}
