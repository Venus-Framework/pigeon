package com.dianping.pigeon.governor.monitor.loadBalanceMonitor.defaultAnalyzerImpl;

import com.dianping.pigeon.governor.monitor.loadBalanceMonitor.BalanceAnalyzer;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by shihuashen on 16/7/13.
 */
public class DefaultBalanceAnalyzer implements BalanceAnalyzer{
    private double threshold = 0.3;
    public DefaultBalanceAnalyzer(double threshold){
        this.threshold = threshold;
    }
    @Override
    public boolean balanceAnalysis(Collection<Long> data) {
        long sum = 0;
        long size = data.size();
        for(Iterator<Long> iterator = data.iterator();iterator.hasNext();){
            sum += iterator.next();
        }
        double avg = (double)sum/(double)size;
        for(Iterator<Long> iterator = data.iterator();iterator.hasNext();){
            long value = iterator.next();
            double D_value = Math.abs(avg-value);
            if(D_value/avg>threshold){
                return false;
            }
        }
        return true;
    }
}
