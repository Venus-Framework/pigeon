package com.dianping.piegon.governor.test;

import org.junit.Test;

import com.dianping.pigeon.governor.task.DisposeTask;

public class DisposeTaskTest {

	DisposeTask task = new DisposeTask(null);

	@Test
	public void testCheckValidService() throws Exception {
		//Assert.assertEquals(false, task.existsValidPort(new Host(null, "10.1.8.139", 2022)));
		//Assert.assertEquals(true, task.existsValidPort(new Host(null, "10.1.8.139", 10088)));
	}
	
	@Test
	public void testExistsValidPort() throws Exception {
		//Assert.assertEquals(1, task.checkAppValidWithCmdb("cat", "10.1.101.84"));
		//Assert.assertEquals(-1, task.checkAppValidWithCmdb("deal-search-service", "10.101.3.42"));
	}
}
