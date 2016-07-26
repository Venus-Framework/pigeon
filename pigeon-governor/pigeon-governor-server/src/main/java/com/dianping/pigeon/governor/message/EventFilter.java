package com.dianping.pigeon.governor.message;

/**
 * Created by shihuashen on 16/7/15.
 */
public interface EventFilter {
    boolean doFilter(Event event);
    String getSignature();
    long getId();
}
