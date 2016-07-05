package com.dianping.pigeon.governor.service;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

/**
 * Created by shihuashen on 16/6/29.
 */
public interface CatReportService {
    TransactionReport getCatTransactionReport(String projectName,String date,String ip,String type);
}
