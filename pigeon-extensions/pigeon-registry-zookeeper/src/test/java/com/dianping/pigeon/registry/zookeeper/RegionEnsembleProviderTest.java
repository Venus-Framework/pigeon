package com.dianping.pigeon.registry.zookeeper;

import static org.junit.Assert.*;

import org.junit.Test;

import com.dianping.pigeon.util.TrieNode;

public class RegionEnsembleProviderTest {

	@Test
	public void testGetRegion() {
		RegionEnsembleProvider provider = new RegionEnsembleProvider();
		TrieNode regionTrie = provider.parseRegionConfig("region1:10.66,172.24;region2:192.168;region3:10.128");
		assertNotNull(regionTrie);
		String region = provider.getRegion(regionTrie, "10.66");
		assertEquals("region1", region);
		region = provider.getRegion(regionTrie, "172.24");
		assertEquals("region1", region);
		region = provider.getRegion(regionTrie, "192.168");
		assertEquals("region2", region);
		region = provider.getRegion(regionTrie, "10.128");
		assertEquals("region3", region);
		region = provider.getRegion(regionTrie, "10.129");
		assertNull(region);
		regionTrie = provider.parseRegionConfig(" region1 : 10 . 66 , 172 . 24 ; region2 : 192 . 168 ; region3 : 10 . 128 ");
		assertNotNull(regionTrie);
		region = provider.getRegion(regionTrie, "10.66");
		assertEquals("region1", region);
		region = provider.getRegion(regionTrie, "172.24");
		assertEquals("region1", region);
		region = provider.getRegion(regionTrie, "192.168");
		assertEquals("region2", region);
		region = provider.getRegion(regionTrie, "10.128");
		assertEquals("region3", region);
		region = provider.getRegion(regionTrie, "10.129");
		assertNull(region);
		regionTrie = provider.parseRegionConfig("region1:1.1,2.2;region2:1.1.1,2.2.2;region3:1.1.1.1,2.2.2.2");
		assertNotNull(regionTrie);
		region = provider.getRegion(regionTrie, "1");
		assertNull(region);
		region = provider.getRegion(regionTrie, "2.2");
		assertEquals("region1", region);
		region = provider.getRegion(regionTrie, "1.1.1");
		assertEquals("region2", region);
		region = provider.getRegion(regionTrie, "2.2.2.2");
		assertEquals("region3", region);
		region = provider.getRegion(regionTrie, "1.1.1.1.1");
		assertEquals("region3", region);
		region = provider.getRegion(regionTrie, "2.1");
		assertNull(region);
	}

	@Test
	public void testGetRegionConnectionString()	{
		RegionEnsembleProvider provider = new RegionEnsembleProvider();
		String str = provider.getRegionConnectString("", "region1:1.1,2.2;region2:1.1.1,2.2.2;region3:1.1.1.1,2.2.2.2");
		assertNull(str);
		str = provider.getRegionConnectString("172.24,172.24.1,172.26,172.25,172.24.1.2:2181,1.1.1", "me:172.24,172.25;region1:1.1,2.2;region2:1.1.1,2.2.2;region3:1.1.1.1,2.2.2.2");
		assertEquals(str, "172.24,172.24.1,172.25,172.24.1.2:2181");
	}
}
