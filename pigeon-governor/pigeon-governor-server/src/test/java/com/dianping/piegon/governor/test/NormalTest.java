package com.dianping.piegon.governor.test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import com.dianping.pigeon.governor.bean.ServiceBean;
import com.dianping.pigeon.governor.bean.ServiceWithGroup;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.util.IPUtils;
import com.dianping.pigeon.registry.zookeeper.Utils;
import com.dianping.pigeon.remoting.common.codec.json.JacksonSerializer;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.governor.bean.JqGridReqFilters;
import com.dianping.pigeon.registry.Registry;
import com.dianping.pigeon.registry.util.Constants;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NormalTest {

	@Test
	public void testIndex(){
		String ss = "pigeon.group.invoker.http:^^www.dianping.com^service_1.0.0";
		int length = "pigeon.group.invoker.".length();
		System.out.println(ss.indexOf("pigeon.group.provider."));
		System.out.println(ss.substring(ss.indexOf("pigeon.group.invoker.")+length));
	}

	@Test
	public void escapeServiceName() {
		String url = "http://service.dianping.com/arch/test/service/EchoService_1.0.0";
		System.out.println(Utils.escapeServiceName(url));
	}

	@Test
	public void testJson() {
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		List<Integer> list = new ArrayList<Integer>();
		list.add(200023756);
		list.add(200023715);
		list.add(200023711);
		map.put(10655809, list);
		JacksonSerializer serializer = new JacksonSerializer();
		String str = serializer.serializeObject(map);
		System.out.println(str);
	}

	@Test
	public void testHash() {
		Map<String, Vector<ServiceWithGroup>> hostIndex = new ConcurrentHashMap<String, Vector<ServiceWithGroup>>();
		Map<ServiceWithGroup, Service> serviceGroupDbIndex = new ConcurrentHashMap<ServiceWithGroup, Service>();
		Map<ServiceWithGroup, Service> serviceGroupZkIndex = new ConcurrentHashMap<ServiceWithGroup, Service>();

		serviceGroupDbIndex.put(new ServiceWithGroup("service1","ccz1"), new Service());
		serviceGroupZkIndex.put(new ServiceWithGroup("service1","ccz1"), new Service());

		for(ServiceWithGroup serviceWithGroup : serviceGroupZkIndex.keySet()) {
			if(serviceGroupDbIndex.containsKey(serviceWithGroup)){
				System.out.println(serviceWithGroup);
			}
		}
	}

	@Test
	public void testList() {
		String[] aaa = "".split(",");
		System.out.println(aaa.length);
		System.out.println(aaa[0]);
	}

	@Test
	public void testOverride() {
		ServiceWithGroup sg1 = new ServiceWithGroup("service",null);
		ServiceWithGroup sg2 = new ServiceWithGroup("service","ccz");
		System.out.println(sg1.equals(sg2));
		System.out.println(sg1.hashCode());
		System.out.println(sg2.hashCode());
		System.out.println(sg1);
		System.out.println(sg2);

		Set<ServiceWithGroup> serviceWithGroupSet = new HashSet<ServiceWithGroup>();
		serviceWithGroupSet.add(sg1);
		serviceWithGroupSet.add(sg2);
		System.out.println(serviceWithGroupSet);

	}

	@Test
	public void testHashSet() {
		String t1 = "1.1.1.1,2.2.2.2,1.1.1.1,";
		String t2 = "";
		Set<String> set1 = new HashSet<String>(Arrays.asList(t1.split(",")));
		Set<String> set2 = new HashSet<String>(Arrays.asList(t2.split(",")));
		String r1 = StringUtils.join(set1,",");
		String r2 = StringUtils.join(set2,",");
		System.out.println(r1);
		System.out.println(r2);
		System.out.println(r1.equals(r2));
	}

	@Test
	public void printClass() {
		Map<String, String> map = new HashMap<String, String>();
		System.out.println(map.getClass().getCanonicalName());
	}

	@Test
	public void testDef() {
		TestDef testDef = new TestDef();
		if(testDef == null || testDef.getBbb() == 0) {
			System.out.println("null");
		}
	}

	class TestDef {
		public boolean aaa;
		public int bbb;

		public int getBbb(){
			return bbb;
		}
	}

	@Test
	public void test(){
		long start = System.currentTimeMillis();
		long time = 1453860622893L;
		Date date = new Date(time);
		System.out.println(date);
		long internal = start - time;
		System.out.println(internal + "ms");
		if(internal > 3* 60000) {
			System.out.println("delete");
		}
	}

	class DealHeartBeat {

		private final String host;

		public DealHeartBeat(String host) {
			this.host = host;
		}
	}

	@Test
	public void testSplit3() {
		new DealHeartBeat(null);
		String aaa = "423:22:332:4040";
		String[] tmp = aaa.split(":");
		System.out.println(tmp.length);
		int idx = aaa.lastIndexOf(":");
		System.out.println(idx);
		System.out.println(aaa.substring(0, idx));
		System.out.println(aaa.substring(idx + 1));
		/*for(String a : aaa.split(",")) {
			System.out.println(a);
		}*/
	}

	@Test
	public void testIp() {
		System.out.println(IPUtils.getFirstNoLoopbackIP4Address());
	}

	@Test
	public void testHeartBeat() {

		String test = "1.1.1.1:4040,2.2.2.2:4040,fjdslfjsk,:1:4080,";
		String validHosts = IPUtils.getValidHosts(test);

		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		map.put("10.1.1.1",new ArrayList<String>());
		map.put("10.2.2.2",new ArrayList<String>());

		if(map.containsKey("10.1.1.1")) {
			//加入service
			ArrayList<String> list = map.get("10.1.1.1");
			list.add("service");
			System.out.println(true);
		} else {
			System.out.println(false);
		}

		HashMap<String, Long> ips = new HashMap<String, Long>();
		ips.put("10.1.1.1", 0L);

		for(String key : map.keySet()){
			if(ips.containsKey(key)) {
				// runnable check hb
			} else {
				// nothing
			}
		}

	}

	@Test
	public void testPause() {
		Long refreshInternal = 60000L;
		Long pause = refreshInternal / 10;
		System.out.println(pause);
	}

	@Test
	public void testSleep(){
		try {
			Thread.sleep(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testArrToSet(){
		String host = "1.1.1";

		HashSet<String> set = new HashSet<String>(Arrays.asList("1.1.1,2.2.2,".split(",")));
		set.remove(host);
		System.out.println(StringUtils.join(set,","));
	}

	@Test
	public void testSplit2(){
		String port = "4040/4080";
		System.out.println(port.split("/")[0]);
	}

	@Test
	public void testSet() {
		HashSet<Service> set = new HashSet<Service>();
		Service service1 = new Service();
		service1.setName("111");
		set.add(service1);
		Service service2 = new Service(service1);
		Service service3 = service1;
		service1.setGroup("ccz");

		System.out.println(set.contains(service2));
		System.out.println(set.contains(service3));

	}

	@Test
	public void testJoin(){
		Set<String> set = new HashSet<String>();
		for(String t: "".split(",")){
			//System.out.println(t);
			if(StringUtils.isNotBlank(t)){
				set.add(t);
			}
		}
		set.add("3232");
		System.out.println(StringUtils.join(set,","));
	}

	@Test
	public void testStrComp(){
		String s1 = "2.6.8";
		String s2 = "2.6.4";
		String s3 = "2.6.3-SNAPSHOT";
		System.out.println(s2.compareTo(s2));
		System.out.println(s2.compareTo(s1));
		System.out.println(s2.compareTo(s3));
		System.out.println("a".compareTo("b"));

	}

	@Test
	public void testIfNull(){

		User user = null;
		System.out.println(user.getDpaccount());

	}

	@Test
	public void testObjNullAttr(){
		ServiceBean serviceBean = new ServiceBean();
		serviceBean.setId("1,2,3");
		System.out.println(serviceBean.getHosts());
	}

	@Test
	public void testValidHosts(){
		String test = "1.1.1.1:4040,2.2.2.2:4040,fjdslfjsk,:1:4080,";
		System.out.println(IPUtils.getValidHosts(test));
	}

	@Test
	public void testFormat(){
		String test = String.format("insert projectOwner error! userId: %s,projectId: %s", 5L, 4);
		System.out.print(test);
	}

	@Test
	public void testSplit(){
		String test = ",,";

		String[] aa = test.split(",");
		System.out.println(aa[0]);
		for (String b : aa){
			System.out.println(b+ "1");
		}
		//System.out.println(aa[3]);
	}

	@Test
	public void testSub(){
		LinkedList<Integer> ls = new LinkedList<Integer>();
		ls.add(1);
		ls.add(2);
		System.out.println(ls.pop());
		ls.add(3);
		System.out.println(ls.pop());
	}
	
	//@Test
	public void testServiceLoader(){
		Registry registry = ExtensionLoader.newExtension(Registry.class);
		Registry registry2 = ExtensionLoader.newExtension(Registry.class);
		
		System.out.println(registry);
		System.out.println(registry2);
		
	}
	//@Test
	public void testJackson2() throws JsonParseException, JsonMappingException, IOException{
		String filters = "{\"groupOp\":\"OR\",\"rules\":[{\"field\":\"id\",\"op\":\"eq\",\"data\":\"1\"},{\"field\":\"name\",\"op\":\"eq\",\"data\":\"yes\"}]}";
		ObjectMapper objectMapper = new ObjectMapper();
		JqGridReqFilters filterBean = objectMapper.readValue(filters, JqGridReqFilters.class);
		
		objectMapper.writeValue(System.out, filterBean);
		
	}
}
