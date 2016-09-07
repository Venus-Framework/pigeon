package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.Result;
import com.dianping.pigeon.governor.bean.ServiceWithGroup;
import com.dianping.pigeon.governor.exception.DbException;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.ServiceService;
import com.dianping.pigeon.governor.service.UserService;
import com.dianping.pigeon.governor.task.CheckAndSyncServiceDB;
import com.dianping.pigeon.governor.task.CheckAndSyncServiceNodeDB;
import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.zookeeper.CuratorClient;
import com.dianping.pigeon.registry.zookeeper.CuratorRegistry;
import com.dianping.pigeon.registry.zookeeper.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenchongze on 16/1/26.
 */
@Controller
@RequestMapping("/test")
public class TestController {

    private Logger logger = LogManager.getLogger();

    @Autowired
    private CheckAndSyncServiceNodeDB checkAndSyncServiceNodeDB;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private UserService userService;

    public TestController() {
    }

    @RequestMapping(value = {"/user/{oper}"}, method = {RequestMethod.POST})
    @ResponseBody
    public Result addAdmin(@PathVariable final String oper,
            @RequestParam(value="validate") final String validate,
            @RequestParam(value="account") final String account,
            @RequestParam(value="roleid") final Integer roleid,
            HttpServletRequest request, HttpServletResponse response) {

        if(IPUtils.getFirstNoLoopbackIP4Address().equalsIgnoreCase(validate)) {
            String msg = "nothing done.";

            try {
                if ("edit".equals(oper)) {
                    User user = userService.retrieveByDpaccount(account);
                    user.setRoleid(roleid);
                    if(userService.updateById(user)) {
                        msg = "success to update.";
                    }
                } else if ("del".equals(oper)) {
                    if(userService.delete(account)) {
                        msg = "success to del.";
                    }
                }
            } catch (Exception e) {
                msg = "failed, cause by: " + e.toString();
                return Result.createErrorResult(msg);
            }

            return Result.createSuccessResult(msg);
        } else {
            return Result.createErrorResult("failed to validate...");
        }

    }

    @RequestMapping(value = {"/syncnode2db"}, method = {RequestMethod.POST})
    @ResponseBody
    public Result syncnode2db(@RequestParam(value="validate") final String validate,
                         HttpServletRequest request, HttpServletResponse response) {
        if(IPUtils.getFirstNoLoopbackIP4Address().equalsIgnoreCase(validate)) {
            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    checkAndSyncServiceNodeDB.checkAndSyncDB();
                }
            });
            return Result.createSuccessResult("start sync db...");
        } else {
            return Result.createErrorResult("failed to validate...");
        }

    }

    @RequestMapping(value = {"/loglevel"}, method = {RequestMethod.GET})
    @ResponseBody
    public Result loglevel(HttpServletRequest request, HttpServletResponse response) {
        logger.trace("trace");
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");
        logger.fatal("fatal");

        return Result.createSuccessResult("success!");
    }


    @RequestMapping(value = {"/shs/test1"},method = {RequestMethod.GET})
    public String testVelocity(HttpServletRequest request, HttpServletResponse response){
        return "/config/GroupConfig";
    }

}
