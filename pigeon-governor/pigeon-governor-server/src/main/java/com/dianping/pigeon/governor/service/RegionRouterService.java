package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.exception.LionNullProjectException;

/**
 * Created by shihuashen on 16/6/14.
 */
public interface RegionRouterService {
    boolean getEnableState(String projectName) throws LionNullProjectException;
    boolean setEnableState(String projectName,String state);
}
