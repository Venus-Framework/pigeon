package com.dianping.pigeon.governor.controller;

import com.dianping.pigeon.governor.bean.Result;
import com.dianping.pigeon.governor.task.CheckAndSyncServiceDB;
import com.dianping.pigeon.governor.util.IPUtils;
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

    @Autowired
    private CheckAndSyncServiceDB checkAndSyncServiceDB;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @RequestMapping(value = {"/syncdb"}, method = {RequestMethod.POST})
    @ResponseBody
    public Result oneClickAdd(@RequestParam(value="validate") final String validate,
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
}
