package com.dianping.pigeon.governor.message;

/**
 * Created by shihuashen on 16/7/20.
 */
//TODO need Refactor
public enum SenderType {
    WeiXin("WeiXin"), SMS("SMS"), Mail("Mail"), DaXiang("DaXiang");
    private String name;
    private SenderType(String name) {
        this.name = name;
    }
    public static SenderType getSenderType(String name){
        if(name.equals("WeiXin"))
            return WeiXin;
        if(name.equals("SMS"))
            return SMS;
        if(name.equals("Mail"))
            return Mail;
        if(name.equals("DaXiang"))
            return DaXiang;
        return null;
    }

}
