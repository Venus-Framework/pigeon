package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.ProjectOwnerService;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.service.ServiceNodeService;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.ThreadPoolFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutorService;

/**
 * Created by chenchongze on 16/7/7.
 */
@Controller
@RequestMapping("/new")
public class ServiceNodeController extends BaseController {

    private Logger logger = LogManager.getLogger();
    private ExecutorService workThreadPool = ThreadPoolFactory.getWorkThreadPool();

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectOwnerService projectOwnerService;
    @Autowired
    private ServiceNodeService serviceNodeService;


    @RequestMapping(value = {"/services/{projectName:.+}"}, method = RequestMethod.GET)
    public String projectInfo(ModelMap modelMap,
                              @PathVariable final String projectName,
                              HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(Constants.DP_USER);
        String currentUser = user.getDpaccount();
        modelMap.addAttribute("currentUser", currentUser);
        Project project = projectService.findProject(projectName);

        if(project == null){
            project = projectService.createProjectFromCmdbOrNot(projectName);

            if(project == null){
                modelMap.addAttribute("errorMsg", "cmdb找不到项目或服务从未自动发布过：" + projectName);
                return "/error/500";
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

        }

        return "/serviceNodes/list4project";
    }
}
