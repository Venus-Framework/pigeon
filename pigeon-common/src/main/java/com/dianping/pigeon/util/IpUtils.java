/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.springframework.util.Assert;

/**
 * @author xiangwu
 * @Oct 11, 2013
 * 
 */
public class IpUtils {

	public static List<InetAddress> getAllLocalAddress() {
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			List<InetAddress> addresses = new ArrayList<InetAddress>();

			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					addresses.add(inetAddress);
				}
			}

			return addresses;
		} catch (SocketException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static List<String> getAllLocalIp() {
		List<String> noLoopbackAddresses = new ArrayList<String>();
		List<InetAddress> allInetAddresses = getAllLocalAddress();

		for (InetAddress address : allInetAddresses) {
			if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
				noLoopbackAddresses.add(address.getHostAddress());
			}
		}

		return noLoopbackAddresses;
	}

	public static String getFirstLocalIp() {
		List<String> allNoLoopbackAddresses = getAllLocalIp();
		Assert.isTrue(!allNoLoopbackAddresses.isEmpty(), " Sorry, seems you don't have a network card :( ");
		return allNoLoopbackAddresses.get(allNoLoopbackAddresses.size() - 1);
	}
}
