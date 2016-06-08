package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescBean;
import com.dianping.pigeon.governor.service.EsService;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by shihuashen on 16/5/26.
 */
@Service
public class EsServiceImpl implements EsService {
    private Client esClient;
    public EsServiceImpl(){
        Settings setting = Settings.settingsBuilder()
                .put("cluster.name","elasticsearch").build();
        esClient = TransportClient.builder().settings(setting).build();
    }
    @Override
    public List<ServiceDescBean> search(String key) {
        return null;
    }
}
