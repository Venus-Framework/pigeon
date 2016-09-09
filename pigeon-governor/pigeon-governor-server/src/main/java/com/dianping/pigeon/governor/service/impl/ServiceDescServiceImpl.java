package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.bean.scanServiceDesc.UpdateResultState;
import com.dianping.pigeon.governor.bean.serviceDesc.ExceptionDescBean;
import com.dianping.pigeon.governor.bean.serviceDesc.MethodDescBean;
import com.dianping.pigeon.governor.bean.serviceDesc.ParamDescBean;
import com.dianping.pigeon.governor.bean.serviceDesc.ServiceDescBean;
import com.dianping.pigeon.governor.dao.*;
import com.dianping.pigeon.governor.model.*;
import com.dianping.pigeon.governor.service.ServiceDescService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by shihuashen on 16/4/22.
 */
@Service
//TODO maybe we need to use threadpool to accelerate the select.
public class ServiceDescServiceImpl implements ServiceDescService{
    private Logger logger = LogManager.getLogger(ServiceDescServiceImpl.class.getName());
    @Autowired
    private ServiceDescMapper serviceDescMapper;
    @Autowired
    private MethodDescMapper methodDescMapper;
    @Autowired
    private CombinedServiceDescMapper combinedServiceDescMapper;
    @Autowired
    private ServiceMapper serviceMapper;
    @Autowired
    private ParamDescMapper paramDescMapper;
    @Override
    public ServiceDescBean getServiceDescBeanById(Integer serviceId) {
        ServiceDesc serviceDesc = serviceDescMapper.selectByPrimaryKey(serviceId);
        if(serviceDesc==null)
            return null;
        return constructServiceDescBeanWithModel(serviceDesc);
    }

    @Override
    public void updateServiceDescById(Integer serviceId, String desc) {
        combinedServiceDescMapper.updateServiceDescById(serviceId,desc);
    }

    @Override
    public void updateMethodDescById(Integer methodId, String value) {
        combinedServiceDescMapper.updateMethodDescById(methodId,value);
    }

    @Override
    public void updateParamDescById(Integer paramId, String value) {
        combinedServiceDescMapper.updateParamDescById(paramId,value);
    }

    @Override
    public void updateExceptionDescById(Integer exceptionId, String value) {
        combinedServiceDescMapper.updateExceptionDescById(exceptionId,value);
    }

    @Override
    public Map<String, Object> getServiceMetaInfoById(Integer serviceId) {
        Map<String,Object> map = new HashMap<String,Object>();
        com.dianping.pigeon.governor.model.Service service = serviceMapper.selectByPrimaryKey(serviceId);
        Project project = combinedServiceDescMapper.selectProjectByServiceId(service.getProjectid());
        map.put("serviceGroup",service.getGroup());
        List<String> hosts = Arrays.asList(service.getHosts().split(","));
        map.put("serviceHosts",hosts);
        map.put("projectName",project.getName());
        map.put("projectOwner",project.getOwner());
        map.put("projectBu",project.getBu());
        return map;
    }
    /**TODO: temporarily used for period scan the DB and fetch throw http json port to update or insert service metaInfo.
     * TODO: For now, we do not involve the exception desc table since the json doesn't contain the exception info.
    **/
    @Override
    public UpdateResultState updateServiceDescBean(ServiceDescBean serviceDescBean){
        try{
            int service_id = serviceDescBean.getServiceId();
            ServiceDescBean dbBean = getServiceDescBeanById(service_id);
            if(dbBean==null){
                logger.debug("Lack the meta info of service "+serviceDescBean.getServiceName());
                addServiceDescMetaInfo(serviceDescBean);
                return UpdateResultState.CREATED;
            }
            else{
                logger.debug("Contains the meta info.");
                if(!(dbBean.getServiceImpl().contains("Proxy")||serviceDescBean.getServiceImpl().contains("Proxy"))&&
                        !dbBean.getServiceImpl().equals(serviceDescBean.getServiceImpl())){
                    removeServiceDesc(service_id);
                    addServiceDescMetaInfo(serviceDescBean);
                    return UpdateResultState.REPLACED;
                }else{
                    boolean changedFlag = false;
                    List<MethodDescBean> newMethods = serviceDescBean.getMethodDescBeanList();
                    List<MethodDescBean> oldMethods = dbBean.getMethodDescBeanList();
                    HashMap<String,MethodDescBean> newMethodsMap = new HashMap<String,MethodDescBean>();
                    for(Iterator<MethodDescBean> iterator = newMethods.iterator();iterator.hasNext();){
                        MethodDescBean method = iterator.next();
                        newMethodsMap.put(method.getMethodFullname(),method);
                    }
                    for(Iterator<MethodDescBean> iterator  = oldMethods.iterator();iterator.hasNext();){
                        MethodDescBean method = iterator.next();
                        String methodFullname = method.getMethodFullname();
                        if(newMethodsMap.containsKey(methodFullname)){
                            newMethodsMap.remove(methodFullname);
                        }else{
                            methodDescMapper.deleteByPrimaryKey(method.getMethodId());
                            combinedServiceDescMapper.deleteParamDesc(method.getMethodId());
                            combinedServiceDescMapper.deleteExceptionDesc(method.getMethodId());
                            changedFlag = true;
                        }
                    }
                    for(Iterator<MethodDescBean> iterator  = newMethodsMap.values().iterator();iterator.hasNext();){
                        changedFlag = true;
                        MethodDescBean methodDescBean = iterator.next();
                        addMethodMetaInfo(methodDescBean);
                    }
                    if(changedFlag)
                        return UpdateResultState.CHANGED;
                    else
                        return UpdateResultState.STABLE;
                }
            }
        }catch(DataAccessException e){
            logger.error(e);
            return UpdateResultState.DBFAIL;
        }catch(Exception e){
            logger.error(e);
            return UpdateResultState.DBFAIL;
        }
    }
    @Override
    public void removeServiceDesc(Integer serviceId) {
        logger.debug("Remove all the desc involved with service id "+serviceId);
        serviceDescMapper.deleteByPrimaryKey(serviceId);
        List<Integer> methodIds = combinedServiceDescMapper.selectMethodId(serviceId);
        for (Integer methodId: methodIds
             ) {
            methodDescMapper.deleteByPrimaryKey(methodId);
            combinedServiceDescMapper.deleteParamDesc(methodId);
            combinedServiceDescMapper.deleteExceptionDesc(methodId);
        }
    }
    private void addServiceDescMetaInfo(ServiceDescBean serviceDescBean){
        logger.debug("Add meta info of service "+serviceDescBean.getServiceName());
        serviceDescMapper.insert(constructServiceDescModelWithBean(serviceDescBean));
        List<MethodDescBean> methodDescBeans = serviceDescBean.getMethodDescBeanList();
        Iterator<MethodDescBean> methodsIterator = methodDescBeans.iterator();
        while(methodsIterator.hasNext()){
            MethodDescBean methodDescBean = methodsIterator.next();
            addMethodMetaInfo(methodDescBean);
        }
    }
    //TODO: Temporarily add the meta Info of a method. Since the bean generated from json doesn't contain exception, so this isn't a
    //TODO: common way to add the meta info of a method to DB.
    private void addMethodMetaInfo(MethodDescBean bean){
        MethodDescWithBLOBs methodModel = constructMethodDescModelWithBean(bean);
        combinedServiceDescMapper.insertMethodDescSelective(methodModel);
        int methodId = methodModel.getMethodId();
        List<ParamDescBean> params = bean.getParamDescBeanArrayList();
        Iterator<ParamDescBean> paramsIterator = params.iterator();
        while(paramsIterator.hasNext()){
            ParamDescBean param = paramsIterator.next();
            param.setMethodId(methodId);
            paramDescMapper.insertSelective(constructParamDescModelWithBean(param));
        }
    }

    private ParamDesc constructParamDescModelWithBean(ParamDescBean param) {
        ParamDesc model = new ParamDesc();
        model.setMethodId(param.getMethodId());
        model.setParamName(param.getParamName());
        model.setParamDesc(param.getParamDesc());
        model.setUpdatetime(param.getUpdatetime());
        return model;
    }

    private MethodDescWithBLOBs constructMethodDescModelWithBean(MethodDescBean methodDescBean) {
        MethodDescWithBLOBs model = new MethodDescWithBLOBs();
        model.setServiceId(methodDescBean.getServiceId());
        model.setMethodName(methodDescBean.getMethodName());
        model.setMethodReturnType(methodDescBean.getMethodReturnType());
        model.setMethodFullname(methodDescBean.getMethodFullname());
        model.setMethodDesc(methodDescBean.getMethodDesc());
        model.setMethodReturnDesc(methodDescBean.getMethodReturnDesc());
        model.setUpdatetime(methodDescBean.getUpdatetime());
        return model;
    }


    private List<MethodDescBean> getMethodDescBeansByServiceId(Integer serviceId){
        List<MethodDescWithBLOBs>  methodDescWithBLOBses = combinedServiceDescMapper.selectMethodsByServiceId(serviceId);
        List<MethodDescBean> methodDescBeans = new ArrayList<MethodDescBean>();
        Iterator<MethodDescWithBLOBs> iterator = methodDescWithBLOBses.iterator();
        while(iterator.hasNext())
            methodDescBeans.add(constructMethodDescBeanWithModel(iterator.next()));
        return methodDescBeans;
    }

    private List<ParamDescBean> getParamDescBeansByMethodId(Integer methodId){
        List<ParamDesc> paramDescs = combinedServiceDescMapper.selectParamByMethodId(methodId);
        List<ParamDescBean> paramDescBeans  = new ArrayList<ParamDescBean>();
        Iterator<ParamDesc> iterator = paramDescs.iterator();
        while(iterator.hasNext())
            paramDescBeans.add(constructParamDescBeanWithModel(iterator.next()));
        return paramDescBeans;
    }

    private List<ExceptionDescBean> getExceptionDescBeansByMethodId(Integer methodId){
        List<ExceptionDesc> exceptionDescs = combinedServiceDescMapper.selectExceptionByMethodId(methodId);
        List<ExceptionDescBean> exceptionDescBeans = new ArrayList<ExceptionDescBean>();
        Iterator<ExceptionDesc> iterator = exceptionDescs.iterator();
        while(iterator.hasNext())
            exceptionDescBeans.add(constructExceptionDescBeanWithModel(iterator.next()));
        return exceptionDescBeans;
    }
    private ParamDescBean constructParamDescBeanWithModel(ParamDesc model){
        ParamDescBean paramDescBean = new ParamDescBean();
        paramDescBean.setParamId(model.getParamId());
        paramDescBean.setMethodId(model.getMethodId());
        paramDescBean.setParamName(model.getParamName());
        paramDescBean.setParamDesc(model.getParamDesc());
        paramDescBean.setUpdatetime(model.getUpdatetime());
        return paramDescBean;
    }
    private  ExceptionDescBean constructExceptionDescBeanWithModel(ExceptionDesc model){
        ExceptionDescBean exceptionDescBean = new ExceptionDescBean();
        exceptionDescBean.setExceptionId(model.getExceptionId());
        exceptionDescBean.setMethodId(model.getMethodId());
        exceptionDescBean.setExceptionName(model.getExceptionName());
        exceptionDescBean.setExceptionDesc(model.getExceptionDesc());
        exceptionDescBean.setUpdatetime(model.getUpdatetime());
        return exceptionDescBean;
    }

    private MethodDescBean constructMethodDescBeanWithModel(MethodDescWithBLOBs model){
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
        methodDescBean.setParamDescBeanArrayList(getParamDescBeansByMethodId(methodId));
        methodDescBean.setExceptionDescBeanArrayList(getExceptionDescBeansByMethodId(methodId));
        return methodDescBean;
    }

    private ServiceDescBean constructServiceDescBeanWithModel(ServiceDesc model){
        ServiceDescBean serviceDescBean = new ServiceDescBean();
        serviceDescBean.setServiceId(model.getServiceId());
        serviceDescBean.setServiceName(model.getServiceName());
        serviceDescBean.setServiceImpl(model.getServiceImpl());
        serviceDescBean.setServiceDesc(model.getServiceDesc());
        serviceDescBean.setUpdatetime(model.getUpdatetime());
        serviceDescBean.setMethodDescBeanList(getMethodDescBeansByServiceId(model.getServiceId()));
        return serviceDescBean;
    }

    private ServiceDesc constructServiceDescModelWithBean(ServiceDescBean bean){
        ServiceDesc model = new ServiceDesc();
        model.setServiceId(bean.getServiceId());
        model.setServiceName(bean.getServiceName());
        model.setServiceImpl(bean.getServiceImpl());
        model.setServiceDesc(bean.getServiceDesc());
        model.setUpdatetime(bean.getUpdatetime());
        return model;
    }
}
