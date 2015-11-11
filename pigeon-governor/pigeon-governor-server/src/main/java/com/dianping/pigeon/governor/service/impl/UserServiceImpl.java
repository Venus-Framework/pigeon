package com.dianping.pigeon.governor.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dianping.pigeon.governor.dao.UserMapper;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.model.UserExample;
import com.dianping.pigeon.governor.service.UserService;
import com.dianping.pigeon.governor.util.UserRole;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserMapper userMapper;
	
	@Override
	public int create(User user) {
		int sqlSucCount = -1;
		
		user.setCreatetime(new Date());
		sqlSucCount = userMapper.insertSelective(user);
		
		return sqlSucCount;
	}

	@Override
	public User retrieveByDpaccount(String dpaccount) {
		UserExample example = new UserExample();
		example.createCriteria().andDpaccountEqualTo(dpaccount);
		List<User> users = userMapper.selectByExample(example);
		
		if(users != null && users.size() > 0) {
			return users.get(0);
		}
		
		return null;
	}

	@Override
	public boolean isAdmin(String dpaccount) {
		UserExample example = new UserExample();
		example.createCriteria().andDpaccountEqualTo(dpaccount);
		List<User> users = userMapper.selectByExample(example);
		User user = null;
		
		if(users != null && users.size() > 0) {
			user = users.get(0);
			
			if (UserRole.USER_SCM.getValue().equals(user.getRoleid())) {
				return true;
			}
		}
		
		return false;
	}

}
