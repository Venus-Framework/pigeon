package com.dianping.pigeon.governor.dao;

import com.dianping.pigeon.governor.bean.serviceDesc.MethodMeta;
import com.dianping.pigeon.governor.bean.serviceDesc.ServiceMeta;
import com.dianping.pigeon.governor.model.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by shihuashen on 16/4/22.
 */
public interface CombinedServiceDescMapper {
    List<MethodDescWithBLOBs> selectMethodsByServiceId(Integer service_id);
    List<ParamDesc> selectParamByMethodId(Integer method_id);
    List<ExceptionDesc> selectExceptionByMethodId(Integer method_id);
    void updateServiceDescById(Integer service_id,String desc);
    void updateMethodDescById(Integer methodId, String value);
    void updateParamDescById(Integer paramId, String value);
    void updateExceptionDescById(Integer exceptionId, String value);
    Project selectProjectByServiceId(Integer project_id);
    List<ServiceHosts> selectServiceHosts();
    int insertMethodDescSelective(MethodDescWithBLOBs methodDescWithBLOBs);
    List<Integer> selectMethodId(Integer service_id);
    int deleteParamDesc(Integer method_id);
    int deleteExceptionDesc(Integer method_id);
    List<ServiceDesc> selectAllServiceDesc();
    List<MethodDescWithBLOBs> selectAllMethodDesc();
    List<ParamDesc> selectAllParamDesc();
    List<ExceptionDesc> selectAllExceptionDesc();


    //查询服务元数据,构建ServiceMeta.
    List<ServiceMeta> selectServiceMetas(String query);
    //查询方法元数据,构建MethodMeta.
    List<MethodMeta> selectMethodMetas(String query);


}
