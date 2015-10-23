package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.model.Host;

/**
 * Created by chenchongze on 15/10/23.
 */
public interface HostService {

    public int create(Host host);

    public Host retrieveByIpPort(String ip, String port);

    public int update(Host host);
}
