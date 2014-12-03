package com.dianping.piegon.governor.test;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.pigeon.governor.task.DisposeTask;
import com.dianping.pigeon.governor.util.Constants.Host;

public class DisposeTaskTest {

	DisposeTask task = new DisposeTask(null);

	@Test
	public void testExistsValidPort() throws Exception {
		//Assert.assertEquals(false, task.existsValidPort(new Host(null, "10.1.8.139", 2022)));
		//Assert.assertEquals(true, task.existsValidPort(new Host(null, "10.1.8.139", 10088)));
	}
}
