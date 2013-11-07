/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.util;

/**
 * Pigeon使用到的静态变量
 * 
 * @author jianhuihuang，saber miao
 * @version $Id: Constants.java, v 0.1 2013-6-17 上午10:59:21 jianhuihuang Exp $
 */
public final class Constants {

	private Constants() {
	}

	// 消息类型----》心跳消息
	public static final int MESSAGE_TYPE_HEART = 1;
	public static final int MESSAGE_TYPE_SERVICE = 2;
	public static final int MESSAGE_TYPE_EXCEPTION = 3;
	public static final int MESSAGE_TYPE_SERVICE_EXCEPTION = 4;
	public static final int MESSAGE_TYPE_ECHO = 5;

	public static final int CALLTYPE_REPLY = 1;
	public static final int CALLTYPE_NOREPLY = 2;

	public static final String CALL_SYNC = "sync";
	public static final String CALL_CALLBACK = "callback";
	public static final String CALL_ONEWAY = "oneway";
	public static final String CALL_FUTURE = "future";

	public static final String SERIALIZE_JAVA = "java";
	public static final String SERIALIZE_HESSIAN = "hessian";

	public static final byte MESSAGE_HEAD_FIRST = 57;
	public static final byte MESSAGE_HEAD_SECOND = 58;
	public static final byte[] MESSAGE_HEAD = new byte[] { MESSAGE_HEAD_FIRST, MESSAGE_HEAD_SECOND };

	public static final byte EXPAND_FLAG_FIRST = 29;
	public static final byte EXPAND_FLAG_SECOND = 30;
	public static final byte EXPAND_FLAG_THIRD = 31;
	public static final byte[] EXPAND_FLAG = new byte[] { EXPAND_FLAG_FIRST, EXPAND_FLAG_SECOND, EXPAND_FLAG_THIRD };

	public static final int ATTACHMENT_RETRY = 1;
	public static final int ATTACHMENT_BYTEBUFFER = 2;
	public static final int ATTACHMENT_IS_TELNET = 3;
	public static final int ATTACHMENT_TELNET_INFO = 5;
	public static final int ATTACHMENT_REQUEST_SEQ = 11;

	public static final String TRANSFER_NULL = "NULL";

	public static final String TELNET_CHARSET = "UTF-8";

	public static final String REQ_ATTACH_FLOW = "FLOW";
	public static final String REQ_ATTACH_WRITE_BUFF_LIMIT = "WRITE_BUFF_LIMIT";

	public static final int VERSION_150 = 150;

	public static final String REQUEST_CREATE_TIME = "requestCreateTime";
	public static final String REQUEST_TIMEOUT = "requestTimeout";
	public static final String REQUEST_FIRST_FLAG = "requestFirstFlag";

	public static final String ECHO_METHOD = "$echo";

	public static final int DEFAULT_FAILOVER_RETRY = 1;
	public static final boolean DEFAULT_FAILOVER_TIMEOUT_RETRY = false;

	public static final String CONFIG_CLUSTER_CLUSTER = "cluster";
	public static final String CONFIG_CLUSTER_RETRY = "retry";
	public static final String CONFIG_CLUSTER_TIMEOUT_RETRY = "timeout-retry";

	public static final String THREADNAME_CLIENT_PRESPONSE_PROCESSOR = "Pigeon-Client-Response-Processor";

	// Pigeon server的处理请求线程池,已经优化为cachethread的方式。
	public static final String THREADNAME_SERVER_REQUEST_PROCESSOR = "Pigeon-Server-Request-Processor";
	public static final String THREADNAME_TELNET_SYSTEM_PROCESSOR = "Pigeon-Telnet-System-Processor";
	public static final String THREADNAME_TELNET_SERVICE_PROCESSOR = "Pigeon-Telnet-Service-Processor";
	public static final String THREADNAME_CLIENT_NETTY_BOSS_EXECUTOR = "Pigeon-Client-Netty-Boss-Executor";
	public static final String THREADNAME_CLIENT_NETTY_WORKER_EXECUTOR = "Pigeon-Client-Netty-Worker-Executor";

	// Netty Server的Boss 线程池，用来和client建立socket链接的线程，后续就交给work thread去处理。
	public static final String THREADNAME_SERVER_NETTY_BOSS_EXECUTOR = "Pigeon-Server-Netty-Boss-Executor";
	// Netty Server的Work 线程池， 和client请求的work线程
	public static final String THREADNAME_SERVER_NETTY_WORKER_EXECUTOR = "Pigeon-Server-Netty-Worker-Executor";

	public static final String CONTEXT_FUTURE = "Context-Future";
	public static final String CONTEXT_SERVER_COST = "Context-Server-Cost";

	// TODO remove me!
	public static final String CONTEXT_REQUEST_SIZE = "Context-Request-Size";
	public static final String CONTEXT_RESPONSE_SIZE = "Context-Response-Size";
	
	// Config keys in /data/webapps/appenv
	public static final String KEY_GROUP = "swimlane";
	public static final String KEY_WEIGHT = "weight";
	public static final String KEY_AUTO_REGISTER = "auto.register";
	public static final String KEY_LOCAL_IP = "local.ip";
	// Deafult value for the above keys
	public static final String DEFAULT_GROUP = "";
	public static final String DEFAULT_WEIGHT = "1";
	public static final int DEFAULT_WEIGHT_INT = Integer.parseInt(DEFAULT_WEIGHT);
	public static final String DEFAULT_AUTO_REGISTER = "true";
}
