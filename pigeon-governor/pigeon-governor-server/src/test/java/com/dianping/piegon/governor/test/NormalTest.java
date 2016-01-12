package com.dianping.piegon.governor.test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import com.dianping.pigeon.governor.bean.ServiceBean;
import com.dianping.pigeon.governor.model.Service;
import com.dianping.pigeon.governor.model.User;
import com.dianping.pigeon.governor.util.IPUtils;
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
