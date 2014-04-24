package com.dianping.pigeon.governor.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.domain.CallbackFuture;
import com.dianping.pigeon.remoting.invoker.domain.ConnectInfo;
import com.dianping.pigeon.remoting.invoker.util.InvokerUtils;
import com.dianping.pigeon.remoting.netty.invoker.NettyClient;

@Component
public class PigeonServiceHealthChecker {

	private static final Logger logger = Logger.getLogger(PigeonServiceHealthChecker.class);

	private static ConfigManager configManger = ExtensionLoader.getExtension(ConfigManager.class);

	public static void main(String[] args) {
		new PigeonServiceHealthChecker().checkRegistry("");
	}

	public void checkRegistry() {
		Map<String, String> registryAddressMap = getRegistryAddressMap();
		configManger.getConfigServerAddress();
	}
	
	private Map<String, String> getRegistryAddressMap() {
		Map<String, String> registryMap = new HashMap<String, String>();
		String registryAddressConfig = configManger.getStringValue("pigeon.healthchecker.registry.address");
		String[] registryAddressArray = registryAddressConfig.split("#");
		for(String registryAddress : registryAddressArray) {
			String[] regAddr = registryAddress.split("=");
			registryMap.put(regAddr[0], regAddr[1]);
		}
		
		return registryMap;
	}

	private void checkRegistry(String registryAddress) {
		Registry registry = RegistryManager.getInstance().getRegistry();
		List<String> children;
		String root = "/DP/SERVER";
		try {
			children = registry.getChildren(root);
			for (String path : children) {
				if (path.startsWith("@HTTP@")) {
					continue;
				}
				try {
					checkPigeonServer(registry.getServiceAddress(path));
				} catch (Exception e) {
					logger.error("", e);
				}

				List<String> groups = registry.getChildren(root + "/" + path);
				if (!CollectionUtils.isEmpty(groups)) {
					for (String group : groups) {
						String groupPath = root + "/" + path + "/" + group;
						try {
							checkPigeonServer(registry.getServiceAddress(groupPath));
						} catch (Exception e) {
							logger.error("", e);
						}
					}
				}
			}
		} catch (RegistryException e1) {
			logger.error("", e1);
		}
	}

	private void checkPigeonServer(String address) {
		if (StringUtils.isBlank(address)) {
			return;
		}
		String[] addressArray = address.split(",");
		for (String addr : addressArray) {
			if (!StringUtils.isBlank(addr)) {
				String[] ipAndPort = addr.split(":");
				String ip = ipAndPort[0];
				int port = Integer.parseInt(ipAndPort[1]);
				ConnectInfo connectInfo = new ConnectInfo("", ip, port, 1);
				boolean status = checkPigeonServer(connectInfo);
				logger.info("checking server:" + connectInfo + ", status:" + status);
			}
		}
	}

	private boolean checkPigeonServer(ConnectInfo connectInfo) {
		InvocationRequest request = new DefaultRequest("", "", null, SerializerFactory.SERIALIZE_HESSIAN,
				Constants.MESSAGE_TYPE_HEALTHCHECK, 3000, null);
		request.setSequence(1);
		request.setCreateMillisTime(System.currentTimeMillis());
		request.setCallType(Constants.CALLTYPE_REPLY);

		NettyClient client = new NettyClient(connectInfo);
		InvocationResponse response = null;
		try {
			client.connect();
			CallbackFuture future = new CallbackFuture();
			future.setRequest(request);
			future.setClient(client);
			InvokerUtils.sendRequest(client, request, future);
			response = future.get(request.getTimeout());
			if (response != null) {
				Map<String, Object> result = (Map<String, Object>) response.getReturn();
				if (result != null && result.get("version") != null) {
					logger.info("checking server:" + connectInfo + ", response:" + result);
					return true;
				}
			}
		} catch (Throwable t) {
			logger.error("Error while checking health of server:" + connectInfo, t);
		} finally {
			try {
				client.close();
			} catch (Throwable t) {
			}
		}
		return false;
	}
}
