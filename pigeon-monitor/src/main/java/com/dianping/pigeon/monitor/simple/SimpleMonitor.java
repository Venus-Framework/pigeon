package com.dianping.pigeon.monitor.simple;

import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorTransaction;

public class SimpleMonitor implements Monitor {

	@Override
	public void init() {

	}

	@Override
	public void logError(String msg, Throwable t) {

	}

	@Override
	public void logError(Throwable t) {

	}

	@Override
	public void logEvent(String name, String event, String desc) {

	}

	@Override
	public void logMonitorError(Throwable t) {

	}

	@Override
	public MonitorTransaction createTransaction(String name, String uri, Object invocationContext, boolean autoCommit) {
		return null;
	}

	@Override
	public MonitorTransaction getCurrentServiceTransaction() {
		return null;
	}
	
	@Override
	public MonitorTransaction getCurrentCallTransaction() {
		return null;
	}

	public String toString() {
		return "SimpleMonitor";
	}

	@Override
	public MonitorTransaction copyTransaction(String name, String uri, Object invocationContext,
			MonitorTransaction transaction) {
		return transaction;
	}

	@Override
	public void clearServiceTransaction() {
		
	}

	@Override
	public void clearCallTransaction() {
		
	}

	@Override
	public void setCurrentServiceTransaction(MonitorTransaction transaction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCurrentCallTransaction(MonitorTransaction transaction) {
		// TODO Auto-generated method stub
		
	}
}
