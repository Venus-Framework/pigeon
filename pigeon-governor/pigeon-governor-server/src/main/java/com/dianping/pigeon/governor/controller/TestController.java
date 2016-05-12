package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.Result;
import com.dianping.pigeon.governor.task.CheckAndSyncServiceDB;
import com.dianping.pigeon.governor.util.IPUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by chenchongze on 16/1/26.
 */
@Controller
@RequestMapping("/test")
public class TestController {

    private Logger logger = LogManager.getLogger();
    @Autowired
    private CheckAndSyncServiceDB checkAndSyncServiceDB;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @RequestMapping(value = {"/syncdb"}, method = {RequestMethod.POST})
    @ResponseBody
    public Result syncdb(@RequestParam(value="validate") final String validate,
                              HttpServletRequest request, HttpServletResponse response) {
        if(IPUtils.getFirstNoLoopbackIP4Address().equalsIgnoreCase(validate)) {
            threadPoolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    checkAndSyncServiceDB.checkAndSyncDB();
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

}
