package com.dianping.pigeon.governor.service;

/**
 * Created by shihuashen on 16/8/9.
 * This cache level is mainly used to accelerate batch db access.
 * The cache refresh interval may cause low consistence so most of the method are invoked for web table view.
 */
public interface CacheService {
    String getProjectNameWithId(int projectId);
    String getUserNameWithDpAccount(String dpAccount);
}
