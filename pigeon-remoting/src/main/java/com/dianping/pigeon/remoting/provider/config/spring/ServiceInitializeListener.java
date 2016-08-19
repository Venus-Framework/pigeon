package com.dianping.pigeon.remoting.provider.config.spring;

import com.dianping.pigeon.log.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.provider.publish.ServicePublisher;

public class ServiceInitializeListener implements ApplicationListener {

	private static final Logger logger = LoggerLoader.getLogger(ServiceInitializeListener.class);

	public void onApplicationEvent(ApplicationEvent event) {
		if (ServicePublisher.isAutoPublish()) {
			if (event instanceof ContextRefreshedEvent) {
				ContextRefreshedEvent refreshEvent = (ContextRefreshedEvent) event;
				if (refreshEvent.getApplicationContext().getParent() == null) {
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							logger.info("service initialized");
							try {
								ServiceFactory.online();
							} catch (RegistryException e) {
								logger.error("error with services online", e);
							}
						}

					});
					t.setDaemon(true);
					t.setName("Pigeon-Service-Initialize-Listener");
					t.start();
				}
			}
		}
	}
}
