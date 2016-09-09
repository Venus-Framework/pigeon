package com.dianping.pigeon.governor.service.impl;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.bean.op.FilterBean;
import com.dianping.pigeon.governor.bean.op.OpLogBean;
import com.dianping.pigeon.governor.dao.CustomOpLogModelMapper;
import com.dianping.pigeon.governor.model.OpLog;
import com.dianping.pigeon.governor.service.CacheService;
import com.dianping.pigeon.governor.service.OpLogManageService;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.google.common.base.Stopwatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by shihuashen on 16/8/9.
 */
@Service
public class OpLogManageServiceImpl implements OpLogManageService{
    @Autowired
    private CustomOpLogModelMapper customOpLogModelMapper;
    @Autowired
    private CacheService cacheService;
    private SimpleDateFormat formatter =  new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private Map<Short,String> typeMap = new HashMap<Short, String>();

    @PostConstruct
    private void init(){
        this.typeMap.put((short)1,"服务创建");
        this.typeMap.put((short)2,"服务更新");
        this.typeMap.put((short)3,"服务删除");
        this.typeMap.put((short)4,"心跳摘除");
        this.typeMap.put((short)5,"应用信息更新");
        this.typeMap.put((short)6,"降级设置更新");
    }

    @Override
    public List<OpLogBean> getTopNOpLog(int size) {
        List<OpLog> models = customOpLogModelMapper.getRecentTopNLogs(size);
        List<OpLogBean> been = new LinkedList<OpLogBean>();
        for(Iterator<OpLog> iterator = models.iterator();
                iterator.hasNext();){
            OpLog model = iterator.next();
            been.add(convertModel(model));
        }
        return been;
    }

    @Override
    public int getTotal() {
        return customOpLogModelMapper.getTotalCount();
    }

    @Override
    public List<OpLogBean> filterOpLog(FilterBean filter) {
        List<OpLog> models = customOpLogModelMapper.filterOpLogs(filter.getStartTime(),filter.getEndTime(),filter.getProjectId(),filter.getType());
        List<OpLogBean> been = new LinkedList<OpLogBean>();
        for(Iterator<OpLog> iterator = models.iterator();
                iterator.hasNext();){
            OpLog model = iterator.next();
            been.add(convertModel(model));
        }
        return been;
    }

    private OpLogBean convertModel(OpLog model){
        OpLogBean bean = new OpLogBean();
        bean.setId(model.getId());
        if(model.getProjectid()==null)
            bean.setProjectName("-");
        else
            bean.setProjectName(cacheService.getProjectNameWithId(model.getProjectid()));


        if(model.getDpaccount().equals(Lion.get("pigeon-governor-server.node.heart.check.enable.ip"))){
            bean.setUserName(model.getDpaccount());
        }else{
            String userName = cacheService.getUserNameWithDpAccount(model.getDpaccount());
            if(userName!=null)
                bean.setUserName(userName);
            else
                bean.setUserName(model.getDpaccount());
        }
        bean.setContent(model.getContent());
        bean.setTime(formatter.format(model.getOptime()));
        bean.setType(typeMap.get(model.getOptype()));
        bean.setIpAddresses(model.getReqip());
        return bean;
    }
}
