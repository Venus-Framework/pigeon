package com.dianping.pigeon.governor.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import jodd.util.StringUtil;

public class NonAuthFilter implements Filter {
	
	private static String[] excludes;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		
		if (filterConfig != null) {
			String ex = filterConfig.getInitParameter("excludes");
			excludes = ex.split(",");
		}
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String requestURI = req.getRequestURI();
		
		for (String uri : excludes) {
			
			if (requestURI.toLowerCase().startsWith(uri)){
				String queryString = req.getQueryString();
				
				//这里千万不要画蛇添足，会导致queryString添加两次
				/*if (StringUtil.isNotBlank(queryString)) {
					requestURI = requestURI + "?" + queryString;
				}*/
				
				request.getRequestDispatcher(requestURI).forward(request, response);
				
				return;
			}
			
		}
		
		chain.doFilter(request, response);
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
