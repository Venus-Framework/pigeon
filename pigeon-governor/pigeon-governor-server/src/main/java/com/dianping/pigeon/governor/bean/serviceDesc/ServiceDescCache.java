package com.dianping.pigeon.governor.bean.serviceDesc;

import com.dianping.pigeon.governor.dao.*;
import com.dianping.pigeon.governor.model.*;
import com.dianping.pigeon.governor.service.ServiceDescService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shihuashen on 16/5/5.
 */
public class ServiceDescCache {
    @Autowired
    private CombinedServiceDescMapper combinedServiceDescMapper;
    public ServiceDescCache(){

    }

    public LinkedList<ServiceDescBean> constructCache(){
        long start = System.currentTimeMillis();
        List<ServiceDesc> serviceDescs = combinedServiceDescMapper.selectAllServiceDesc();
        List<MethodDescWithBLOBs> methodDescs = combinedServiceDescMapper.selectAllMethodDesc();
        List<ParamDesc> paramDescs = combinedServiceDescMapper.selectAllParamDesc();
        List<ExceptionDesc> exceptionDescs = combinedServiceDescMapper.selectAllExceptionDesc();
        HashMap<Integer,List<ParamDescBean>> methodParamMap = new HashMap<Integer,List<ParamDescBean>>();
        HashMap<Integer,List<ExceptionDescBean>> methodExceptionMap = new HashMap<Integer, List<ExceptionDescBean>>();
        HashMap<Integer,List<MethodDescBean>> methodMap = new HashMap<Integer, List<MethodDescBean>>();
        LinkedList<ServiceDescBean> serviceDescBeanList =  new LinkedList<ServiceDescBean>();
        for(ParamDesc paramDesc : paramDescs){
            int methodId = paramDesc.getMethodId();
            if(!methodParamMap.containsKey(methodId))
                methodParamMap.put(methodId,new LinkedList<ParamDescBean>());
            methodParamMap.get(methodId).add(convertParamDesc(paramDesc));
        }

        for(ExceptionDesc exceptionDesc : exceptionDescs){
            int methodId = exceptionDesc.getMethodId();
            if(!methodExceptionMap.containsKey(methodId))
                methodExceptionMap.put(methodId,new LinkedList<ExceptionDescBean>());
            methodExceptionMap.get(methodId).add(convertExceptionDesc(exceptionDesc));
        }

        for(MethodDescWithBLOBs methodDescWithBLOBs : methodDescs){
            int serviceId = methodDescWithBLOBs.getServiceId();
            if(!methodMap.containsKey(serviceId))
                methodMap.put(serviceId,new LinkedList<MethodDescBean>());
            methodMap.get(serviceId).add(convertMethodDesc(methodDescWithBLOBs, methodParamMap,methodExceptionMap));
        }
        for(ServiceDesc serviceDesc : serviceDescs){
            serviceDescBeanList.add(convertServiceDesc(serviceDesc,methodMap));
        }
        System.out.println((System.currentTimeMillis()-start)+"ms");
        return serviceDescBeanList;
    }




    public List<String> search(String key){
        List<ServiceDescBean> list = constructCache();
        Iterator<ServiceDescBean> iterator = list.iterator();
        List<String> strings = new LinkedList<String>();
        Gson gson = new Gson();
        while(iterator.hasNext()){
            ServiceDescBean bean = iterator.next();
            strings.add(gson.toJson(bean));
        }
        LinkedList<String> ans = new LinkedList<String>();
        long start = System.currentTimeMillis();
        for ( String s: strings) {
            if(s.contains(key))
                ans.add(s);
        }
        System.out.println(System.currentTimeMillis()-start);
        return ans;
    }

    private ParamDescBean convertParamDesc(ParamDesc model){
        ParamDescBean paramDescBean = new ParamDescBean();
        paramDescBean.setParamId(model.getParamId());
        paramDescBean.setMethodId(model.getMethodId());
        paramDescBean.setParamName(model.getParamName());
        paramDescBean.setParamDesc(model.getParamDesc());
        paramDescBean.setUpdatetime(model.getUpdatetime());
        return paramDescBean;
    }
    private  ExceptionDescBean convertExceptionDesc(ExceptionDesc model){
        ExceptionDescBean exceptionDescBean = new ExceptionDescBean();
        exceptionDescBean.setExceptionId(model.getExceptionId());
        exceptionDescBean.setMethodId(model.getMethodId());
        exceptionDescBean.setExceptionName(model.getExceptionName());
        exceptionDescBean.setExceptionDesc(model.getExceptionDesc());
        exceptionDescBean.setUpdatetime(model.getUpdatetime());
        return exceptionDescBean;
    }

    private MethodDescBean convertMethodDesc(MethodDescWithBLOBs model,
                                             HashMap<Integer,List<ParamDescBean>> params,
                                             HashMap<Integer,List<ExceptionDescBean>> exceptions){
        MethodDescBean methodDescBean = new MethodDescBean();
        Integer methodId = model.getMethodId();
        methodDescBean.setMethodId(model.getMethodId());
        methodDescBean.setServiceId(model.getServiceId());
        methodDescBean.setMethodName(model.getMethodName());
        methodDescBean.setMethodFullname(model.getMethodFullname());
        methodDescBean.setMethodReturnType(model.getMethodReturnType());
        methodDescBean.setMethodDesc(model.getMethodDesc());
        methodDescBean.setMethodReturnDesc(model.getMethodReturnDesc());
        methodDescBean.setUpdatetime(model.getUpdatetime());
        methodDescBean.setParamDescBeanArrayList(params.get(methodId));
        methodDescBean.setExceptionDescBeanArrayList(exceptions.get(methodId));
        return methodDescBean;
    }

    private ServiceDescBean convertServiceDesc(ServiceDesc model,
                                               HashMap<Integer,List<MethodDescBean>> methodMap){
        ServiceDescBean serviceDescBean = new ServiceDescBean();
        serviceDescBean.setServiceId(model.getServiceId());
        serviceDescBean.setServiceName(model.getServiceName());
        serviceDescBean.setServiceImpl(model.getServiceImpl());
        serviceDescBean.setServiceDesc(model.getServiceDesc());
        serviceDescBean.setUpdatetime(model.getUpdatetime());
        serviceDescBean.setMethodDescBeanList(methodMap.get(model.getServiceId()));
        return serviceDescBean;
    }
}
