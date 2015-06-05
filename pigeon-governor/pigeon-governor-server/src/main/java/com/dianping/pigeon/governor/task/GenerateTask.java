package com.dianping.pigeon.governor.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.util.AddressUtils;
import com.dianping.pigeon.governor.util.AddressUtils.Address;
import com.dianping.pigeon.governor.util.Constants.Environment;
import com.dianping.pigeon.governor.util.Constants.Host;
import com.dianping.pigeon.governor.util.Constants.Service;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;

public class GenerateTask implements Runnable {

	private static final Logger logger = LoggerLoader.getLogger(GenerateTask.class);

	private static final String ROOT = ConfigManagerLoader.getConfigManager().getStringValue("pigeon.governor.root",
			"/DP/SERVER");

	private static ConcurrentHashMap<String, Service> services = new ConcurrentHashMap<String, Service>();

	private HealthCheckManager manager;
	private AddressRepo addrRepo;

	public GenerateTask(HealthCheckManager manager) {
		this.manager = manager;
		addrRepo = new AddressRepo();
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				logger.info("round of health check started");
				generateTasks();
				addrRepo.clear();
				waitForTaskComplete();
				services.clear();
				manager.getResultQueue().clear();
				logger.info("round of health check finished");
				Thread.sleep(manager.getInterval());
			} catch (RegistryException e) {
				logger.error("", e);
			} catch (InterruptedException e) {
				logger.warn("HealthCheckManager is interrupted", e);
			} catch (Throwable e) {
				logger.error("", e);
			}
		}
	}

	private void waitForTaskComplete() throws InterruptedException {
		AtomicInteger n = new AtomicInteger(0);
		while (manager.getWorkerPool().getActiveCount() > 0 || !manager.getResultQueue().isEmpty()) {
			if (n.getAndIncrement() % 30 == 0) {
				String message = String.format("active threads: %d, queue size: %d, completed task: %d", manager
						.getWorkerPool().getActiveCount(), manager.getWorkerPool().getQueue().size(), manager
						.getWorkerPool().getCompletedTaskCount());
				if (logger.isInfoEnabled()) {
					logger.info(message);
				}
			}
			Thread.sleep(1000);
		}
	}

	private void generateTasks() throws RegistryException {
		for (Environment env : manager.getEnvSet()) {
			generateTasks(env);
		}
	}

	private void generateTasks(Environment env) throws RegistryException {
		Registry registry = manager.getRegistry(env);
		List<String> children = registry.getChildren(ROOT);
		for (String path : children) {
			if (path.startsWith("@HTTP@")) {
				String hosts = registry.getServiceAddress(path);
				generateTasks(env, path.substring(6), hosts);
			}
		}
	}

	private void generateTasks(Environment env, String path, String pigeon2hosts) throws RegistryException {
		Registry registry = manager.getRegistry(env);
		String service = path.replace('^', '/');
		String hosts = registry.getServiceAddress(path);
		generateTasks(env, service, "", hosts, pigeon2hosts);
		generateTasks(env, "@HTTP@" + service, "", pigeon2hosts, pigeon2hosts);

		if (hosts != null) {
			List<String> groups = registry.getChildren(ROOT + "/" + path);
			if (groups != null && groups.size() > 0) {
				for (String group : groups) {
					String groupHosts = registry.getServiceAddress(path, group);
					generateTasks(env, service, group, groupHosts, pigeon2hosts);
				}
			}
		}

		if (pigeon2hosts != null) {
			List<String> groups = registry.getChildren(ROOT + "/@HTTP@" + path);
			if (groups != null && groups.size() > 0) {
				for (String group : groups) {
					String groupHosts = registry.getServiceAddress("@HTTP@" + path, group);
					generateTasks(env, "@HTTP@" + service, group, groupHosts, pigeon2hosts);
				}
			}
		}
	}

	private Service getService(Environment env, String url, String group) {
		String key = env.toString() + "#" + url + "#" + group;
		Service service = new Service(env, url, group);
		Service service2 = services.putIfAbsent(key, service);
		if (service2 == null) {
			service2 = service;
		}
		return service2;
	}

	private void generateTasks(Environment env, String url, String group, String hosts, String pigeon2hosts) {
		if (hosts == null)
			return;

		String[] addrArray = hosts.split(",");
		Service service = getService(env, url, group);
		List<CheckTask> tasks = new ArrayList<CheckTask>();
		for (String address : addrArray) {
			if (StringUtils.isBlank(address)) {
				continue;
			}
			Address ad = AddressUtils.toAddress(address);
			if (ad.isValid()) {
				Host host = new Host(service, ad.getIp(), ad.getPort());
				if (!addrRepo.contains(env, address)) {
					addrRepo.add(env, address);
					if (pigeon2hosts.indexOf(ad.getIp()) == -1) {
						host.setCheckResponse(false);
					}
					CheckTask task = new CheckTask(manager, host);
					tasks.add(task);
				}
				service.addHost(host);
			} else {
				logger.warn(env.name() + "#invalid address:" + address + " for service:" + url);
			}
		}
		for (CheckTask task : tasks) {
			manager.getWorkerPool().submit(task);
		}
	}

	class AddressRepo {
		private Map<Environment, Set<String>> addrRepo;

		public AddressRepo() {
			addrRepo = new HashMap<Environment, Set<String>>();
		}

		public void clear() {
			for (Map.Entry<Environment, Set<String>> entry : addrRepo.entrySet()) {
				Environment env = entry.getKey();
				Set<String> addrSet = entry.getValue();
				if (logger.isDebugEnabled())
					logger.debug(String.format("generated %d health check tasks for env %s[%s]", addrSet.size(), env,
							env.getZkAddress()));
				addrSet.clear();
			}
		}

		public void add(Environment env, String address) {
			Set<String> addrSet = addrRepo.get(env);
			if (addrSet == null) {
				addrSet = new HashSet<String>();
				addrRepo.put(env, addrSet);
			}
			addrSet.add(address);
		}

		public boolean contains(Environment env, String address) {
			Set<String> addrSet = addrRepo.get(env);
			if (addrSet == null) {
				return false;
			}
			return addrSet.contains(address);
		}
	}

}
