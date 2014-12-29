package com.dianping.pigeon.test.benchmark.service;

import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service(url = "com.dianping.pigeon.test.benchmark.service.EchoTestServiceKryoImpl")
public class EchoTestServiceKryoImpl  extends AbstractEchoTestService {

	@Reference(url = "com.dianping.pigeon.demo.EchoService", serialize = "kryo")
	EchoService echoService;

	EchoService getEchoService() {
		return echoService;
	}
}