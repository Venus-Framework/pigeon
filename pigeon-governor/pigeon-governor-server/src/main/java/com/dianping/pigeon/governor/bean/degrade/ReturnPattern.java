package com.dianping.pigeon.governor.bean.degrade;

/**
 * Created by shihuashen on 16/8/17.
 */
public enum ReturnPattern {
    DEFAULT("return null"),
    VALUE("configure return value"),
    EXCEPTION("throw exception"),
    MOCK("using mock impl to serve");
    private String name;
    ReturnPattern(String name) {
        this.name = name;
    }
    public static ReturnPattern getReturnPattern(int id){
        if(id==0)
            return DEFAULT;
        if(id==1)
            return VALUE;
        if(id==2)
            return EXCEPTION;
        if(id==3)
            return MOCK;
        return null;
    }
}
