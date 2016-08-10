package com.dianping.pigeon.governor.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dianping.pigeon.registry.exception.RegistryException;
import org.apache.commons.collections.CollectionUtils;

import com.dianping.pigeon.governor.lion.registry.DefaultCuratorClient;
import com.dianping.pigeon.governor.lion.registry.ZkListenerFactory;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.service.RegistryService;
import com.dianping.pigeon.registry.zookeeper.Utils;
import com.dianping.pigeon.remoting.common.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Deprecated
@org.springframework.stereotype.Service("doubleWriteRegistrySerivce")
public class DoubleWriteRegistryServiceImpl implements RegistryService {

	private Logger logger = LogManager.getLogger();

	@Override
	public void registryUpdateService(Service oriService, Service newService) throws RegistryException {
		try {
			DefaultCuratorClient lionZkClient = ZkListenerFactory.getLionZkClient();
			DefaultCuratorClient pigeonZkClient = ZkListenerFactory.getPigeonZkClient();

			String servicePath = Utils.getServicePath(newService.getName(), newService.getGroup());

			lionZkClient.set(servicePath, newService.getHosts());
			pigeonZkClient.set(servicePath, newService.getHosts());

			Set<String> oriHostSet = new HashSet<String>();
			for(String host : oriService)
                oriHostSet.add(host);

			Set<String> hostSet = new HashSet<String>();
			for(String host : newService)
                hostSet.add(host);

			Collection<String> addSet = CollectionUtils.subtract(hostSet, oriHostSet);
			for(String host : addSet) {
                String path = Utils.getWeightPath(host);
                lionZkClient.set(path, Constants.WEIGHT_DEFAULT);
                pigeonZkClient.set(path, Constants.WEIGHT_DEFAULT);

            }
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RegistryException(e);
		}

	}
	
	@Override
	public void registryCreateService(Service service) throws RegistryException {
		try {
			DefaultCuratorClient lionZkClient = ZkListenerFactory.getLionZkClient();
			DefaultCuratorClient pigeonZkClient = ZkListenerFactory.getPigeonZkClient();

			String servicePath = Utils.getServicePath(service.getName(), service.getGroup());

			lionZkClient.set(servicePath, service.getHosts());
			pigeonZkClient.set(servicePath, service.getHosts());

			Set<String> hostSet = new HashSet<String>();
			for(String host : service)
                hostSet.add(host);

			for(String host : hostSet) {
                String path = Utils.getWeightPath(host);
                lionZkClient.set(path, Constants.WEIGHT_DEFAULT);
                pigeonZkClient.set(path, Constants.WEIGHT_DEFAULT);
            }
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RegistryException(e);
		}

	}

	@Override
	public void registryDeleteService(Service service) throws RegistryException {
		try {
			DefaultCuratorClient lionZkClient = ZkListenerFactory.getLionZkClient();
			DefaultCuratorClient pigeonZkClient = ZkListenerFactory.getPigeonZkClient();

			String servicePath = Utils.getServicePath(service.getName(), service.getGroup());

			List<String> children = lionZkClient.getChildren(servicePath);
			if (children != null && children.size() > 0) {
                lionZkClient.set(servicePath, "");
            } else {
                lionZkClient.deleteWithChildren(servicePath);
            }

			children = pigeonZkClient.getChildren(servicePath);
			if (children != null && children.size() > 0) {
                pigeonZkClient.set(servicePath, "");
            } else {
                pigeonZkClient.deleteWithChildren(servicePath);
            }
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RegistryException(e);
		}
	}

	@Override
	public String getServiceHosts(String serviceName, String group) throws RegistryException {
		return null;
	}

	@Override
	public void registryUpdateService(String serviceName, String group, Set<String> newHosts, Set<String> toAddHosts) throws RegistryException {

	}
}
