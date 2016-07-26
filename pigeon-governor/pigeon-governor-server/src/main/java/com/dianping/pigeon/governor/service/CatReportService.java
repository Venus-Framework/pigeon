package com.dianping.pigeon.governor.service;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.pigeon.governor.bean.flowMonitor.host.ServerHostDataTableBean;
import com.dianping.pigeon.governor.bean.flowMonitor.method.MethodDistributedGraphBean;

/**
 * Created by shihuashen on 16/6/29.
 */
public interface CatReportService {
    TransactionReport getCatTransactionReport(String projectName,String date,String ip,String type);
    MethodDistributedGraphBean getMethodDistributedGraph(String projectName,String date,String nameId);
    ServerHostDataTableBean getServerHostTable(String projectName,String date, String ip);
}
