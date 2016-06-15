package com.dianping.pigeon.governor.service;

/**
 * Created by shihuashen on 16/6/14.
 */
public interface RegionRouterService {
    boolean getEnableState(String projectName);
    boolean setEnableState(String projectName,String state);
}
