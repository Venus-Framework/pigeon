package com.dianping.pigeon.governor.bean.comments;

import java.util.Date;

/**
 * Created by shihuashen on 16/6/22.
 */
public class CommentBean {
    private int id;
    private String title;
    private String content;
    private Date updateTime;
    private String author;
    private String replyAuthor;
    private boolean empowered;
    private int supportedNumber;
    private boolean supported;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getReplyAuthor() {
        return replyAuthor;
    }

    public void setReplyAuthor(String replyAuthor) {
        this.replyAuthor = replyAuthor;
    }

    public boolean isEmpowered() {
        return empowered;
    }

    public void setEmpowered(boolean empowered) {
        this.empowered = empowered;
    }

    public int getSupportedNumber() {
        return supportedNumber;
    }

    public void setSupportedNumber(int supportedNumber) {
        this.supportedNumber = supportedNumber;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }
}
