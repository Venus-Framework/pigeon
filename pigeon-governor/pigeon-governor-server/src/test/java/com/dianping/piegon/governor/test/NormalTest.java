package com.dianping.piegon.governor.test;

import java.io.IOException;

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
	public void testSub(){
		String path = "/DP/SERVER/yesterday";
		String serviceName = path.substring(Constants.SERVICE_PATH.length() + 1);
		System.out.println(serviceName);
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
