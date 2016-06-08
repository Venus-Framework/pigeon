package com.dianping.pigeon.governor.service;

import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescBean;

import java.util.List;

/**
 * Created by shihuashen on 16/5/26.
 */
public interface EsService {
    //TODO 仅为测试用.后续需要考虑分页,高亮,是否将其封装为一个方便前端展示的Bean.
    List<ServiceDescBean> search(String key);
}
