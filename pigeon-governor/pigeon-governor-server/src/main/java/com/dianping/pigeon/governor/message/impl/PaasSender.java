package com.dianping.pigeon.governor.message.impl;

import com.dianping.pigeon.governor.message.*;
import com.dianping.pigeon.governor.util.CommonUtils;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.dianping.pigeon.governor.util.HttpCallUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.mortbay.jetty.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.*;


/**
 * Created by shihuashen on 16/7/18.
 */
@Component
public class PaasSender implements EventSender{
    private Logger logger = LogManager.getLogger();
    private final String paasWebApiConfig = "paasWebApi.properties";
    private static final String MAIL_KEY = "mail";
    private static final String WEIXIN_KEY = "weiXin";
    private static final String SMS_KEY = "sms";
    private String mailUrl;
    private String weiXinUrl;
    private String smsUrl;
    public PaasSender(){
        this.initProperties();
    }
    private void initProperties() {
        try {
            InputStream in = PaasSender.class.getClassLoader().getResourceAsStream(paasWebApiConfig);
            if (in != null) {
                Properties prop = new Properties();
                try {
                    prop.load(in);
                    mailUrl = StringUtils.trim(prop.getProperty(MAIL_KEY));
                    weiXinUrl = StringUtils.trim(prop.getProperty(WEIXIN_KEY));
                    smsUrl = StringUtils.trim(prop.getProperty(SMS_KEY));
                } finally {
                    in.close();
                }
            } else {
                logger.info("[initProperties] Load {} file failed.", paasWebApiConfig);
                throw new RuntimeException();
            }
        } catch (Exception e) {
            logger.info("[initProperties] Load {} file failed.", paasWebApiConfig);
            throw new RuntimeException(e);
        }
    }
    private boolean sendMail(String title,String content,List<String> mailAddresses){
        Map<String,Object> map = new HashMap<String,Object>();
        StringBuilder sb =  new StringBuilder();
        for(Iterator<String> iterator = mailAddresses.iterator();iterator.hasNext();){
            sb.append(iterator.next()+",");
        }
        String addresses = sb.deleteCharAt(sb.length()-1).toString();
        map.put("title",title);
        map.put("body",content);
        map.put("recipients",addresses);
        String response = null;
        response = HttpCallUtils.httpPost(this.mailUrl,map);
        if(response!=null&&response.contains("true"))
            return true;
        return false;
    }

    private boolean sendWeiXin(String email, String title, String content){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("keyword", email);
            jsonObject.put("title", title);
            jsonObject.put("content", CommonUtils.escapeSpace(content));
        } catch (Exception e) {
            logger.error("[sendWeiXin] jsonObject put error.",e);
        }
        String response = null;
        response = HttpCallUtils.httpPost(this.weiXinUrl,jsonObject);
        if(response!=null&&response.contains("true"))
            return true;
        return false;
    }
    private SendResult sendMail(Event event,List<String> addresses){
        SendResult result = new SendResult();
        boolean sendState = sendMail(event.getTitle(),event.getSummary(),addresses);
        for(Iterator<String> iterator = addresses.iterator();
                iterator.hasNext();){
            String address = iterator.next();
            result.add(address,SenderType.Mail,sendState);
        }
        return result;
    }
    private SendResult sendWeiXin(Event event,List<String> addresses){
        SendResult result = new SendResult();
        for(Iterator<String> iterator = addresses.iterator();
                iterator.hasNext();){
            String address = iterator.next();
            result.add(address,SenderType.WeiXin,
                    sendWeiXin(address,event.getTitle(),event.getSummary()));
        }
        return result;
    }


    public boolean sendSMS(String mobile, String title, String body) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("mobile", mobile);
        map.put("body", body);
        String response = null;
        response = HttpCallUtils.httpPost(this.smsUrl, map);
        if (response != null && response.contains("200"))
            return true;
        return false;
    }


    public SendResult sendSMS(Event event,List<String> addresses){
        SendResult result = new SendResult();
        for(Iterator<String> iterator = addresses.iterator();iterator.hasNext();){
            String address = iterator.next();
            result.add(address,SenderType.SMS,sendSMS(address,event.getTitle(),event.getSummary()));
        }
        return result;
    }

    @Override
    public SendResult sendMessage(Event event, EventReceiver receiver) {
        Map<SenderType,List<String>> destinations = receiver.getDestinations();
        SendResult result = new SendResult();
        for(Iterator<SenderType> iterator = destinations.keySet().iterator();
                iterator.hasNext();){
            SenderType senderType = iterator.next();
            switch(senderType) {
                //TODO if senderType is unsupported type, we need to log.
                case WeiXin:
                    result.add(sendWeiXin(event,destinations.get(senderType)));
                    break;
                case Mail:
                    result.add(sendMail(event,destinations.get(senderType)));
                    break;
                case SMS:
                    result.add(sendSMS(event,destinations.get(senderType)));
                    break;
            }
        }
        return result;
    }
}
