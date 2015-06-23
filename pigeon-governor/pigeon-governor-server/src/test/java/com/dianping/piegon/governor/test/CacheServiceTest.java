package com.dianping.piegon.governor.test;

import org.junit.Test;

import com.dianping.pigeon.governor.service.CacheDemoService;
import com.dianping.pigeon.remoting.ServiceFactory;

public class CacheServiceTest {

	@Test
	public void testMemcacheService() throws Exception {
		CacheDemoService cacheService = ServiceFactory.getService("com.dianping.cache.test.MemcacheDemoService",
				CacheDemoService.class);
		while (true) {
			System.out.println(cacheService.getKeyValue("a"));
		}
	}
}
