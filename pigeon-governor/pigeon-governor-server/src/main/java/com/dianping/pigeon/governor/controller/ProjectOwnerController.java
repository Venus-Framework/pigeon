package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.Result;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.util.CmdbUtils;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.dianping.pigeon.governor.util.ThreadPoolFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Created by chenchongze on 15/10/29.
 */
@Controller
public class ProjectOwnerController extends BaseController {

    private Logger log = LogManager.getLogger();

    @Autowired
    private ProjectOwnerService projectOwnerService;

    @RequestMapping(value = {"/projectowners/cmdb"}, method = RequestMethod.POST)
    @ResponseBody
    public Result refreshOwnerFromCmdb(@RequestParam(value="dpaccount") String dpaccount,
                           @RequestParam(value="projectName") final String projectName,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        Project project = CmdbUtils.getProjectInfo(projectName);

        if(project == null) {
            String message = String.format("CMDB上找不到应用：" + projectName);
            return Result.createErrorResult(message);
        }
        final String emails = project.getEmail();
        ThreadPoolFactory.getWorkThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                //create default project owner
                //TODO product from workflow
                projectOwnerService.createDefaultOwner(emails, projectName);
            }
        });

        String message = String.format("管理员刷新成功：" + projectName);
        return Result.createSuccessResult(message);

    }

    @RequestMapping(value = {"/projectowners.api"}, method = RequestMethod.POST)
    public void api4owners(@RequestParam(value="id") String id,
                           @RequestParam(value="projectid") Integer projectid,
                           @RequestParam(value="userid") Integer userid,
                           @RequestParam(value="createtime") Date createtime,
                           @RequestParam(value="oper") String oper,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        try {
            if("edit".equals(oper)){
                //

            }else if("del".equals(oper)){
                //

            }else if("add".equals(oper)){
                //
                projectOwnerService.create(userid, projectid);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("update projectOwner error");
        }
    }
}
