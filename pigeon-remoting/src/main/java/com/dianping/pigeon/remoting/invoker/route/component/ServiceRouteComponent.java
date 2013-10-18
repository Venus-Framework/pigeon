package com.dianping.pigeon.remoting.invoker.route.component;

import com.dianping.pigeon.event.EventManager;
import com.dianping.pigeon.extension.plugin.Component;
import com.dianping.pigeon.remoting.invoker.route.listener.AfterRPCInvokeListener;
import com.dianping.pigeon.remoting.invoker.route.listener.BeforeRPCInvokeListener;
import com.dianping.pigeon.remoting.invoker.route.listener.ConnectExceptionInRPCInvokeListener;
import com.dianping.pigeon.remoting.invoker.route.listener.ServiceExceptionInRPCInvokeListener;
import com.dianping.pigeon.remoting.invoker.route.stat.barrel.ServiceBarrelExpiredRequestChecker;

public class ServiceRouteComponent implements Component {

	@Override
	public void init() {
		new ServiceBarrelExpiredRequestChecker().start();
		EventManager.getInstance().addServiceListener(new BeforeRPCInvokeListener());
		EventManager.getInstance().addServiceListener(new AfterRPCInvokeListener());
		EventManager.getInstance().addServiceListener(new ServiceExceptionInRPCInvokeListener());
		EventManager.getInstance().addServiceListener(new ConnectExceptionInRPCInvokeListener());
	}

}
