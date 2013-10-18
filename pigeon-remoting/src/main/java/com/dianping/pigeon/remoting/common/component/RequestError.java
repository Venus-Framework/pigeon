/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.component;

/**
 * 
 * 请求处理错误枚举
 * 
 * @author jianhuihuang
 * @version $Id: RequestError.java, v 0.1 2013-6-18 上午10:36:24 jianhuihuang Exp
 *          $
 */
public enum RequestError {

	NOCONNECTION("no connect for use"),

	TIMEOUT("request timeout"),

	CHANNELFAIL("request error");

	private String errorMsg;

	private RequestError(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getMsg() {
		return this.errorMsg;
	}

}
