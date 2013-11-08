/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.provider.listener;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import com.dianping.pigeon.event.RuntimeServiceEvent;
import com.dianping.pigeon.event.RuntimeServiceListener;
import com.dianping.pigeon.monitor.Log4jLoader;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: DynamicMonitorListener.java, v 0.1 2013-7-19 涓��1:32:29
 *          jianhuihuang Exp $
 */
public class SystemMonitorListener implements RuntimeServiceListener {

	final static String name = "provider-system-monitor";
	private static final Logger logger = Log4jLoader.getLogger(name);
	private static final String SPLITTER = ",";
	private static final DecimalFormat decimalformat = new DecimalFormat("#.##");
	private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

	public void handleEvent(RuntimeServiceEvent event) {

		Thread monitor = new Thread() {
			public void run() {
				try {
					while (true) {
						logger.info(getSysInfo());
						sleep(30000);
					}
				} catch (Exception e) {
					// do nothing
				}
			}
		};

		monitor.setDaemon(true);
		monitor.setName(name);
		monitor.start();
	}

	public boolean support(RuntimeServiceEvent event) {
		if (event.getEventType() == RuntimeServiceEvent.Type.RUNTIME_STARTED) {
			return true;
		}

		return false;
	}

	private String getSysInfo() {
		MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(formatValue(memoryUsage.getUsed())).append(SPLITTER);
		sb.append(formatValue(memoryUsage.getMax()));
		sb.append(")");

		sb.append("(");
		sb.append(formatNanosecond(threadMXBean.getCurrentThreadCpuTime())).append(SPLITTER);
		sb.append(threadMXBean.getDaemonThreadCount()).append(SPLITTER);
		sb.append(threadMXBean.getThreadCount()).append(SPLITTER);
		sb.append(threadMXBean.getTotalStartedThreadCount());
		sb.append(")");

		return sb.toString();
	}

	private static String formatValue(long value) {
		Double tempValue = new Double(value) / 1024 / 1024;
		return decimalformat.format(tempValue);
	}

	private String formatNanosecond(long value) {
		Double tempValue = new Double(value) / 1000000000;
		return decimalformat.format(tempValue);
	}

}
