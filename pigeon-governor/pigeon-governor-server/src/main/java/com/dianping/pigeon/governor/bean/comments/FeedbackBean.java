package com.dianping.pigeon.governor.bean.comments;

import java.util.Date;

/**
 * Created by shihuashen on 16/6/22.
 */
public class FeedbackBean {
    private int id;
    private String title;
    private String content;
    private int commentsNumber;
    private int supportsNumber;
    private String author;
    private Date updateTime;
    private boolean empowered;
    private boolean supported;
    public FeedbackBean(){

    }
    public FeedbackBean(int id, String author, int supportsNumber, String title, String content, int commentsNumber, Date updateTime, boolean empowered, boolean supported) {
        this.id = id;
        this.supportsNumber = supportsNumber;
        this.title = title;
        this.content = content;
        this.commentsNumber = commentsNumber;
        this.author = author;
        this.updateTime = updateTime;
        this.empowered = empowered;
        this.supported = supported;
    }

    public int getId() {
        return id;
    }
    public void setId(int id){
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

    public int getCommentsNumber() {
        return commentsNumber;
    }

    public void setCommentsNumber(int commentsNumber) {
        this.commentsNumber = commentsNumber;
    }

    public int getSupportsNumber() {
        return supportsNumber;
    }

    public void setSupportsNumber(int supportsNumber) {
        this.supportsNumber = supportsNumber;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isEmpowered() {
        return empowered;
    }

    public void setEmpowered(boolean empowered) {
        this.empowered = empowered;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }


    public String getDate(){
        String s = null;
        Date date = this.getUpdateTime();
        String year = String.valueOf (date.getYear()+1900);
        String month = date.getMonth()<10?("0"+date.getMonth()):String.valueOf(date.getMonth());
        String day = date.getDay()<10?("0"+date.getDay()):String.valueOf(date.getDay());
        s = year+"/"+month+"/"+day;
        return s;
    }

    public String getTime(){
        String s = null;
        Date date = this.getUpdateTime();
        String hour = date.getHours()<10?("0"+date.getHours()):String.valueOf(date.getHours());
        String minutes = date.getMinutes()<10?("0"+date.getMinutes()):String.valueOf(date.getMinutes());
        s = hour+":"+minutes;
        return s;
    }
}
