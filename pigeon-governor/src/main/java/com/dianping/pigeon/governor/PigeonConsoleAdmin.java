package com.dianping.pigeon.governor;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.dianping.pigeon.extension.plugin.Component;
import com.dianping.pigeon.monitor.Log4jLoader;

public final class PigeonConsoleAdmin implements Component {

	private static final int CONSOLE_ADMIN_PORT = 9191;

	private static final Logger logger = Log4jLoader.getLogger(PigeonConsoleAdmin.class);
	private static String SHUTDOWN_HOOK_KEY = "true";
	private static boolean running = false;

	private final static Server server = new Server(CONSOLE_ADMIN_PORT);

	public static void begin() {
		new Thread("pigeon-admin-start-thread") {
			public void run() {
				try {

					if ("true".equals(SHUTDOWN_HOOK_KEY)) {
						Runtime.getRuntime().addShutdownHook(new Thread("pigeon-admin-shutdown-thread") {
							public void run() {
								try {
									_destroy();
									if (logger.isInfoEnabled()) {
										logger.info("Jetty Server stopped!");
									}
								} catch (Throwable t) {
									logger.error(t.getMessage(), t);
								}
								synchronized (PigeonConsoleAdmin.class) {
									running = false;
									PigeonConsoleAdmin.class.notify();
								}
							}
						});
					}

					_fire();
					logger.error("Jetty " + " started!" + server.getState() + "server dump" + server.dump());
					logger.error(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date())
							+ " Jetty server started!" + server.getState());

				} catch (Throwable e) {
					logger.error("begin", e);
				}

				synchronized (PigeonConsoleAdmin.class) {
					while (running) {
						try {
							PigeonConsoleAdmin.class.wait();
						} catch (Throwable e) {
							logger.error("", e);
						}
					}
				}
			}
		}.start();

	}

	private static void _fire() {
		synchronized (PigeonConsoleAdmin.class) {
			if (!running) {
				try {
					configServer();
					server.start();
					running = true;
				} catch (Exception e) {
					logger.error("_fire", e);
				}
			}
		}

	}

	private static void _destroy() {
		try {
			server.stop();
		} catch (Exception e) {
			logger.error("_destroy", e);
		}
	}

	private static void configServer() {

		int maxThreads = 5;
		int maxIdeleTimeMs = 30 * 1000;
		int minThreads = 2;
		String poolName = "Pigeon-Admin-Server-Monitor";
		final QueuedThreadPool poolInstance = new QueuedThreadPool();
		poolInstance.setName(poolName);
		poolInstance.setMaxThreads(maxThreads);
		poolInstance.setMinThreads(minThreads);
		poolInstance.setMaxIdleTimeMs(maxIdeleTimeMs);
		server.setHandler(new RequestHandler());
		server.setThreadPool(poolInstance);
	}

	@Override
	public void init() {
		PigeonConsoleAdmin.begin();
	}

}
