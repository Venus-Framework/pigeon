package com.dianping.pigeon.governor.service;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;

/**
 * Created by shihuashen on 16/7/11.
 */
public interface CrossReportService {
    CrossReport getCrossReport(String projectName,String dateTime,String ip);
    void serverAndClientCountCheck(CrossReport report);
}
