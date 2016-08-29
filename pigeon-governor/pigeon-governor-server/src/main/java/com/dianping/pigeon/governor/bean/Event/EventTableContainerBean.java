package com.dianping.pigeon.governor.bean.Event;

import com.dianping.pigeon.util.RandomUtils;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shihuashen on 16/8/1.
 */
public class EventTableContainerBean {
    private int draw;
    private int recordsTotal;
    private int recordsFiltered;
    private List<EventBean> data;
    private int totalCount;
    public EventTableContainerBean(int draw , List<EventBean> data,int totalCount){
        this.data = data;
        this.draw = draw;
        this.recordsFiltered = data.size();
        this.recordsTotal = data.size();
        this.totalCount = totalCount;
    }
    public List<EventBean> getData(){
        return this.data;
    }
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }
}
