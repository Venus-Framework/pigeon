package com.dianping.pigeon.governor.filter;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dianping.pigeon.governor.util.Constants;

import jodd.util.StringUtil;

/**
 * 
 * @author chenchongze
 *
 */
public class AuthenticationFilter implements Filter {

	private String[] excludePages;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		if (filterConfig != null) {
			String excludePage = filterConfig.getInitParameter("excludePage");
			excludePages = excludePage.split(",");
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	      ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String requestURI = req.getRequestURI();
		
		// Filter出口1. /api接口放行
		// 登录/ssologin 本机放行，cas不能放行，否则可以伪造cas认证信息；登出/ssologout 本机和cas都放行
		for (String uri : excludePages) {
			if (requestURI.toLowerCase().startsWith(uri)){
				chain.doFilter(request, response);
				return;
			}
		}
		
		if (StringUtil.isNotBlank(req.getQueryString())) {
			requestURI = requestURI + "?" + req.getQueryString();
		}
		
		HttpSession session = req.getSession(true);
		Object currentUser = session.getAttribute(Constants.USER_NAME);
		
		//未登录
		if (currentUser == null) {
			String loginUrl =  req.getContextPath() 
								+ "/ssologin?redirect-url=" 
								+ URLEncoder.encode(requestURI, "UTF-8");
			req.getRequestDispatcher(loginUrl).forward(req, res);
		} else {// Filter出口2.
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
	}

}
