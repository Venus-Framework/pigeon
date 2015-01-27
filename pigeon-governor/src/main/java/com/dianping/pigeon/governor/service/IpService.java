package com.dianping.pigeon.governor.service;

import com.dianping.iphub.IpInfo;
import com.dianping.iphub.exception.IpHubException;

public interface IpService {

	public void cancel();

	public void concurrentGet(int threads);

	IpInfo getIpInfo(String ip) throws IpHubException;
}
