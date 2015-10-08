package com.dianping.piegon.governor.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.dianping.pigeon.governor.util.RestCallUtils;

public class ServiceApiTest {

	public static void main(String args[]) {
		String managerAddress = "http://127.0.0.1:8080/api/service/";

		String action = "publish";
		String env = "qa";
		String serviceName = getUtf8Encoded("服务2");
		String group = "kkk";
		String ip = "1.1.1.1";
		String port = "4444";
		String projectName = "taurus-web";
		
		StringBuilder url = new StringBuilder();
		url.append(managerAddress).append(action);
		url.append("?env=").append(env).append("&id=3&updatezk=false&service=");
		url.append(serviceName);
		url.append("&group=").append(group);
		url.append("&ip=").append(ip);
		url.append("&port=").append(port);
		url.append("&app=").append(projectName);
		
		String result = RestCallUtils.getRestCall(url.toString(), String.class);
		
		System.out.println(result);
	}
	
	public static String getUtf8Encoded(String target) {
		String result = null;
		
		try {
			result = URLEncoder.encode(target, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
