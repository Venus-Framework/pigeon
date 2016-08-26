/**
 * 
 */
package com.dianping.pigeon.remoting.invoker.process;

import com.dianping.pigeon.log.Logger;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.monitor.Monitor;
import com.dianping.pigeon.monitor.MonitorLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.process.threadpool.ResponseThreadPoolProcessor;

/**
 * @author xiangwu
 * 
 */
public abstract class AbstractResponseProcessor implements ResponseProcessor {

	protected static final Logger logger = LoggerLoader.getLogger(ResponseThreadPoolProcessor.class);

	protected static final Monitor monitor = MonitorLoader.getMonitor();

	public abstract void doProcessResponse(InvocationResponse response, Client client);

	@Override
	public void processResponse(InvocationResponse response, Client client) {
		try {
			doProcessResponse(response, client);
		} catch (Throwable e) {
			String error = String.format("process response failed:%s, processor stats:%s", response,
					getProcessorStatistics());
			logger.error(error, e);
			if(monitor != null) {
				monitor.logError(error, e);
			}
		}
	}

}
