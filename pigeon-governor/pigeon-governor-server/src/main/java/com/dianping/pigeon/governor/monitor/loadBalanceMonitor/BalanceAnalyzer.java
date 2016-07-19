package com.dianping.pigeon.governor.monitor.loadBalanceMonitor;

import java.util.Collection;

/**
 * Created by shihuashen on 16/7/13.
 */
public interface BalanceAnalyzer {
    boolean balanceAnalysis(Collection<Long> data);
}
