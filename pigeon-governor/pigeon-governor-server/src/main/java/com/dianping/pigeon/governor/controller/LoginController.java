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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.dianping.pigeon.governor.lion.ConfigHolder;
import com.dianping.pigeon.governor.lion.LionKeys;
import com.dianping.pigeon.governor.util.Constants;

/**
 * 
 * @author chenchongze
 *
 */
@Controller
public class LoginController extends BaseController {

	private Logger log = LogManager.getLogger();
	
    
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
		log.info("ssoUserInfo: " + userInfoStr);
		
		if(StringUtils.isBlank(userInfoStr)){
			//TODO
			response.sendRedirect(request.getContextPath() + "/error");
			return ;
		}
		
		String userName = userInfoStr.split("\\|")[0];
		
		
		//sso登录成功之后
		HttpSession session = request.getSession(true);
		session.setAttribute(Constants.USER_NAME, userName);
		log.info("login success!");

		//createIfNotExist
//		UserDTO userDTO = setUserInfo(userName);
//		ClientResource cr = new ClientResource(LionConfigUtil.RESTLET_API_BASE + "user");
//		cr.post(userDTO);
		
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
	
}
