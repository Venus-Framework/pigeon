package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.util.ThreadPoolFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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


    @RequestMapping(value = {"/services/{projectName:.+}"}, method = RequestMethod.GET)
    public String projectInfo(ModelMap modelMap,
                              @PathVariable final String projectName,
                              HttpServletRequest request) {


        return "/serviceNodes/list4project";
    }
}
