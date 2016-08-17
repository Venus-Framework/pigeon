package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.bean.Event.EventBean;
import com.dianping.pigeon.governor.bean.Event.EventDetailBean;
import com.dianping.pigeon.governor.bean.Event.FilterBean;
import com.dianping.pigeon.governor.bean.Event.FlowSkewEventBean;
import com.dianping.pigeon.governor.dao.CustomEventModelMapper;
import com.dianping.pigeon.governor.dao.EventModelMapper;
import com.dianping.pigeon.governor.message.SendResult;
import com.dianping.pigeon.governor.model.EventModelWithBLOBs;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.service.CacheService;
import com.dianping.pigeon.governor.service.EventService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.google.common.base.Stopwatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by shihuashen on 16/8/5.
 */
@Service
public class EventServiceImpl implements EventService {
    @Autowired
    private CustomEventModelMapper customEventModelMapper;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private EventModelMapper eventModelMapper;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");


    //Load path refactor
    //TODO temp cache to aviod the convert and project DB table access!
    private Map<Integer,String> typeMap = new HashMap<Integer,String>();
    {
        typeMap.put(2,"ClientSkew");
        typeMap.put(1,"ServerSkew");
    }
    private Map<Integer,String> levelMap = new HashMap<Integer,String>();
    {
        levelMap.put(1,"常规");
        levelMap.put(2,"重要");
    }
    private Map<Integer,String> sendResultMap = new HashMap<Integer, String>();
    {
        sendResultMap.put(1,"全部成功");
        sendResultMap.put(2,"部分失效");
        sendResultMap.put(3,"全部失效");
    }
    @Override
    public List<EventBean> getRecentEvents(int size){
        List<EventBean> list = new LinkedList<EventBean>();
        List<EventModelWithBLOBs> models  = customEventModelMapper.getRecentEvents(size);
        for(Iterator<EventModelWithBLOBs> iterator = models.iterator();
                iterator.hasNext();)
            list.add(modelConvert(iterator.next()));
        return list;
    }

    @Override
    public List<EventBean> filterEvents(FilterBean filterBean) {
        List<EventBean> list = new LinkedList<EventBean>();
        List<EventModelWithBLOBs> models =
                customEventModelMapper.filterEvents(filterBean.getStartTime(),
                        filterBean.getEndTime(),
                        filterBean.getProjectId(),
                        filterBean.getLevels(),
                        filterBean.getTypes());
        for(Iterator<EventModelWithBLOBs> iterator = models.iterator();
            iterator.hasNext();)
            list.add(modelConvert(iterator.next()));
        return list;
    }

    @Override
    public int getTotalCount() {
        return customEventModelMapper.getTotalCount();
    }

//    @Override
//    public EventDetailBean getEventDetail(int eventId) {
//        EventModelWithBLOBs model = eventModelMapper.selectByPrimaryKey(eventId);
//
//    }


    private EventBean modelConvert(EventModelWithBLOBs model){
        EventBean bean = new EventBean();
        bean.setEventId(model.getId());
        bean.setType(typeMap.get(model.getEventType()));
        bean.setLevel(levelMap.get(model.getLevel()));
        bean.setTitle(model.getTitle());
        bean.setProjectName(getProjectName(model));
        bean.setRelatedProjectName(getRelatedProjectName(model));
        bean.setSummary(model.getSummary());
        bean.setSendResult(getSendResult(model.getSendStatus()));
        bean.setTime(updateTimeFormat(model.getUpdatetime()));
        return bean;
    }




    private String getProjectName(EventModelWithBLOBs model){
        if(model.getProjectId()==null)
            return "-";
        return cacheService.getProjectNameWithId(model.getProjectId());

    }
    private String getRelatedProjectName(EventModelWithBLOBs model){
        if(model.getRelateProjectId()==null)
            return "-";
        return cacheService.getProjectNameWithId(model.getRelateProjectId());
    }

    private String getSendResult(String json){
        SendResult result = GsonUtils.fromJson(json,SendResult.class);
        List<SendResult.Tuple> tuples = result.getTuples();
        int failCount = 0;
        int totalCount = tuples.size();
        for(Iterator<SendResult.Tuple> iterator = tuples.iterator();
                iterator.hasNext();){
            SendResult.Tuple tuple = iterator.next();
            if(!tuple.isSuccess())
                failCount++;
        }
        if(failCount==totalCount){
            return sendResultMap.get(3);
        }else{
            if(failCount==0)
                return sendResultMap.get(1);
            else
                return sendResultMap.get(2);
        }
    }
    private String updateTimeFormat(Date date){
        return formatter.format(date);
    }


}
