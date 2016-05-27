package com.dianping.pigeon.governor.bean.scanServiceDesc;

import java.util.List;

/**
 * Created by shihuashen on 16/4/27.
 */
public class JsonMethod {
    private String name;
    private List<String> parameterTypes;
    private String returnType;

    public String getName(){
        return name;
    }

    public List<String> getParameterTypes(){
        return this.parameterTypes;
    }

    public String getReturnType(){
        return this.returnType;
    }
}
