package com.dianping.pigeon.governor.bean.scanServiceDesc;

/**
 * Created by shihuashen on 16/5/3.
 * 用于标示ServiceDesc数据库更新的结果.
 */
public enum UpdateResultState {
    STABLE, //更新后原始信息没有发生变化.
    CHANGED,//原始信息发生了改变.会添加新的方法或者移除旧有的方法.相应对应的文档会被删除.
    DBFAIL, //更新数据库连接原因失败.
    CREATED,//新增服务.
    NETFAIL,//网络原因获取JSON信息失败.
    REPLACED;//服务实现发生了改变,删除旧有实现并新增元数据.
}
