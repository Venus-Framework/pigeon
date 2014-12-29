package com.dianping.pigeon.test.benchmark.service;

import com.dianping.pigeon.remoting.invoker.config.annotation.Reference;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service(url = "com.dianping.pigeon.test.benchmark.service.EchoTestServiceHessianImpl")
public class EchoTestServiceHessianImpl extends AbstractEchoTestService {

	@Reference(url = "com.dianping.pigeon.demo.EchoService", serialize = "hessian")
	EchoService echoService;

	EchoService getEchoService() {
		return echoService;
	}
}
