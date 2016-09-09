package com.dianping.pigeon.governor.util;

import com.google.gson.JsonObject;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.json.JSONObject;
import org.mortbay.jetty.*;
import org.unidal.helper.Objects;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;

/**
 * Created by shihuashen on 16/5/24.
 */
public class HttpCallUtils {
    private static Logger logger = LogManager.getLogger();
    private static HttpClient httpClient;
    private static int maxConnectionsPerHost = 200;
    private static int maxTotalConnections = 200;
    private static int connectionTimeout  =500;
    private static int soTimeout = 3000;
    static{
        MultiThreadedHttpConnectionManager cm = new MultiThreadedHttpConnectionManager();
        cm.getParams().setDefaultMaxConnectionsPerHost(maxConnectionsPerHost);
        cm.getParams().setMaxTotalConnections(maxTotalConnections);
        cm.getParams().setConnectionTimeout(connectionTimeout);
        cm.getParams().setSoTimeout(soTimeout);
        httpClient = new HttpClient(cm);
    }
    public static String httpGet(String url){
        HttpMethod method = new GetMethod(url);
        String responseBody = null;
        try{
            int statusCode = httpClient.executeMethod(method);
            logger.debug("Http GET 返回码为:"+statusCode);
            InputStream resStream = method.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(resStream));
            StringBuffer resBuffer = new StringBuffer();
            String resTemp = "";
            while((resTemp = br.readLine()) != null){
                resBuffer.append(resTemp);
            }
            responseBody = resBuffer.toString();
        }catch (HttpException e) {
            logger.warn(e);
        }catch (Throwable e) {
            logger.warn(e);
        }finally {
            method.releaseConnection();
        }
        return responseBody;
    }

    public static Document httpGetCatReport(String url){
        HttpMethod method = new GetMethod(url);
        Document doc = null;
        try{
            int statusCode = httpClient.executeMethod(method);
            logger.debug("Http GET 返回码为:"+statusCode);
            InputStream resStream = method.getResponseBodyAsStream();
            SAXReader saxReader = new SAXReader();
            doc = saxReader.read(resStream); //
        }catch (DocumentException e){
            //TODO fix when commit code
            e.printStackTrace();
            logger.warn(e);
        }catch(HttpException e){
            logger.warn(e);
        }catch(Throwable t){
            logger.warn(t);
        }finally{
            method.releaseConnection();
        }
        return doc;
    }


    public static String httpPost(String url, Map<String,Object> propsMap){
        String response = null;
        PostMethod postMethod = new PostMethod(url);
        postMethod.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");
        postMethod.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        Set<String> keySet=propsMap.keySet();
        NameValuePair[] postData = new NameValuePair[keySet.size()];
        int index=0;
        for(String key:keySet){
            postData[index++]=new NameValuePair(key,propsMap.get(key).toString());
        }
        postMethod.addParameters(postData);
        try{
            int statusCode =  httpClient.executeMethod(postMethod);//发送请求
            logger.debug("Http POST 返回码为: "+statusCode);
            InputStream resStream = postMethod.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(resStream));
            StringBuffer resBuffer = new StringBuffer();
            String resTemp = "";
            while((resTemp = br.readLine()) != null){
                resBuffer.append(resTemp);
            }
            response = resBuffer.toString();
        }catch (IOException e){
            logger.error(e);
            e.printStackTrace();
        }finally{
            postMethod.releaseConnection();//关闭连接
        }
        return response;
    }


    public static String httpPost(String url, String content){
        String response = null;
        PostMethod postMethod = new PostMethod(url);
        postMethod.addRequestHeader("Content-Type",
                "application/json");
        postMethod.setRequestBody(content);
        try{
            httpClient.executeMethod(postMethod);
            InputStream in = postMethod.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuffer sb = new StringBuffer();
            String ans = "";
            while((ans=br.readLine())!=null)
                sb.append(ans);
            response = ans.toString();
        }catch(IOException e){
            logger.error(e);
            e.printStackTrace();
        }
        return  response;
    }


    public static String httpPost(String url, JSONObject jsonObject) {
        String response = null;
        PostMethod postMethod = new PostMethod(url);
        postMethod.addRequestHeader("Content-Type","application/json;charset=utf-8");
        postMethod.addRequestHeader("Accept","application/json");
        try {
            RequestEntity entity = new StringRequestEntity(jsonObject.toString()/**,"application/json","utf-8"**/);
            postMethod.setRequestEntity(entity);
            httpClient.executeMethod(postMethod);
            InputStream in = postMethod.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String ans = "";
            while((ans=br.readLine())!=null)
                sb.append(ans);
            response = sb.toString();
        } catch (UnsupportedEncodingException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (HttpException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        }
        return response;
    }
}
