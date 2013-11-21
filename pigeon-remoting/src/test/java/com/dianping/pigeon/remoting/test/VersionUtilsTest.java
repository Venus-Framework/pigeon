package com.dianping.pigeon.remoting.test;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.pigeon.util.VersionUtils;

public class VersionUtilsTest {

	@Test
	public void testCompareVersion() {
		Assert.assertEquals(-1, VersionUtils.compareVersion("1.2.0", "1.3.0"));
		Assert.assertEquals(-1, VersionUtils.compareVersion("1.2.0", "2.3.0"));
		Assert.assertEquals(1, VersionUtils.compareVersion("3.2.0", "1.3.0"));
		Assert.assertEquals(-1, VersionUtils.compareVersion("1.2.6", "1.3.30"));
		Assert.assertEquals(1, VersionUtils.compareVersion("1.3.0", "1.3.0-SNAPSHOT"));
		Assert.assertEquals(-1, VersionUtils.compareVersion("1.2", "1.3.0"));
		Assert.assertEquals(1, VersionUtils.compareVersion("1.3.0", "1.2"));
		Assert.assertEquals(0, VersionUtils.compareVersion("1.3.0", "1.3.0"));
		Assert.assertEquals(-1, VersionUtils.compareVersion("1.3.0", "1.3"));
		Assert.assertEquals(1, VersionUtils.compareVersion("1.3", "1.3.1"));
	}
}
