package com.dianping.pigeon.governor.service.impl;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.service.CatReportService;
import com.dianping.pigeon.governor.util.CatReportXMLUtils;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.HttpCallUtils;
import org.dom4j.Document;
import org.springframework.stereotype.Service;

/**
 * Created by shihuashen on 16/6/29.
 */
@Service
public class CatReportServiceImpl implements CatReportService{
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    @Override
    public TransactionReport getCatTransactionReport(String projectName, String date, String ip,String type) {
        String url = getCatAddress()+"cat/r/t?domain="+projectName+"&date="+date+"&ip="+ip+"&type="+type+"&forceDownload=xml";
        String xml = HttpCallUtils.httpGet(url);
        return CatReportXMLUtils.convertXMLToModel(CatReportXMLUtils.XMLFitFormat(xml));
    }

    private String getCatAddress(){
        String env = configManager.getEnv();
        if(env.equals("qa"))
            return Constants.qaCatAddress;
        if(env.equals("prelease"))
            return Constants.ppeCatAddress;
        return Constants.onlineCatAddress;
    }
}
