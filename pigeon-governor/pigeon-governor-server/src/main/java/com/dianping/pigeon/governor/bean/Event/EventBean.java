package com.dianping.pigeon.governor.bean.Event;

/**
 * Created by shihuashen on 16/8/1.
 *
 *
 * Front end event model. Facing web UI.
 */
public class EventBean {
    private int eventId;
    private String type;
    private String level;
    private String title;
    private String projectName;
    private String relatedProjectName;
    private String summary;
    private String sendResult;
    private String time;


    public EventBean(){
        this.eventId = -1;
        this.type = "-";
        this.level = "-1";
        this.title = "-";
        this.projectName = "-";
        this.relatedProjectName = "-";
        this.summary = "-";
        this.sendResult = "-";
        this.time = "-";
    }
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRelatedProjectName() {
        return relatedProjectName;
    }

    public void setRelatedProjectName(String relatedProjectName) {
        this.relatedProjectName = relatedProjectName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSendResult() {
        return sendResult;
    }

    public void setSendResult(String sendResult) {
        this.sendResult = sendResult;
    }
}
