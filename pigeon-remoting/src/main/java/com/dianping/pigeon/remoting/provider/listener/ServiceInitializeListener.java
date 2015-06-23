package com.dianping.pigeon.remoting.provider.listener;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.registry.exception.RegistryException;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.util.Constants;

public class ServiceInitializeListener {

	private static final Logger logger = LoggerLoader.getLogger(ServiceInitializeListener.class);

	boolean autoPublishEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
			Constants.KEY_AUTOPUBLISH_ENABLE, true);
	boolean autoRegisterEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(
			Constants.KEY_AUTOREGISTER_ENABLE, true);
	boolean warmupEnable = ConfigManagerLoader.getConfigManager().getBooleanValue(Constants.KEY_SERVICEWARMUP_ENABLE,
			true);
	boolean onlineAfterSpringInitialized = ConfigManagerLoader.getConfigManager().getBooleanValue(
			"pigeon.online.springinitialized", false);

	public void onApplicationEvent(ApplicationEvent event) {
		if (onlineAfterSpringInitialized) {
			if (event instanceof ContextRefreshedEvent) {
				ContextRefreshedEvent refreshEvent = (ContextRefreshedEvent) event;
				if (refreshEvent.getApplicationContext().getParent() == null && warmupEnable && autoRegisterEnable
						&& autoPublishEnable) {
					try {
						ServiceFactory.online();
					} catch (RegistryException e) {
						logger.error("error with services online", e);
					}
				}
			}
		}
	}
}
