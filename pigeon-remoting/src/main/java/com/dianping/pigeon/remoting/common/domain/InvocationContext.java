/**
 ** Dianping.com Inc.
 ** Copyright (c) 2003-2013 All Rights Reserved.
 **/
package com.dianping.pigeon.remoting.common.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface InvocationContext {

	InvocationRequest getRequest();

	void setRequest(InvocationRequest request);

	InvocationResponse getResponse();

	/****
	 ** 在整个调用流程中公用，会随着调用被传播，如被修改，会随着调用流被同步
	 ** 
	 ** @param key
	 ** @param value
	 **/
	void putContextValue(String key, Serializable value);

	/****
	 ** 在整个调用流程中公用，会随着调用被传播，如被修改，会随着调用流被同步
	 ** 
	 ** @param key
	 ** @return
	 **/
	Serializable getContextValue(String key);

	/****
	 ** 在整个调用流程中公用，会随着调用被传播，如被修改，会随着调用流被同步
	 ** 
	 ** @return
	 **/
	Map<String, Serializable> getContextValues();

	String getMethodUri();

	void setMethodUri(String uri);

	List<TimePoint> getTimeline();

	enum TimePhase {
		S/** start **/
		, R/** receive **/
		, T/** thread pool **/
		, D/** degrade **/
		, Q/** request **/
		, P/** response **/
		, O/** monitor **/
		, C/** context **/
		, G/** gateway **/
		, A/** authenticate **/
		, U/** business **/
		, M/** method **/
		, F/** future **/
		, B/** back **/
		, E
		/** end **/
	}

	public static class TimePoint {
		TimePhase phase;
		long time;

		public TimePoint(TimePhase phase, long time) {
			this.phase = phase;
			this.time = time;
		}

		public TimePoint(TimePhase phase) {
			this.phase = phase;
			this.time = System.currentTimeMillis();
		}

		public TimePhase getPhase() {
			return phase;
		}

		public void setPhase(TimePhase phase) {
			this.phase = phase;
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public String toString() {
			return phase + "" + time;
		}
	}
}
