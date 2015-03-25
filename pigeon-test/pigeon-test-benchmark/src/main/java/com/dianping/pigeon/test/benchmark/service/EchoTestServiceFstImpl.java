package com.dianping.pigeon.test.benchmark.service;

import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service(url = "com.dianping.pigeon.test.benchmark.service.EchoTestServiceFstImpl")
public class EchoTestServiceFstImpl  extends AbstractEchoTestService {

	@Reference(url = "com.dianping.pigeon.demo.EchoService", serialize = "fst")
	EchoService echoService;

	EchoService getEchoService() {
		return echoService;
	}
}