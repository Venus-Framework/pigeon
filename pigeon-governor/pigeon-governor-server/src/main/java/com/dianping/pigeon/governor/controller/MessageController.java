package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.Event.EventBean;
import com.dianping.pigeon.governor.bean.Event.EventTableContainerBean;
import com.dianping.pigeon.governor.bean.Event.FilterBean;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.service.EventService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.google.common.base.Stopwatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.dianping.pigeon.governor.util.GsonUtils.Print;
import static com.dianping.pigeon.governor.util.GsonUtils.gson;


/**
 * Created by shihuashen on 16/7/29.
 */
@Controller
public class MessageController extends BaseController{
    @Autowired
    private EventService eventService;
    @Autowired
    private ProjectService projectService;


    @RequestMapping(value = {"/message"},method = RequestMethod.GET)
    public String mainEntrance(HttpServletRequest request,
                               HttpServletResponse response,
                               ModelMap modelMap){
        commonnav(modelMap,request);
        List<Project> projects = projectService.retrieveAllIdNamesByCache();
        modelMap.addAttribute("projects",GsonUtils.toJson(projects));
        return "/message/main";
    }
    @RequestMapping(value = {"/message/table"},method = RequestMethod.POST)
    public String loadTable(HttpServletRequest request,
                            HttpServletResponse response,
                            ModelMap modelMap) throws ParseException {
        String level = request.getParameter("level");
        String type = request.getParameter("type");
        String projectName = request.getParameter("projectName");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        if(startTime==null||startTime.equals("null")){
            List<EventBean> list = eventService.getRecentEvents(500);
            EventTableContainerBean container = new EventTableContainerBean(1,list,eventService.getTotalCount());
            modelMap.put("container",container);
            return "/message/table";
        }
        Project project = projectService.findProject(projectName);
        int projectId = -1;
        if(project!=null){
            projectId = project.getId();
        }
        FilterBean filterBean = new FilterBean(level,type,projectId,startTime,endTime);
        List<EventBean> list = eventService.filterEvents(filterBean);
        EventTableContainerBean container =  new EventTableContainerBean(1,list,eventService.getTotalCount());
        modelMap.put("container",container);
        return "/message/table";
    }


    @RequestMapping(value = {"/message/{messageId}"},method = RequestMethod.GET)
    public String messageDetail(HttpServletRequest request,
                                HttpServletResponse response,
                                ModelMap modelMap,
                                @PathVariable final int messageId){
        return "/message/detail";
    }
    @RequestMapping(value = {"/message/id/content"},method = RequestMethod.POST)
    public String messageContent(HttpServletRequest request,
                                 HttpServletResponse response,
                                 ModelMap modelMap){
        //TODO switch when we need to support different event type;
        List<GraphData> datas =  new LinkedList<GraphData>();
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        datas.add(new GraphData("192.168.0.1",5));
        modelMap.put("data", GsonUtils.toJson(datas));
        return "/message/content/serverSkew";
    }



    public class GraphData{
        private String name;
        private double y;
        public GraphData(String name,double y){
            this.name = name;
            this.y = y;
        }
    }


    public class Value{
        private String key;
        private String value;
        public Value(String key,String value){
            this.key = key;
            this.value = value;
        }
    }
    public class Data{
        private int draw = 1;
        private int recordsTotal = 3;
        private int recordsFiltered = 3;
        private List<Value> data;
        public Data(){
            this.data = new LinkedList<Value>();
            this.data.add(new Value("testKey","testValue"));
            this.data.add(new Value("testKey","testValue"));
            this.data.add(new Value("testKey","testValue"));
        }
    }
}
