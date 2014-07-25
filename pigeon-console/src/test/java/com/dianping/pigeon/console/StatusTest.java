package com.dianping.pigeon.console;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.console.status.checker.GlobalStatusChecker;

public class StatusTest {

	@Test
	public void test1() throws Exception {
		GlobalStatusChecker status = new GlobalStatusChecker();
		Assert.assertArrayEquals(new String[] { "env", "swimlane", "weight", "error", "status" }, status
				.getGlobalStatusProperties().keySet().toArray());
	}
}
