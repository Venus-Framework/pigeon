package com.dianping.pigeon.governor.message;


import java.util.Date;
import java.util.List;

/**
 * Created by shihuashen on 16/7/15.
 */
public interface Event {
    String getSignature();
    String getTitle();
    String getContent();
    Date getCreateTime();
    String getSummary();
    int getLevel();

}
