package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.bean.serviceDesc.MethodMeta;
import com.dianping.pigeon.governor.bean.serviceDesc.SearchResultBean;
import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescBean;
import com.dianping.pigeon.governor.bean.serviceDesc.ServiceMeta;
import com.dianping.pigeon.governor.dao.CombinedServiceDescMapper;
import com.dianping.pigeon.governor.dao.MethodDescMapper;
import com.dianping.pigeon.governor.dao.ServiceDescMapper;
import com.dianping.pigeon.governor.dao.ServiceMapper;

import com.dianping.pigeon.governor.service.DescSearchService;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by shihuashen on 16/5/5.
 */
@Service
public class DescSearchServiceImpl implements DescSearchService {
    @Autowired
    private CombinedServiceDescMapper combinedServiceDescMapper;
    @Autowired
    private ServiceMapper serviceMapper;
    @Autowired
    private ServiceDescMapper serviceDescMapper;
    @Autowired
    private MethodDescMapper methodDescMapper;

    @Override
    public List<ServiceDescBean> search(String query) {
        return null;
    }

    @Override
    public SearchResultBean searchForViewBean(String query) {
        long start = System.currentTimeMillis();
        SearchResultBean resultBean = new SearchResultBean();
        List<ServiceMeta> serviceMetas = combinedServiceDescMapper.selectServiceMetas("%"+query+"%");
        List<MethodMeta> methodMetas = combinedServiceDescMapper.selectMethodMetas("%"+query+"%");
        resultBean.setServiceMetas(serviceMetas);
        resultBean.setMethodMetas(methodMetas);
        System.out.println("本次查询耗时:"+(System.currentTimeMillis()-start)+"ms");
        return resultBean;
    }



}
