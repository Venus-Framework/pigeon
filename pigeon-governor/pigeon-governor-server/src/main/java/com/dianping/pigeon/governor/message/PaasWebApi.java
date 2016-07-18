package com.dianping.pigeon.governor.message;

import com.dianping.pigeon.governor.util.HttpCallUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.*;


/**
 * Created by shihuashen on 16/7/18.
 */
public class PaasWebApi {
    private Logger logger = LogManager.getLogger();
    private final String paasWebApiConfig = "paasWebApi.properties";
    private static final String MAIL_KEY = "mail";
    private static final String WEIXIN_KEY = "weiXin";
    private static final String SMS_KEY = "sms";
    private String mailUrl;
    private String weiXinUrl;
    private String smsUrl;
    public PaasWebApi(){
        this.initProperties();
    }
    private void initProperties() {
        try {
            InputStream in = PaasWebApi.class.getClassLoader().getResourceAsStream(paasWebApiConfig);
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
    public boolean sendMail(String title,String content,List<String> mailAddresses){
        Map<String,Object> map = new HashMap<String,Object>();
        StringBuilder sb =  new StringBuilder();
        for(Iterator<String> iterator = mailAddresses.iterator();iterator.hasNext();){
            sb.append(iterator.next()+",");
        }
        String addresses = sb.deleteCharAt(sb.length()-1).toString();
        map.put("title",title);
        map.put("body",content);
        map.put("recipients",addresses);
        String response = HttpCallUtils.httpPost(this.mailUrl,map);
        System.out.println(response);
        return true;
    }


}
