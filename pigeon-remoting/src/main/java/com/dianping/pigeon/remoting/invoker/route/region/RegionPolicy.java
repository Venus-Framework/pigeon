package com.dianping.pigeon.remoting.invoker.route.region;

import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;

import java.util.List;

/**
 * Created by chenchongze on 16/4/15.
 */
public interface RegionPolicy {

    public List<Client> getPreferRegionClients(List<Client> clientList, InvocationRequest request);
}
