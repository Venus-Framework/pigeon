package com.dianping.pigeon.governor.bean.scanServiceDesc;

import java.util.List;

/**
 * Created by shihuashen on 16/4/27.
 */
public class JsonService {
    private String name;
    private boolean published;
    private String type;
    private List<JsonMethod> methods;

    public String getName(){
        return name;
    }

    public List<JsonMethod> getMethods(){
        return this.methods;
    }

    public String getType(){
        return this.type;
    }

}
