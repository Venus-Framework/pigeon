/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.event;

public class RuntimeServiceEvent {

	public static enum Type {

		RUNTIME_ABOUT_TO_START(0, Topic.STARTUP, "马上启动"), //
		RUNTIME_STARTING(1, Topic.STARTUP, "启动中"), //
		RUNTIME_BEFORE_DEPLOYCOMPLETION(2, Topic.STARTUP, "组件部署"), //
		RUNTIME_STARTED(3, Topic.STARTUP, "启动完成"), //
		RUNTIME_ABOUT_TO_STOP(4, Topic.STARTUP, "服务器将要停止ֹͣ"), //
		RUNTIME_STOPPED(5, Topic.STARTUP, "ͣ服务器停止ֹ"), //
		RUNTIME_SERVICE_PUBLISH(10, Topic.STARTING, "服务发布中"), //
		RUNTIME_RPC_INVOKE_BEFORE(100, Topic.RPC, "RPC调用之前"), //
		RUNTIME_RPC_INVOKE_CONNECT_EXCEPTION(101, Topic.RPC, "RPC调用链接异常"), //
		RUNTIME_RPC_INVOKE_CLIENT_TIMEOUT_EXCEPTION(102, Topic.RPC, "RPC调用客户端超时异常"), //
		RUNTIME_RPC_INVOKE_CLIENT_RECEIVE_RESPONSE(102, Topic.RPC, "RPC调用客户端超时返回"), //
		RUNTIME_RPC_INVOKE_CLIENT_CHANNEL_EXCEPTION(103, Topic.RPC, "RPC调用客户端超时异常"), //
		RUNTIME_RPC_INVOKE_CLIENT_CHANNEL_COMPLETE(103, Topic.RPC, "RPC调用客户端超时异常"), //
		RUNTIME_RPC_INVOKE_AFTER(104, Topic.RPC, "RPC调用之后"), //
		RUNTIME_RPC_INVOKE_CALLBACK(105, Topic.RPC, "RPC调用之后"), //
		RUNTIME_RPC_INVOKE_EXCEPTION(201, Topic.RPC, "RPC调用出现异常");

		private int code;
		private String desc;
		private Topic topic;

		private Type(int code, Topic topic, String desc) {
			this.code = code;
			this.topic = topic;
			this.desc = desc;
		}

		public int getCode() {
			return code;
		}

		public String getDesc() {
			return desc;
		}

		public Topic getTopic() {
			return topic;
		}
	}

	public static enum Topic {
		STARTUP, STARTING, RPC
	}

	// the event id
	public final Type type;

	public final Object eventObj;

	public RuntimeServiceEvent(Type type) {
		this.type = type;
		eventObj = null;
	}

	public RuntimeServiceEvent(Type type, Object eventObj) {
		this.type = type;
		this.eventObj = eventObj;
	}

	public String getEventName() {
		return type.name();
	}

	public Type getEventType() {
		return this.type;
	}

	public Object getEventObject() {
		return this.eventObj;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getEventName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (!(obj instanceof RuntimeServiceEvent)) {
			return false;
		}

		RuntimeServiceEvent other = (RuntimeServiceEvent) obj;

		return other.type == this.type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return type.hashCode();
	}
}
