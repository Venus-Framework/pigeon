package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.op.FilterBean;
import com.dianping.pigeon.governor.bean.op.OpLogBean;
import com.dianping.pigeon.governor.bean.op.OpLogContainerBean;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.service.OpLogManageService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.google.common.base.Stopwatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by shihuashen on 16/8/9.
 */
@Controller
public class OpLogController extends BaseController{
    @Autowired
    private OpLogManageService opLogManageService;
    @Autowired
    private ProjectService projectService;

    @RequestMapping(value = {"/oplog"},method = RequestMethod.GET)
    public String mainPage(HttpServletRequest request,
                           HttpServletResponse response,
                           ModelMap modelMap){
        commonnav(modelMap,request);
        List<Project> projects = projectService.retrieveAllIdNamesByCache();
        modelMap.addAttribute("projects", GsonUtils.toJson(projects));
        return "/v2/opLog/main";
    }
    @RequestMapping(value={"/oplog/table"},method = RequestMethod.POST)
    public String getTable(HttpServletRequest request,
                           HttpServletResponse response,
                           ModelMap modelMap) throws ParseException {
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();
        FilterBean filter = new FilterBean(request,projectService);
        List<OpLogBean> opLogBeen = opLogManageService.filterOpLog(filter);
        stopwatch.stop();
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));
        int totalCount = opLogManageService.getTotal();
        OpLogContainerBean container = new OpLogContainerBean(opLogBeen,totalCount);
        modelMap.put("container",container);
        return "/v2/opLog/table";
    }
}
