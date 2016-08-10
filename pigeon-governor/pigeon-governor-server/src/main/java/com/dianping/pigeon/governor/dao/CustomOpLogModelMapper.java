package com.dianping.pigeon.governor.dao;

import com.dianping.pigeon.governor.model.OpLog;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by shihuashen on 16/8/9.
 */
public interface CustomOpLogModelMapper {
    List<OpLog> getRecentTopNLogs(@Param("topN") int n);
    int getTotalCount();
    List<OpLog> filterOpLogs(@Param("start") Timestamp start,
                                           @Param("end") Timestamp end,
                                           @Param("projectId") int projectId,
                                           @Param("type") int type);
}
