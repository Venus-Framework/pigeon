package com.dianping.pigeon.governor.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.pigeon.governor.dao.ProcessMapper;
import com.dianping.pigeon.governor.model.Process;
import com.dianping.pigeon.governor.service.ProcessService;

@Service
public class ProcessServiceImpl implements ProcessService {

	@Autowired
	private ProcessMapper processMapper;
	
	@Override
	public void create(Process process) {
		// TODO Auto-generated method stub
		processMapper.insert(process);
	}

}
