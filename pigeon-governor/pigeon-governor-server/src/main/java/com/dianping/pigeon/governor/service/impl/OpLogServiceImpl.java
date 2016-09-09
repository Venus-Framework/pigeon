package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.dao.OpLogMapper;
import com.dianping.pigeon.governor.model.OpLog;
import com.dianping.pigeon.governor.service.OpLogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by chenchongze on 15/11/11.
 */
@Service
public class OpLogServiceImpl implements OpLogService {

    private Logger logger = LogManager.getLogger();

    @Autowired
    private OpLogMapper opLogMapper;
    private ExecutorService exec;
    private int poolSize = 20;
    @PostConstruct
    private void init(){
        this.exec = Executors.newFixedThreadPool(poolSize);
    }
    @Override
    public int create(OpLog opLog) {
        int result = 0;

        if(opLog != null) {
            try {
                result = opLogMapper.insertSelective(opLog);
            } catch (DataAccessException e) {
                logger.error("insert opLog error: " + opLog.getContent(), e);
            }
        }

        return result;
    }
    public Future<Integer> asyncCreate(final OpLog opLog){
        final Future<Integer> result = null;
        if(opLog!= null){
            return this.exec.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws DataAccessException{
                    return opLogMapper.insertSelective(opLog);
                }
            });
        }
        return result;
    }
}
