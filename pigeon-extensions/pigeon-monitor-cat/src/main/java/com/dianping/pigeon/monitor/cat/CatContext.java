package com.dianping.pigeon.monitor.cat;

import com.dianping.cat.Cat;
import com.dianping.pigeon.monitor.MonitorConstants;
import com.dianping.pigeon.util.ContextUtils;

/**
 * @author qi.yin
 *         2016/08/16  下午4:19.
 */
public class CatContext implements Cat.Context {

    @Override
    public void addProperty(String key, String value) {
        if (Cat.Context.ROOT.equals(key)) {
            ContextUtils.putRequestContext(MonitorConstants.ROOT_MSG_ID, value);
        } else if (Cat.Context.PARENT.equals(key)) {
            ContextUtils.putRequestContext(MonitorConstants.CURRENT_MSG_ID, value);
        } else if (Cat.Context.CHILD.equals(key)) {
            ContextUtils.putRequestContext(MonitorConstants.SERVER_MSG_ID, value);
        } else {
            throw new IllegalArgumentException("monitor no releted key = " + key);
        }
    }

    @Override
    public String getProperty(String key) {
        if (Cat.Context.ROOT.equals(key)) {
            return (String) ContextUtils.getLocalContext(MonitorConstants.ROOT_MSG_ID);
        } else if (Cat.Context.PARENT.equals(key)) {
            return (String) ContextUtils.getLocalContext(MonitorConstants.CURRENT_MSG_ID);
        } else if (Cat.Context.CHILD.equals(key)) {
            return (String) ContextUtils.getLocalContext(MonitorConstants.SERVER_MSG_ID);
        } else {
            throw new IllegalArgumentException("monitor no releted key = " + key);
        }
    }
}
