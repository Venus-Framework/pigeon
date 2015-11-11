package com.dianping.piegon.governor.test;

import java.io.IOException;
import java.util.LinkedList;

import com.dianping.pigeon.governor.bean.ServiceBean;
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
	public void testIfNull(){
		User user = null;
		if(user != null && "yes".equalsIgnoreCase(user.getDpaccount())) {
			System.out.println("11");
		}

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
		String test = "/DP/WEIGHT/1.1.1.1:4080";

		String[] aa = test.split("/");
		/*for (String b : aa){
			System.out.println(b);
		}*/
		System.out.println(aa[3]);
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
