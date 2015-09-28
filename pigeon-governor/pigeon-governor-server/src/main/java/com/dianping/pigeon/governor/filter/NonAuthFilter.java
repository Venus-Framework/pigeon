package com.dianping.pigeon.governor.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class NonAuthFilter implements Filter {
	
	private static String[] excludes;
	
	static final String ALREADY_FILTERED_ATTRIBUTE = NonAuthFilter.class.getName() + ".FILTERED";

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
		
		if (request.getAttribute(ALREADY_FILTERED_ATTRIBUTE) != null) {
            chain.doFilter(request, response);
            
        } else {
            request.setAttribute(ALREADY_FILTERED_ATTRIBUTE, Boolean.TRUE);
            
    		String requestURI = ((HttpServletRequest) request).getRequestURI();
    		
    		for (String uri : excludes) {
    			
    			if (requestURI.toLowerCase().startsWith(uri)){
    				
    				//这里千万不要画蛇添足，会导致queryString添加两次
    				/*String queryString = req.getQueryString();
    				if (StringUtil.isNotBlank(queryString)) {
    					requestURI = requestURI + "?" + queryString;
    				}*/
    				
    				request.getRequestDispatcher(requestURI).forward(request, response);
    				
    				return;
    			}
    			
    		}
    		
    		chain.doFilter(request, response);
        }
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
