/**
 * 
 */
package com.dianping.pigeon.console.servlet.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dianping.pigeon.console.servlet.ServicePage;
import com.dianping.pigeon.console.servlet.ServiceServlet;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.provider.ProviderBootStrap;
import com.dianping.pigeon.remoting.provider.Server;
import com.dianping.pigeon.remoting.provider.config.ServerConfig;

public class DependencyJsonServlet extends ServiceServlet {

	private static final long serialVersionUID = -3000545547453006628L;

	private static ClientManager clientManager = ClientManager.getInstance();

	public DependencyJsonServlet(ServerConfig serverConfig, int port) {
		super(serverConfig, port);
	}

	@Override
	public String getView() {
		return "DependencyJson.ftl";
	}

	public String getContentType() {
		return "application/json; charset=UTF-8";
	}

	protected void initServicePage(HttpServletRequest request) {
		List<String> invokers = new ArrayList<String>();
		Collection<Server> servers = ProviderBootStrap.getServersMap().values();
		for (Server server : servers) {
			List<String> serverInvokers = server.getInvokerMetaInfo();
			if (serverInvokers != null) {
				invokers.addAll(serverInvokers);
			}
		}
		ServicePage page = new ServicePage();
		page.setInvokers(invokers);
		page.setEnvironment(configManager.getEnv());
		Map<String, String> heartbeatsResults = new HashMap<String, String>();
		Map<String, List<Client>> heartbeats = clientManager.getHeartTask().getWorkingClients();
		if (heartbeats != null) {
			for (String key : heartbeats.keySet()) {
				List<Client> clients = heartbeats.get(key);
				StringBuilder sb = new StringBuilder();
				for (Client client : clients) {
					sb.append("[").append(client.toString()).append("-weight:")
							.append(RegistryManager.getInstance().getServiceWeight(client.getAddress())).append("]");
				}
				heartbeatsResults.put(key, sb.toString());
			}
		}
		page.setHeartbeats(heartbeatsResults);

		Map<String, String> reconnectsResults = new HashMap<String, String>();
		Map<String, Client> reconnects = clientManager.getReconnectTask().getClosedClients();
		if (reconnects != null) {
			for (String key : reconnects.keySet()) {
				Client client = reconnects.get(key);
				StringBuilder sb = new StringBuilder();
				sb.append(client.toString()).append("-weight:")
						.append(RegistryManager.getInstance().getServiceWeight(client.getAddress()));
				reconnectsResults.put(key, sb.toString());
			}
		}
		page.setReconnects(reconnectsResults);

		this.model = page;
	}
}
