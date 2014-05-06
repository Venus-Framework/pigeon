package com.dianping.pigeon.remoting.provider.listener;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.dianping.pigeon.remoting.provider.service.ServiceProviderFactory;

public class ServiceInitializeListener implements ApplicationListener {

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if(event instanceof ContextRefreshedEvent) {
			ServiceProviderFactory.startServiceOnlineListener();
		}
	}

}
