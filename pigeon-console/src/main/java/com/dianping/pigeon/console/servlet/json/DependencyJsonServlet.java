/**
 * 
 */
package com.dianping.pigeon.console.servlet.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dianping.pigeon.console.servlet.ServicePage;
import com.dianping.pigeon.console.servlet.ServicePage.ClientInfo;
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

	protected boolean initServicePage(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
		Map<String, List<ClientInfo>> clientResults = new HashMap<String, List<ClientInfo>>();

		Map<String, List<Client>> clientsMap = clientManager.getClusterListener().getServiceClients();
		if (clientsMap != null) {
			for (String key : clientsMap.keySet()) {
				List<Client> clients = clientsMap.get(key);
				List<ClientInfo> clientInfoList = new ArrayList<ClientInfo>();
				for (Client client : clients) {
					clientInfoList.add(new ClientInfo(client, RegistryManager.getInstance().getServiceWeight(
							client.getAddress())));
				}
				clientResults.put(key, clientInfoList);
			}
		}
		page.setClients(clientResults);

		page.setServiceAddresses(RegistryManager.getInstance().getAllReferencedServiceAddresses());

		this.model = page;
		
		return true;
	}
}
