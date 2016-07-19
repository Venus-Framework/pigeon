package com.dianping.pigeon.governor.monitor.loadBalanceMonitor;

/**
 * Created by shihuashen on 16/7/13.
 */
public interface ServerClientDataComparator {
    boolean compare(long a,long b);
}
