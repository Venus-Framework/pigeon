package com.dianping.pigeon.governor.monitor.loadBalanceMonitor.defaultAnalyzerImpl;

import com.dianping.pigeon.governor.monitor.loadBalanceMonitor.ServerClientDataComparator;

/**
 * Created by shihuashen on 16/7/13.
 */
public class DefaultServerClientComparator implements ServerClientDataComparator {
    @Override
    public boolean compare(long a, long b) {
        if(a==b)
            return true;
        return false;
    }
}
