package com.dianping.pigeon.governor.bean.op;

import com.dianping.pigeon.governor.util.GsonUtils;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by shihuashen on 16/8/9.
 */
public class OpLogBean {
    private int id;
    private String userName;
    private String projectName;
    private String type;
    private String content;
    private String ipAddresses;
    private String time;




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(String ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
