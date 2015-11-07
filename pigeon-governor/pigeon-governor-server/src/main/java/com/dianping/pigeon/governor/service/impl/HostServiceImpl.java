package com.dianping.pigeon.governor.service.impl;

import com.dianping.pigeon.governor.dao.HostMapper;
import com.dianping.pigeon.governor.model.Host;
import com.dianping.pigeon.governor.model.HostExample;
import com.dianping.pigeon.governor.service.HostService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by chenchongze on 15/10/23.
 */
@Service
public class HostServiceImpl implements HostService {

    private Logger logger = LogManager.getLogger();

    @Autowired
    private HostMapper hostMapper;

    @Override
    public int create(Host host) {
        int result = -1;
        result = hostMapper.insertSelective(host);
        return result;
    }

    @Override
    public Host retrieveByIpPort(String ip, String port) {
        HostExample example = new HostExample();
        example.createCriteria().andIpportEqualTo(ip + ":" + port);
        List<Host> hosts = hostMapper.selectByExample(example);

        if(hosts.size() > 0)
            return hosts.get(0);

        return null;
    }

    @Override
    public int update(Host host) {
        int result = -1;
        result = hostMapper.updateByPrimaryKeySelective(host);
        return result;
    }

    @Override
    public List<Host> retrieveAll() {
        List<Host> hosts = null;

        try {
            hosts = hostMapper.selectByExample(null);
        } catch (DataAccessException e) {
            logger.error("DB error", e);
        }

        return hosts;
    }

    @Override
    public int deleteById(Integer id) {
        Integer result = null;

        try {
            result = hostMapper.deleteByPrimaryKey(id);
        } catch (DataAccessException e) {
            logger.error("DB error", e);
        }

        return result;
    }

}
