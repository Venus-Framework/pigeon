package com.dianping.pigeon.config.region;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.dianping.pigeon.config.AbstractConfigManager;
import com.dianping.pigeon.config.ConfigChangeListener;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.util.NetUtils;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class RegionManagerTest {

	@Mocked
	private NetUtils netUtils;
	@Mocked
	private ConfigManagerLoader configManagerLoader;
	private Map<String, String> configs = new HashMap<String, String>() {{
		put("pigeon.regions.enable", "true");
		put("pigeon.regions", "r1:1.1,1.2;r2:2.1,2.2;r3:3.1,3.2");
	}};
	
	@Test
	public void testIsRegionEanbled() {
        new Expectations(){{
        	NetUtils.getFirstLocalIp(); result = "1.1.1.1";
        	ConfigManagerLoader.getConfigManager(); result = new MockedConfigManager(configs);
        }};
        RegionManager regionManager = RegionManager.getInstance();
        assertTrue(regionManager.isRegionEanbled());
	}

	@Test
	public void testGetLocalRegion() {
		new Expectations(){{
        	NetUtils.getFirstLocalIp(); result = "1.1.1.1";
        	ConfigManagerLoader.getConfigManager(); result = new MockedConfigManager(configs);
        }};
        RegionManager regionManager = RegionManager.getInstance();
        assertEquals("r1", regionManager.getLocalRegion());
	}

	@Test
	public void testIsInLocalRegion() {
		new Expectations(){{
        	NetUtils.getFirstLocalIp(); result = "1.1.1.1";
        	ConfigManagerLoader.getConfigManager(); result = new MockedConfigManager(configs);
        }};
        RegionManager regionManager = RegionManager.getInstance();
        assertEquals("r1", regionManager.getLocalRegion());
	}

	@Test
	public void testGetRegion() {
		new Expectations(){{
        	NetUtils.getFirstLocalIp(); result = "1.1.1.1";
        	ConfigManagerLoader.getConfigManager(); result = new MockedConfigManager(configs);
        }};
        RegionManager regionManager = RegionManager.getInstance();
        assertEquals("r1", regionManager.getRegion("1.1 "));
        assertEquals("r2", regionManager.getRegion("2.1.1"));
        assertEquals("r3", regionManager.getRegion(" 3.1.1.1"));
        assertEquals(null, regionManager.getRegion("1"));
        assertEquals(null, regionManager.getRegion("1.3"));
        assertEquals(null, regionManager.getRegion("hello"));
        assertEquals("r1", regionManager.getRegion("1.1.hello"));
	}

	@Test
	public void testIsInRegion() {
		new Expectations(){{
        	NetUtils.getFirstLocalIp(); result = "1.1.1.1";
        	ConfigManagerLoader.getConfigManager(); result = new MockedConfigManager(configs);
        }};
        RegionManager regionManager = RegionManager.getInstance();
        assertEquals(true, regionManager.isInRegion("1.2.1", "r1"));
        assertEquals(false, regionManager.isInRegion("1.2.1", "r2"));
        assertEquals(false, regionManager.isInRegion("4.2.1", "r4"));
	}

	@Test
	public void testIsInSameRegion() {
		fail("Not yet implemented");
	}

	@Test
	public void testFilterLocalAddress() {
		new Expectations(){{
        	NetUtils.getFirstLocalIp(); result = "1.1.1.1";
        	ConfigManagerLoader.getConfigManager(); result = new MockedConfigManager(configs);
        }};
        RegionManager regionManager = RegionManager.getInstance();
        List<String> list = regionManager.filterLocalAddress(new ArrayList<String>() {{
        	add("1.1");add("1.2");add("4.1.1");add("1.4.hello");add("3.1");
        }});
        assertEquals(2, list.size());
        assertEquals("1.2", list.get(1));
        String list2 = regionManager.filterLocalAddress("1.1,1.2:22,4.1.1,1.2.hello,3.1");
        assertEquals(list2, "1.1,1.2:22,1.2.hello");
	}

	@Test
	public void testFilterAddressByRegion() {
		new Expectations(){{
        	NetUtils.getFirstLocalIp(); result = "1.1.1.1";
        	ConfigManagerLoader.getConfigManager(); result = new MockedConfigManager(configs);
        }};
        RegionManager regionManager = RegionManager.getInstance();
        List<String> list = regionManager.filterAddressByRegion(new ArrayList<String>() {{
        	add("1.1");add("1.2");add("4.1.1");add("1.4.hello");add("3.1");
        }}, "r1");
        assertEquals(2, list.size());
        assertEquals("1.2", list.get(1));
        list = regionManager.filterAddressByRegion(new ArrayList<String>() {{
        	add("1.1");add("1.2");add("4.1.1");add("1.4.hello");add("3.1");
        }}, "r3");
        assertEquals(1, list.size());
        assertEquals("3.1", list.get(0));
        list = regionManager.filterAddressByRegion(new ArrayList<String>() {{
        	add("1.1");add("1.2");add("4.1.1");add("1.4.hello");add("3.1");
        }}, "r5");
        assertEquals(0, list.size());
	}

	@Test
	public void testFilterAddressByAddress() {
		new Expectations(){{
        	NetUtils.getFirstLocalIp(); result = "1.1.1.1";
        	ConfigManagerLoader.getConfigManager(); result = new MockedConfigManager(configs);
        }};
        RegionManager regionManager = RegionManager.getInstance();
        List<String> list = regionManager.filterAddressByAddress(new ArrayList<String>() {{
        	add("1.1");add("1.2");add("4.1.1");add("1.4.hello");add("3.1");
        }}, "1.2.3");
        assertEquals(2, list.size());
        assertEquals("1.2", list.get(1));
        list = regionManager.filterAddressByAddress(new ArrayList<String>() {{
        	add("1.1");add("1.2");add("4.1.1");add("1.4.hello");add("3.1");
        }}, "3.2.1");
        assertEquals(1, list.size());
        assertEquals("3.1", list.get(0));
        list = regionManager.filterAddressByRegion(new ArrayList<String>() {{
        	add("1.1");add("1.2");add("4.1.1");add("1.4.hello");add("3.1");
        }}, "4.1.1");
        assertEquals(0, list.size());
	}

	class MockedConfigManager extends AbstractConfigManager {

		private Map<String, String> configs;

		public MockedConfigManager(Map<String, String> configs) {
			this.configs = configs;
		}

		@Override
		public String getConfigServerAddress() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void init() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String doGetProperty(String key) throws Exception {
			return configs.get(key);
		}

		@Override
		public String doGetLocalProperty(String key) throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String doGetEnv() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String doGetLocalIp() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String doGetGroup() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void doSetStringValue(String key, String value) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void doDeleteKey(String key) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void doRegisterConfigChangeListener(ConfigChangeListener configChangeListener) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
	}
}
