package com.dianping.pigeon.governor.bean.op;

import java.util.List;

/**
 * Created by shihuashen on 16/8/9.
 */
public class OpLogContainerBean {
    private int recordsFiltered;
    private List<OpLogBean> data;
    private int totalCount;
    public OpLogContainerBean(List<OpLogBean> data,int totalCount){
        this.data = data;
        this.recordsFiltered = data.size();
        this.totalCount = totalCount;
    }
    public List<OpLogBean> getData(){
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
