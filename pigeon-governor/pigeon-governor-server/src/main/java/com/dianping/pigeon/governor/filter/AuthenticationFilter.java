package com.dianping.pigeon.governor.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.UserService;
import com.dianping.pigeon.governor.util.Constants;

/**
 * 
 * @author chenchongze
 *
 */
public class AuthenticationFilter implements Filter {

	private ApplicationContext applicationContext;
	
	private UserService userService;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext());
		userService = (UserService) BeanFactoryUtils.beanOfType(applicationContext, UserService.class);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	      ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String userInfoStr = req.getRemoteUser();
		
		if(StringUtils.isBlank(userInfoStr)){
			res.sendRedirect(req.getContextPath() + "/error");
			return ;
		}
		
		String[] ssoInfos = userInfoStr.split("\\|");
		String dpaccount = ssoInfos[0];
		HttpSession session = req.getSession(true);
		String sessionAccount = (String) session.getAttribute(Constants.DP_ACCOUNT);
		
		if(dpaccount.equalsIgnoreCase(sessionAccount)){
			chain.doFilter(request, response);
			return;
		}
		
		//createIfNotExist
		User user = checkUser(ssoInfos);
		session.setAttribute(Constants.DP_ACCOUNT, user.getDpaccount());
		
		chain.doFilter(request, response);
		
	}

	@Override
	public void destroy() {
	}
	
	private User checkUser(String[] ssoInfos) {
		String dpaccount = ssoInfos[0];
		User user = userService.retrieveByDpaccount(dpaccount);
		
		if(user == null){
			user = new User();
			user.setDpaccount(dpaccount);
			user.setSsologinid(Integer.parseInt(ssoInfos[1]));
			user.setJobnumber(ssoInfos[2]);
			user.setUsername(ssoInfos[3]);
			userService.create(user);
		}
		
		return user;
	}

}
