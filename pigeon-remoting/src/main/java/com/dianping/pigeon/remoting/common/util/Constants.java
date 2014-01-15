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

	public Constants() {
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
	public static final String SERIALIZE_PROTOBUF = "protobuf";
	public static final String SERIALIZE_JSON = "json";

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

	public static final String CONTEXT_FUTURE = "Context-Future";
	public static final String CONTEXT_SERVER_COST = "Context-Server-Cost";

	// Config keys in /data/webapps/appenv
	public static final String KEY_GROUP = "swimlane";
	public static final String KEY_WEIGHT = "weight";
	public static final String KEY_AUTO_REGISTER = "auto.register";
	public static final String KEY_LOCAL_IP = "host.ip";
	// Deafult value for the above keys
	public static final String DEFAULT_GROUP = "";
	public static final int DEFAULT_WEIGHT = 1;
	public static final String DEFAULT_AUTO_REGISTER = "true";

	public static final String KEY_LOADBALANCE = "pigeon.loadbalance";
	public static final String KEY_RECONNECT_INTERVAL = "pigeon.reconnect.interval";
	public static final String KEY_HEARTBEAT_INTERVAL = "pigeon.heartbeat.interval";
	public static final String KEY_HEARTBEAT_TIMEOUT = "pigeon.heartbeat.timeout";
	public static final String KEY_HEARTBEAT_DEADTHRESHOLD = "pigeon.heartbeat.dead.threshold";
	public static final String KEY_HEARTBEAT_HEALTHTHRESHOLD = "pigeon.heartbeat.health.threshold";
	public static final String KEY_HEARTBEAT_AUTOPICKOFF = "pigeon.heartbeat.autopickoff";
	public static final String KEY_SERVICE_NAMESPACE = "pigeon.service.namespace";
	public static final String KEY_MONITOR_ENABLED = "pigeon.monitor.enabled";
	public static final String KEY_INVOKER_MAXREQUESTS = "pigeon.invoker.maxrequests";
	public static final String KEY_PROVIDER_COREPOOLSIZE = "pigeon.provider.corePoolSize";
	public static final String KEY_PROVIDER_MAXPOOLSIZE = "pigeon.provider.maxPoolSize";
	public static final String KEY_PROVIDER_WORKQUEUESIZE = "pigeon.provider.workQueueSize";
	public static final String KEY_INVOKER_MAXPOOLSIZE = "pigeon.invoker.maxPoolSize";
	public static final String KEY_INVOKER_TIMEOUT = "pigeon.invoker.timeout";
	public static final String KEY_PROCESS_TYPE = "pigeon.process.type";
	public static final String KEY_TIMEOUT_INTERVAL = "pigeon.timeout.interval";
	public static final String KEY_DEFAULT_WRITE_BUFF_LIMIT = "pigeon.channel.writebuff.defaultlimit";

	public static final int DEFAULT_INVOKER_TIMEOUT = 5000;
	public static final int DEFAULT_PROVIDER_COREPOOLSIZE = 100;
	public static final int DEFAULT_PROVIDER_MAXPOOLSIZE = 300;
	public static final int DEFAULT_PROVIDER_WORKQUEUESIZE = 100;
	public static final int DEFAULT_INVOKER_MAXPOOLSIZE = 300;
	public static final long DEFAULT_RECONNECT_INTERVAL = 3000;
	public static final long DEFAULT_HEARTBEAT_INTERVAL = 3000;
	public static final long DEFAULT_HEARTBEAT_TIMEOUT = 5000;
	public static final long DEFAULT_HEARTBEAT_DEADCOUNT = 5;
	public static final long DEFAULT_HEARTBEAT_HEALTHCOUNT = 5;
	public static final boolean DEFAULT_HEARTBEAT_AUTOPICKOFF = true;
	public static final String DEFAULT_SERVICE_NAMESPACE = "http://service.dianping.com/";
	public static final int DEFAULT_WRITE_BUFFER_HIGH_WATER = 35 * 1024 * 1024;
	public static final int DEFAULT_WRITE_BUFFER_LOW_WATER = 25 * 1024 * 1024;
	public static final boolean DEFAULT_WRITE_BUFF_LIMIT = false;
	public static final String DEFAULT_PROCESS_TYPE = "threadpool";
	public static final long DEFAULT_TIMEOUT_INTERVAL = 100;
	public static final String ROUTE_ROUNDROBIN = "roundRobin";

	public static final String PROTOCOL_HTTP = "http";
	public static final String PROTOCOL_DEFAULT = "default";
}
