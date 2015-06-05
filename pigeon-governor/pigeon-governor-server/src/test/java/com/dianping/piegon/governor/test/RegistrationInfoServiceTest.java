package com.dianping.piegon.governor.test;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.governor.service.RegistrationInfoService;
import com.dianping.pigeon.remoting.ServiceFactory;

public class RegistrationInfoServiceTest {

	@Test
	public void testCheckValidService() throws Exception {
		RegistrationInfoService registrationInfoService = ServiceFactory.getService(RegistrationInfoService.class);
		String app = registrationInfoService.getAppOfService("com.dianping.pigeon.governor.service.RegistrationInfoService");
		Assert.assertEquals("pigeon-governor-server", app);
	}
}
