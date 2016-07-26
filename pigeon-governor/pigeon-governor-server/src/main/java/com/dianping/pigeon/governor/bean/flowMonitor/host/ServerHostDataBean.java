package com.dianping.pigeon.governor.bean.flowMonitor.host;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;

/**
 * Created by shihuashen on 16/7/7.
 */
public class ServerHostDataBean {
    private String methodName;
    private long callTime;
    private long failureTime;
    private double failureRate;
    private double minConsumeTime;
    private double maxConsumeTime;
    private double avgConsumeTime;
    private double line95;
    private double line99;
    private double std;
    private double QPS;
    public ServerHostDataBean(TransactionName name){
        this.methodName = name.getId();
        this.callTime = name.getTotalCount();
        this.failureTime = name.getFailCount();
        this.failureRate = name.getFailPercent();
        this.minConsumeTime = name.getMin();
        this.maxConsumeTime = name.getMax();
        this.avgConsumeTime = name.getAvg();
        this.line95 = name.getLine95Value();
        this.line99 = name.getLine99Value();
        this.std = name.getStd();
        this.QPS = name.getTps();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long getCallTime() {
        return callTime;
    }

    public void setCallTime(long callTime) {
        this.callTime = callTime;
    }

    public long getFailureTime() {
        return failureTime;
    }

    public void setFailureTime(long failureTime) {
        this.failureTime = failureTime;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public double getMinConsumeTime() {
        return minConsumeTime;
    }

    public void setMinConsumeTime(double minConsumeTime) {
        this.minConsumeTime = minConsumeTime;
    }

    public double getMaxConsumeTime() {
        return maxConsumeTime;
    }

    public void setMaxConsumeTime(double maxConsumeTime) {
        this.maxConsumeTime = maxConsumeTime;
    }

    public double getAvgConsumeTime() {
        return avgConsumeTime;
    }

    public void setAvgConsumeTime(double avgConsumeTime) {
        this.avgConsumeTime = avgConsumeTime;
    }

    public double getLine95() {
        return line95;
    }

    public void setLine95(double line95) {
        this.line95 = line95;
    }

    public double getLine99() {
        return line99;
    }

    public void setLine99(double line99) {
        this.line99 = line99;
    }

    public double getStd() {
        return std;
    }

    public void setStd(double std) {
        this.std = std;
    }

    public double getQPS() {
        return QPS;
    }

    public void setQPS(double QPS) {
        this.QPS = QPS;
    }
}
