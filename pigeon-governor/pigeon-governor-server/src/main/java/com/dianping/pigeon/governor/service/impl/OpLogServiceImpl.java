package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.dao.OpLogMapper;
import com.dianping.pigeon.governor.model.OpLog;
import com.dianping.pigeon.governor.service.OpLogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Created by chenchongze on 15/11/11.
 */
@Service
public class OpLogServiceImpl implements OpLogService {

    private Logger logger = LogManager.getLogger();

    @Autowired
    private OpLogMapper opLogMapper;

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
}
