package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.serviceDesc.SearchResultBean;
import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescBean;

import java.util.List;

/**
 * Created by shihuashen on 16/5/5.
 */
public interface DescSearchService {
    List<ServiceDescBean> search(String query);
    //暂时行搜索关键字,返回封装的Bean用以前端展示.
    SearchResultBean searchForViewBean(String query);
}
