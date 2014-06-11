package com.dianping.pigeon.remoting.provider.listener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;

public class ServiceInitializeListener {

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {

		}
	}

}
