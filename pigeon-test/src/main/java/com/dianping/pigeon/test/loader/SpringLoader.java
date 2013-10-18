/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.remoting.provider.ServerFactory;

public final class SpringLoader {

	private static String PROVIDER_SPRING_CONFIG = "classpath*:META-INF/spring/app-provider.xml";
	private static String INVOKER_SPRING_CONFIG = "classpath*:META-INF/spring/app-invoker.xml";

	private static Map<String, SpringContainer> serverContainers = new HashMap<String, SpringContainer>();
	
	private static SpringContainer invokerSpringContainer = new SpringContainer(INVOKER_SPRING_CONFIG);

	//private static SpringContainer providerSpringContainer = new SpringContainer(PROVIDER_SPRING_CONFIG);

	public static void startupInvoker() {
		ConfigLoader.initClient();
		invokerSpringContainer.start();
		// InvokerBootStrapLoader.startup();
	}

	public static void stopInvoker() {
		invokerSpringContainer.stop();
		// InvokerBootStrapLoader.shutdown();
	}

	public static void startupProvider(int port) {
		ConfigLoader.initServer(port);
		String key = port + "";
		if(serverContainers.containsKey(key)) {
			throw new RuntimeException("existed server container:" + key);
		}
		SpringContainer providerSpringContainer = new SpringContainer(PROVIDER_SPRING_CONFIG);
		providerSpringContainer.start();
		serverContainers.put(key, providerSpringContainer);
		
//		try {
//			Thread.currentThread().join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	public static void stopProvider(int port) {
		String key = port + "";
		SpringContainer providerSpringContainer = serverContainers.get(key);
		if(providerSpringContainer != null) {
			providerSpringContainer.stop();
			serverContainers.remove(key);
		}
	}

	static class PrintThread extends Thread {

		InputStream is = null;

		public PrintThread(InputStream is) {
			this.is = is;
		}

		public void run() {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					if (line.length() > 0) {
						System.out.println(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (Exception e) {
					}
				}
			}
		}

	};

	public static void startupProviderProcess(int port) throws Exception {
		String cmd = "java -cp /Users/xiangwu/project/pigeon2/pigeon-test/target/classes com.dianping.pigeon.test.loader.SpringLoader " + port;
		try {
			Process proc = Runtime.getRuntime().exec(cmd);
			proc.waitFor();
			new PrintThread(proc.getErrorStream()).start();
			new PrintThread(proc.getInputStream()).start();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}

	public static void main(String[] args) {
		int port = ServerFactory.DEFAULT_PORT;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		startupProvider(port);
	}
}
