package com.dianping.pigeon.governor.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dianping.pigeon.governor.lion.ConfigHolder;
import com.dianping.pigeon.governor.lion.LionKeys;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.service.UserService;
import com.dianping.pigeon.governor.util.Constants;

/**
 * 
 * @author chenchongze
 *
 */
@Controller
public class LoginController extends BaseController {

	private Logger log = LogManager.getLogger();
	
	@Autowired
	private UserService userService;
    
	@Deprecated
	/**
	 * 登陆sso
	 * @param modelMap
	 * @param request
	 * @param response
	 * @param encodeRedirectUri
	 * @throws IOException 
	 */
	@RequestMapping(value = "/ssologin", method = RequestMethod.GET)
	public void ssologin(ModelMap modelMap, 
							HttpServletRequest request,
							HttpServletResponse response) throws IOException 
	{
		String encodedUrl = request.getParameter("redirect-url");
		
	    if (StringUtils.isBlank(encodedUrl)) { encodedUrl = "/"; }
	    
		String userInfoStr = request.getRemoteUser();
		
		if(StringUtils.isBlank(userInfoStr)){
			//TODO
			response.sendRedirect(request.getContextPath() + "/error");
			return ;
		}
		
		//createIfNotExist
		User user = checkUser(userInfoStr);
		
		//sso登录成功之后
		HttpSession session = request.getSession(true);
		session.setAttribute(Constants.DP_ACCOUNT, user.getDpaccount());
		log.info("login success!");

		response.setStatus(200);
		response.sendRedirect(URLDecoder.decode(encodedUrl, "UTF-8"));
		
	}
	
	/**
	 * 登出sso
	 * @param modelMap
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value = "/ssologout", method = RequestMethod.GET)
	public void ssologout(ModelMap modelMap, 
							HttpServletRequest request,
							HttpServletResponse response) throws IOException 
	{
		// 只销毁了session。在线用户库里的注销工作在session的SessionDestroyedListener里完成
        request.getSession().invalidate(); 
        
        log.info("logout success!");
        
        String ssoLogoutUrl = ConfigHolder.get(LionKeys.SSO_LOUGOUT_URL);
        String taurusUrl = ConfigHolder.get(LionKeys.WEB_SERVERNAME);
        
        response.sendRedirect(ssoLogoutUrl + "?service=" + URLEncoder.encode(taurusUrl, "UTF-8"));
	}
	
	@Deprecated
	private User checkUser(String ssoUserInfo) {
		String[] ssoInfos = ssoUserInfo.split("\\|");
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
