/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.test.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.LoggerLoader;
import com.dianping.pigeon.remoting.provider.ServerFactory;
import com.dianping.pigeon.util.NetUtils;

public class EchoServiceImpl implements EchoService {

	Logger logger = LoggerLoader.getLogger(EchoServiceImpl.class);

	@Override
	public String echo(String input) {
		// String tname1 = Thread.currentThread().getName();
		if (input.equals("200000")) {
			System.out.println("sleep......");
			try {
				Thread.currentThread().sleep(10000);
			} catch (InterruptedException e) {
			}
		}
		// try {
		// Thread.currentThread().sleep(100);
		// } catch (InterruptedException e) {
		// }
		// String tname2 = Thread.currentThread().getName();
		// if (!tname1.equals(tname2)) {
		// System.out.println(input + ",t1:" + tname1 + ",t2:" + tname2);
		// }

		return "echo:" + input;
	}

	@Override
	public String echoWithException1(String input) {
		System.out.println("server port:" + ExtensionLoader.getExtension(ServerFactory.class).getPort()
				+ ", received: " + input);
		throw new EchoException1("error while receive msg:" + input);
	}

	@Override
	public String echoWithException2(String input) {
		System.out.println("server port:" + ExtensionLoader.getExtension(ServerFactory.class).getPort()
				+ ", received: " + input);
		throw new EchoException2("error while receive msg:" + input);
	}

	@Override
	public String echoWithServerInfo(String input) {
		String serverInfo = "server:" + NetUtils.getFirstLocalIp() + ":"
				+ ExtensionLoader.getExtension(ServerFactory.class).getPort();
		// System.out.println("reveived: " + input);
		return serverInfo + ", echo:" + input;
	}
}
