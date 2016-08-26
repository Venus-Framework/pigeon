package com.dianping.pigeon.governor.bean.scanServiceDesc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.ConnectionPoolTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dianping.pigeon.governor.bean.serviceDesc.MethodDescBean;
import com.dianping.pigeon.governor.bean.serviceDesc.ParamDescBean;
import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescBean;
import com.dianping.pigeon.governor.model.ServiceHosts;
import com.dianping.pigeon.governor.service.ServiceDescService;
import com.dianping.pigeon.governor.util.AddressUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Created by shihuashen on 16/4/29.
 * Using ServiceHosts to fecth the Json form info of a service. Then convert the json into ServiceDescBean.
 * Using ServiceDescService to update the service description.
 */
public class UpdateServiceDescTask implements Runnable{
    private HttpClient httpClient;
    private ServiceDescService serviceDescService;
    private Logger logger = LogManager.getLogger(UpdateServiceDescTask.class.getName());
    private Gson gson;
    private ServiceHosts serviceHosts;
    //TODO refine the configure
    private ConcurrentHashMap<String,UpdateResultState> statics;
    private ConcurrentHashMap<String,JsonHostInfo> cachedInfo;
    public UpdateServiceDescTask(HttpClient httpClient,
                                 ServiceDescService service,
                                 ServiceHosts hosts,
                                 Gson gson,
                                 ConcurrentHashMap<String,UpdateResultState> statics,
                                 ConcurrentHashMap<String,JsonHostInfo> cachedInfo){
        this.httpClient = httpClient;
        this.serviceDescService = service;
        this.serviceHosts = hosts;
        this.gson = gson;
        this.statics = statics;
        this.cachedInfo = cachedInfo;
    }
    @Override
    public void run() {
        JsonService jsonService = null;
        try{
            jsonService = fetchJsonPerService(this.httpClient,this.serviceHosts);
        }catch (Throwable e){
            logger.warn(e);
            statics.put(serviceHosts.getServiceName()+":"+serviceHosts.getGroup(),UpdateResultState.NETFAIL);
            return;
        }
        if(jsonService==null){
            logger.warn("服务" + serviceHosts.getServiceName()+"泳道:"+serviceHosts.getGroup()+"的JSON文档获取失败");
            statics.put(serviceHosts.getServiceName()+":"+serviceHosts.getGroup(),UpdateResultState.NETFAIL);
        }else{
            try{
                logger.debug("服务" + serviceHosts.getServiceName() +"泳道:"+serviceHosts.getGroup()+ "的JSON文档获取成功,开始更新数据库");
                ServiceDescBean serviceDescBean = convertJsonToBean(serviceHosts.getServiceId(), jsonService);
                UpdateResultState state = serviceDescService.updateServiceDescBean(serviceDescBean);
                logger.info("服务"+serviceDescBean.getServiceName()+"泳道:"+serviceHosts.getGroup()+"的更新完成");
                statics.put(serviceHosts.getServiceName()+":"+serviceHosts.getGroup(),state);
            }catch(Throwable e){
                logger.info(e);
                statics.put(serviceHosts.getServiceName()+":"+serviceHosts.getGroup(),UpdateResultState.DBFAIL);
            }
        }
    }




    public JsonService fetchJsonPerService(HttpClient httpClient, ServiceHosts serviceHosts){
        if(serviceHosts.getHosts()==null){
            logger.debug("服务ID:"+serviceHosts.getGroup()+"服务名:"+serviceHosts.getServiceName()+"的主机名列表为空,停止获取JSON文档");
            return null;
        }
        List<String> hosts = splitHosts(serviceHosts.getHosts());
        Iterator<String> iterator = hosts.iterator();
        while(iterator.hasNext()){
            String host = iterator.next();
            JsonHostInfo jsonHostInfo = localCacheFetch(httpClient,host);
            if(jsonHostInfo==null){
                logger.debug("主机"+host+"上的JSON提取失败");
            }else{
                String group = jsonHostInfo.getGroup();
                List<JsonService> list = jsonHostInfo.getServices();
                for (JsonService service: list) {
                    if(service.getName().equals(serviceHosts.getServiceName())&&serviceHosts.getGroup().equals(group)){
                        logger.debug("成功获取服务id:"+serviceHosts.getServiceId()+" 服务名:"+service.getName()+"的JSON文档");
                        return service;
                    }
                }
            }
        }
        logger.debug("服务"+serviceHosts.getServiceName()+"于所有主机上的JSON获取全部失败");
        return null;
    }

    private List<String> splitHosts(String hosts){
        List<String> validHosts = new ArrayList<String>();
        String[] tmpHosts = hosts.split(",");
        for(int i = 0;i < tmpHosts.length; i++){
            AddressUtils.Address address =  AddressUtils.toAddress(tmpHosts[i]);
            if(address.isValid())
                validHosts.add(address.getIp());
        }
        return validHosts;
    }
    private JsonHostInfo localCacheFetch(HttpClient httpClient,String host){
        if(cachedInfo.containsKey(host)){
            logger.debug("已经访问过主机"+host+",JSON数据由本地缓存取出.");
            return cachedInfo.get(host);
        }else{
            logger.debug("新添加主机"+host+"JSON数据缓存.");
            JsonHostInfo jsonHostInfo = httpGet(httpClient,host);
            cachedInfo.putIfAbsent(host,jsonHostInfo);
            return jsonHostInfo;
        }

    }



    public JsonHostInfo httpGet(HttpClient httpClient,String host){
        String address = "http://"+host+":4080/services.json";
        HttpMethod method = new GetMethod(address);
        String responseBody = null;
        try{
            int statusCode = httpClient.executeMethod(method);
            logger.debug("Http GET method状态返回码为:"+statusCode);
            InputStream resStream = method.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(resStream));
            StringBuffer resBuffer = new StringBuffer();
            String resTemp = "";
            while((resTemp = br.readLine()) != null){
                resBuffer.append(resTemp);
            }
            responseBody = resBuffer.toString();
        }catch(ConnectionPoolTimeoutException e){
            logger.warn("从连接池中获取可用连接失败,请尝试调整ConnectionManager参数设置");
            logger.warn(e.toString());
        }catch(ConnectTimeoutException e){
            logger.warn("服务器拒绝请求");
            logger.warn(e.toString());
        }catch(SocketTimeoutException e){
            logger.warn("连接超时");
            logger.warn(e.toString());
        }catch (HttpException e) {
            logger.warn(e);
        }catch (Throwable e) {
            logger.warn(e);
        }finally {
            method.releaseConnection();
        }
        if(responseBody==null){
            logger.debug("主机"+ host+"上的JSON获取失败");
            return null;
        }
        else{
            String result = responseBody.replaceAll("ip:port","ipPort");
            JsonHostInfo services = null;
            try{
                services = gson.fromJson(result,JsonHostInfo.class);
            }catch (JsonSyntaxException e){
                logger.warn("Gson转换失败");
                logger.warn("原始数据源为"+result);
                logger.warn(e);
            }

            return services;
        }
    }


    public ServiceDescBean convertJsonToBean(int service_id, JsonService service){
        ServiceDescBean serviceDescBean = new ServiceDescBean();
        serviceDescBean.setServiceId(service_id);
        serviceDescBean.setServiceName(service.getName());
        serviceDescBean.setServiceImpl(service.getType());
        List<JsonMethod> methods = service.getMethods();
        if(methods!=null){
            Iterator<JsonMethod> methodIterator = methods.iterator();
            List<MethodDescBean> methodDescBeans=  new ArrayList<MethodDescBean>();
            while(methodIterator.hasNext()){
                MethodDescBean methodDescBean = new MethodDescBean();
                JsonMethod jsonMethod = methodIterator.next();

                methodDescBean.setServiceId(service_id);
                methodDescBean.setMethodReturnType(jsonMethod.getReturnType());
                methodDescBean.setMethodName(jsonMethod.getName());

                List<String> params = jsonMethod.getParameterTypes();
                StringBuilder paramsString = new StringBuilder();
                if(params!=null&&params.size()!=0){
                    paramsString.append(params.get(0));
                    for(int i =1;i<params.size();i++)
                        paramsString.append(","+params.get(i));
                }
                methodDescBean.setMethodFullname(methodDescBean.getMethodReturnType()+" "+
                        methodDescBean.getMethodName()+"("+paramsString+")");
                if(params!=null){
                    List<ParamDescBean> paramDescs = new ArrayList<ParamDescBean>();
                    Iterator<String> paramIterator = params.iterator();
                    while(paramIterator.hasNext()){
                        String param = paramIterator.next();
                        ParamDescBean paramDescBean = new ParamDescBean();
                        paramDescBean.setParamName(param);
                        paramDescs.add(paramDescBean);
                    }
                    methodDescBean.setParamDescBeanArrayList(paramDescs);
                }
                methodDescBeans.add(methodDescBean);
            }
            serviceDescBean.setMethodDescBeanList(methodDescBeans);
        }
        return serviceDescBean;
    }
}