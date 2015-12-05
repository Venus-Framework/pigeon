package com.dianping.pigeon.governor.bean;

/**
 * Created by chenchongze on 15/11/27.
 */
public class ConsoleService {

    private String name;
    private String published;
    private String type;
    private Object[] methods;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object[] getMethods() {
        return methods;
    }

    public void setMethods(Object[] methods) {
        this.methods = methods;
    }
}
