package com.dianping.pigeon.governor.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.util.Utils;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service
public class RegistrationInfoServiceDefaultImpl implements RegistrationInfoService {

	private static Registry registry = RegistryManager.getInstance().getRegistry();

	@Override
	public String getAppOfService(String url, String group) throws RegistryException {
		String serviceAddress = registry.getServiceAddress(url, group);
		List<String> addressList = Utils.getAddressList(url, serviceAddress);
		Map<String, Integer> appCount = new HashMap<String, Integer>();
		for (String addr : addressList) {
			String addrApp = registry.getServerApp(addr);
			if (StringUtils.isNotBlank(addrApp)) {
				Integer count = appCount.get(addrApp);
				if (count == null) {
					appCount.put(addrApp, 1);
				} else {
					return addrApp;
				}
			}
		}
		if (!appCount.isEmpty()) {
			return appCount.keySet().iterator().next();
		}
		return null;
	}

	public String getAppOfService(String url) throws RegistryException {
		return getAppOfService(url, null);
	}

	@Override
	public String getWeightOfAddress(String address) throws RegistryException {
		CuratorClient client = ((CuratorRegistry) registry).getCuratorClient();
		try {
			return client.get("/DP/WEIGHT/" + address);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	public String getValueOfPath(String path) throws RegistryException {
		CuratorClient client = ((CuratorRegistry) registry).getCuratorClient();
		try {
			return client.get(path);
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	@Override
	public String getAppOfAddress(String address) throws RegistryException {
		return registry.getServerApp(address);
	}

	@Override
	public List<String> getAddressListOfService(String url, String group) throws RegistryException {
		String serviceAddress = registry.getServiceAddress(url, group);
		List<String> addressList = Utils.getAddressList(url, serviceAddress);
		return addressList;
	}

	@Override
	public List<String> getAddressListOfService(String url) throws RegistryException {
		return getAddressListOfService(url, null);
	}
}
