package com.dianping.pigeon.governor.dao;

import com.dianping.pigeon.governor.model.EventModelWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by shihuashen on 16/8/1.
 */
public interface CustomEventModelMapper {
    List<EventModelWithBLOBs> getHotProjects(Integer count);
    List<EventModelWithBLOBs> getRecentEvents(@Param("size") Integer size);
    List<EventModelWithBLOBs> filterEvents(@Param("start") Timestamp start,
                                           @Param("end") Timestamp end,
                                           @Param("projectId") int projectId,
                                           @Param("levels") List<Integer> levels,
                                           @Param("types") List<Integer> types);
    int getTotalCount();
}
