package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.degrade.DegradeConfig;
import com.dianping.pigeon.governor.exception.LionNullProjectException;

import java.util.List;

/**
 * Created by shihuashen on 16/8/17.
 */
public interface InvokerDegradeService {
    boolean getForceDegradeState(String projectName) throws LionNullProjectException;
    boolean setForceDegradeState(String projectName, String state);
    boolean getAutoDegradeState(String projectName) throws LionNullProjectException;
    boolean setAutoDegradeState(String projectName,String state);
    boolean getFailureDegradeState(String projectName) throws LionNullProjectException;
    boolean setFailureDegradeState(String projectName,String state);
    boolean addDegradeConfig(DegradeConfig config);
    boolean updateDegradeConfig(DegradeConfig config);
    boolean deleteDegradeConfig(DegradeConfig config);
    List<DegradeConfig> getDegradeConfigs(String projectName);
    double getRecoverPercentage(String project) throws LionNullProjectException;
    boolean setRecoverPercentage(String project,double value);
}
