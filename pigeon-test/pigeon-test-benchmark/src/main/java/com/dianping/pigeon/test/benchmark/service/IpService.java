package com.dianping.pigeon.test.benchmark.service;

import com.dianping.iphub.IpInfo;
import com.dianping.iphub.exception.IpHubException;

public interface IpService {

	public void cancel();

	public void concurrentGet(final int threads, final int sleepTime);

	IpInfo getIpInfo(String ip) throws IpHubException;
}
