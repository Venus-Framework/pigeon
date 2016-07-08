package com.dianping.pigeon.console.domain;

import com.dianping.pigeon.remoting.invoker.route.quality.RequestQualityManager;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenchongze on 16/6/29.
 */
public class RequestQualityStatus {

    private Boolean support = Boolean.FALSE;

    private ConcurrentHashMap<String, ConcurrentHashMap<String, RequestQualityManager.Quality>> addrReqUrlQualities
            = new ConcurrentHashMap<String, ConcurrentHashMap<String, RequestQualityManager.Quality>>();

    public Boolean getSupport() {
        return support;
    }

    public void setSupport(Boolean support) {
        this.support = support;
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, RequestQualityManager.Quality>> getAddrReqUrlQualities() {
        return addrReqUrlQualities;
    }

    public void setAddrReqUrlQualities(ConcurrentHashMap<String, ConcurrentHashMap<String, RequestQualityManager.Quality>> addrReqUrlQualities) {
        this.addrReqUrlQualities = addrReqUrlQualities;
    }
}
