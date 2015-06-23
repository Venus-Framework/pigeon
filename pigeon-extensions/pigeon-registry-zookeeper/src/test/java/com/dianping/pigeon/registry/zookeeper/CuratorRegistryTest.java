package com.dianping.pigeon.registry.zookeeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.test.TestingServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.registry.util.Constants;

public class CuratorRegistryTest {

	private static TestingServer server = null;
	private static Properties properties = null;

	@BeforeClass
	public static void startTestServer() throws Exception {
		server = new TestingServer();
		properties = new Properties();
		properties.put(Constants.KEY_REGISTRY_ADDRESS, server.getConnectString());
		System.out.println(properties);
	}

	@AfterClass
	public static void stopTestServer() throws Exception {
		if (server != null) {
			server.close();
			server = null;
		}
	}

	@Test
	public void testGetName() {
		Registry registry = ExtensionLoader.getExtension(Registry.class);
		registry.init(properties);
		assertEquals(registry.getName(), "curator");
	}

	@Test
	public void testRegisterService() throws Exception {
		Registry registry = ExtensionLoader.getExtension(Registry.class);
		registry.init(properties);
		registry.registerService("srv_a", "group_a", "1.1.1.1:1234", 5);
		assertEquals(registry.getServiceAddress("srv_a", "group_a"), "1.1.1.1:1234");
		assertEquals(registry.getServerWeight("1.1.1.1:1234"), 5);
		assertEquals(registry.getServiceAddress("srv_a"), "");
	}

	@Test
	public void testRegisterService2() throws Exception {
		CuratorRegistry registry = new CuratorRegistry();
		registry.init(properties);
		registry.registerPersistentNode("srv_a", "group_a", "1.1.1.1:1234", 5);
		registry.registerPersistentNode("srv_a", "group_a", "1.1.1.2:1234", 5);
		registry.registerPersistentNode("srv_a", "group_a", "1.1.1.3:1234", 5);
		String oldHosts = registry.getOldServiceAddress("srv_a", "group_a");
		System.out.println("Old hosts: " + oldHosts);
		assertTrue(oldHosts.contains("1.1.1.2:1234"));
		assertFalse(oldHosts.contains("1.1.1.4:1234"));
		String srvHosts = registry.getServiceAddress("srv_a", "group_a");
		assertTrue(srvHosts.contains("1.1.1.2:1234"));
		assertTrue(srvHosts.contains("1.1.1.4:1234"));
		System.out.println("All hosts: " + srvHosts);
	}

	@Test
	public void testUnregisterServiceStringString() throws Exception {
		Registry registry = ExtensionLoader.getExtension(Registry.class);
		registry.init(properties);
		registry.registerService("srv_a", "group_a", "1.1.1.1:1234", 5);
		registry.unregisterService("srv_a", "group_a", "1.1.1.1:1234");
		assertEquals(registry.getServiceAddress("srv_a", "group_a"), "");
		assertEquals(registry.getServerWeight("1.1.1.1:1234"), 5);
	}

	@Test
	public void testUnregisterServiceStringStringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetServerWeight() throws Exception {
		Registry registry = ExtensionLoader.getExtension(Registry.class);
		registry.init(properties);
		registry.registerService("srv_a", "group_a", "1.1.1.1:1234", 5);
		assertEquals(registry.getServerWeight("1.1.1.1:1234"), 5);
		registry.setServerWeight("1.1.1.1:1234", 7);
		assertEquals(registry.getServerWeight("1.1.1.1:1234"), 7);
	}

	@Test
	public void testGetChildren() throws Exception {
		Registry registry = ExtensionLoader.getExtension(Registry.class);
		registry.init(properties);
		registry.registerService("srv_a", "group_a", "1.1.1.1:1234", 5);
		registry.registerService("srv_a", "group_a", "1.1.1.2:1234", 6);
		registry.registerService("srv_a", "group_a", "1.1.1.3:1234", 7);
		assertTrue(registry.getServiceAddress("srv_a", "group_a").contains("1.1.1.2:1234"));
		assertEquals(registry.getChildren("/DP/SERVICE/srv_a@group_a").size(), 3);
	}

	@Test
	public void testWatch() throws Exception {
		CuratorRegistry registry = new CuratorRegistry();
		registry.init(properties);
		CuratorClient client = registry.getCuratorClient();
		client.watch("/DP/SERVER/a");
		client.watchChildren("/DP/SERVICE/a");
		client.create("/DP/SERVER/a", "1.1.1.1:1111");
		client.set("/DP/SERVER/a", "1.1.1.1:1112");
		client.set("/DP/SERVER/a", "1.1.1.1:1113");
		client.delete("/DP/SERVER/a");
		client.create("/DP/SERVER/a", "2.2.2.2:2222");
		client.createEphemeral("/DP/SERVICE/a/3.3.3.3:3333");
		client.createEphemeral("/DP/SERVICE/a/4.4.4.4:4444");
		client.delete("/DP/SERVICE/a/4.4.4.4:4444");
		client.delete("/DP/SERVICE/a/3.3.3.3:3333");
		client.delete("/DP/SERVICE/a");
		client.createEphemeral("/DP/SERVICE/a/5.5.5.5:5555");
		client.createEphemeral("/DP/SERVICE/a/6.6.6.6:6666");
		TimeUnit.SECONDS.sleep(10);
	}

	@Test
	public void integrateTest() throws Exception {
		CuratorRegistry registry = new CuratorRegistry();
		registry.init(properties);
		Process oldProcess1 = new Process(properties, false);
		Process oldProcess2 = new Process(properties, false);
		Process newProcess1 = new Process(properties, true);
		Process newProcess2 = new Process(properties, true);
		Process newProcess3 = new Process(properties, true);

		oldProcess1.register();
		oldProcess2.register();
		newProcess1.register();
		newProcess2.register();
		newProcess3.register();
		String serviceAddress = registry.getServiceAddress(serviceName, group);
		System.out.println(serviceAddress);

		oldProcess1.unregister();
		serviceAddress = registry.getServiceAddress(serviceName, group);
		System.out.println(serviceAddress);
		oldProcess1.register();
		serviceAddress = registry.getServiceAddress(serviceName, group);
		System.out.println(serviceAddress);

		newProcess1.unregister();
		serviceAddress = registry.getServiceAddress(serviceName, group);
		System.out.println(serviceAddress);
		newProcess1.register();
		serviceAddress = registry.getServiceAddress(serviceName, group);
		System.out.println(serviceAddress);

		newProcess3.unregister();
		RegistryEventListener.providerRemoved(serviceName, ip, newProcess3.getPort());
		TimeUnit.SECONDS.sleep(3);
		oldProcess1.unregister();
		RegistryEventListener.providerRemoved(serviceName, ip, oldProcess1.getPort());
	}

	static final String serviceName = "service_test";
	static final String group = "group_test";
	static final String ip = "1.1.1.1";
	static final AtomicInteger seq = new AtomicInteger(1111);
	static final int weight = 10;

	static class Process extends Thread {

		CuratorRegistry registry = null;
		boolean isEphemeral = false;
		String serverAddress = null;
		volatile boolean isExit = false;
		int port = 0;

		public Process(Properties properties, boolean isEphemeral) {
			this.isEphemeral = isEphemeral;
			this.port = seq.getAndIncrement();
			this.serverAddress = ip + ":" + port;
			registry = new CuratorRegistry();
			registry.init(properties);
			setDaemon(true);
			start();
		}

		@Override
		public void run() {
			while (!isExit) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		public String getServerAddress() {
			return serverAddress;
		}

		public int getPort() {
			return port;
		}

		public void register() throws RegistryException {
			if (isEphemeral) {
			} else {
				registry.registerPersistentNode(serviceName, group, serverAddress, weight);
			}
			System.out.println("register " + (isEphemeral ? "ephemeral " : "persistent ") + serverAddress);
		}

		public void unregister() throws RegistryException {
			if (isEphemeral) {
			} else {
				registry.unregisterPersistentNode(serviceName, group, serverAddress);
			}
			System.out.println("unregister " + (isEphemeral ? "ephemeral " : "persistent ") + serverAddress);
		}

		public void normalExit() throws RegistryException {
			unregister();
			registry.close();
			isExit = true;
			System.out.println("normal exit");
		}

		public void abnormalExit() {
			registry.close();
			isExit = true;
			System.out.println("abnormal exit");
		}

	}

}
