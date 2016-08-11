package com.dianping.pigeon.governor.bean.Event;

import com.dianping.pigeon.governor.util.GsonUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by shihuashen on 16/8/10.
 */
public class FlowSkewEventBean extends EventDetailBean{
    List<GraphData> graphDatas =  new LinkedList<GraphData>();
    public List<GraphData> getGraphDatas() {
        return graphDatas;
    }
    public void setGraphDatas(List<GraphData> graphDatas) {
        this.graphDatas = graphDatas;
    }
    class GraphData {
        private String name;
        private double y;

        public GraphData(String name, double y) {
            this.name = name;
            this.y = y;
        }
    }
}