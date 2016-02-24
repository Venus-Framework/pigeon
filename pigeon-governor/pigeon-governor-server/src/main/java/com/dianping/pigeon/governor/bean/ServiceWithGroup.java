package com.dianping.pigeon.governor.bean;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Created by chenchongze on 16/1/21.
 */
public class ServiceWithGroup {

    private final String service;
    private final String group;

    public ServiceWithGroup(String service, String group) {
        if(service != null) {
            this.service = service;
        } else {
            this.service = "";
        }
        if(group != null) {
            this.group = group;
        } else {
            this.group = "";
        }
    }

    public String getGroup() {
        return group;
    }

    public String getService() {
        return service;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
