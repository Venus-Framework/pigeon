package com.dianping.pigeon.governor.service.impl;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.bean.flowMonitor.host.ServerHostDataTableBean;
import com.dianping.pigeon.governor.bean.flowMonitor.method.MethodDistributedGraphBean;
import com.dianping.pigeon.governor.service.CatReportService;
import com.dianping.pigeon.governor.util.CatReportXMLUtils;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.dianping.pigeon.governor.util.HttpCallUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by shihuashen on 16/6/29.
 */
@Service
public class CatReportServiceImpl implements CatReportService{
    private ConfigManager configManager = ConfigManagerLoader.getConfigManager();


    //TODO implements Cache
    private TransactionReport cacheFetch(){
        return null;
    }


    @Override
    public TransactionReport getCatTransactionReport(String projectName, String date, String ip,String type) {
        String url = getCatAddress()+"cat/r/t?domain="+projectName+"&date="+date+"&ip="+ip+"&type="+type+"&forceDownload=xml";
        String xml = HttpCallUtils.httpGet(url);
        return CatReportXMLUtils.convertXMLToModel(CatReportXMLUtils.XMLFitFormat(xml));
    }

    @Override
    public MethodDistributedGraphBean getMethodDistributedGraph(String projectName, String date, String nameId) {
        TransactionReport allReport = getCatTransactionReport(projectName,date,"All","PigeonService");
        String tmpIp = allReport.getIps().iterator().next();
        TransactionReport report = getCatTransactionReport(projectName,date,tmpIp,"PigeonService");
        System.out.println(GsonUtils.prettyPrint(GsonUtils.toJson(report)));
        Map<String,Long> dataMap = new HashMap<String, Long>();
        for(Iterator<String> iterator = report.getMachines().keySet().iterator();iterator.hasNext();){
            String ip = iterator.next();
            long visitData = report.getMachines().get(ip).getTypes().get("PigeonService").getNames().get(nameId).getTotalCount();
            dataMap.put(ip,visitData);
        }
        return new MethodDistributedGraphBean(dataMap);
    }

    @Override
    public ServerHostDataTableBean getServerHostTable(String projectName, String date, String ip) {
        TransactionReport hostReport = getCatTransactionReport(projectName,date,ip,"PigeonService");
        return new ServerHostDataTableBean(hostReport.getMachines().get(ip).getTypes().get("PigeonService").getNames().values(),projectName,date,ip);
    }

    private String getCatAddress(){
        return Constants.onlineCatAddress;
//        String env = configManager.getEnv();
//        if(env.equals("qa"))
//            return Constants.qaCatAddress;
//        if(env.equals("prelease"))
//            return Constants.ppeCatAddress;
//        return Constants.onlineCatAddress;
    }
}
