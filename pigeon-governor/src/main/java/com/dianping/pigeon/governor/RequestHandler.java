package com.dianping.pigeon.governor;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.dianping.pigeon.component.HostInfo;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.remoting.common.service.ServiceFactory;

/**
 * 
 * 
 * @author jianhuihuang
 * @version $Id: RequestHandler.java, v 0.1 2013-7-18 ���莽5:47:55 jianhuihuang
 *          Exp $
 */
public class RequestHandler extends AbstractHandler {

	private static final Logger logger = Log4jLoader.getLogger(RequestHandler.class);
	private VelocityContext context;
	private Template template;
	private static VelocityEngine ve = new VelocityEngine();

	static {
		ve.setProperty(Velocity.RESOURCE_LOADER, "class");
		ve.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		try {
			ve.init();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);

		initVelocity();

		String rst = getMonitorPage();
		response.getWriter().print(rst);
		if (logger.isDebugEnabled()) {
			logger.debug(rst);
		}
	}

	private void initVelocity() {
		try {
			template = ve.getTemplate("monitor.vm", "gbk");
			context = new VelocityContext();
		} catch (Exception e) {
			logger.error("", e);
		}

	}

	private String getMonitorPage() throws IOException {
		setUpMonitorItem();
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		System.out.println(writer.toString());
		return writer.toString();
	}

	private void setUpMonitorItem() {
		addWeight();
		addServices();
		addThread();
	}

	private void addWeight() {
		Map<String, Set<HostInfo>> weights = RegistryManager.getInstance().getAllServiceServers();
		context.put("weights", weights);
	}

	private void addServices() {
		ServiceFactory serviceManager = ExtensionLoader.getExtension(ServiceFactory.class);
		Map<String, Object> services = serviceManager.getAllServices();
		context.put("allServices", services);
	}

	private void addThread() {
		// ThreadManager threadManager =
		// ExtensionLoader.getExtension(ThreadManager.class);
		//
		// ThreadPoolExecutor clientPool =
		// threadManager.getClinetProcessThreadPool().getExecutor();
		// // context.put("cp", clientPool.getExecutor());
		//
		// ThreadPoolExecutor timeCheckPool =
		// threadManager.getTimeCheckThreadPool().getExecutor();
		// // context.put("tp", timeCheckPool.getExecutor());
		//
		// ThreadPoolExecutor innovationPool =
		// threadManager.getInvocatinTimeCheckThreadPool().getExecutor();
		// // context.put("ip", innovationPool);
		//
		// ThreadPoolExecutor heartPool =
		// threadManager.getHeartBeatThreadPool().getExecutor();
		// // context.put("hp", heartPool);
		//
		// ThreadPoolExecutor reconnectPool =
		// threadManager.getReconnectThreadPool().getExecutor();
		// // context.put("rp", reconnectPool);

		Map<String, ThreadPoolExecutor> threadPool = new HashMap<String, ThreadPoolExecutor>();
		// threadPool.put("Client Process Thread Pool", clientPool);
		// threadPool.put("Time Check Thread Pool", timeCheckPool);
		// threadPool.put("Invocation Time Check Thread Pool", innovationPool);
		// threadPool.put("Heart Beat Thread Pool", heartPool);
		// threadPool.put("Reconnect Thread Pool", reconnectPool);

		context.put("pools", threadPool);
	}

}