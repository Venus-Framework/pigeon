package com.dianping.piegon.governor.test.cmdb;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.dianping.pigeon.governor.bean.CmdbSingleProject;
import com.dianping.pigeon.governor.util.CmdbUtils;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.RestCallUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiTest {

	@Test
	public void testCmdbUtils2(){
		List list = CmdbUtils.getProjectsInfoByPage(1);
		System.out.println(list);
		System.out.println(list.size());
	}

	//@Test
	public void testCmdbUtils1(){
		System.out.println(CmdbUtils.getProjectInfo("pigeon-governor-server"));
	}
	
	//@Test
	public void testProAPI2(){
		String url = Constants.CMDB_API_BASE + "/projects/{project_name}";
		url = url.replace("{project_name}", "taurus-web");
    	
		CmdbSingleProject cmdbSingleProject = RestCallUtils.getRestCall(url, CmdbSingleProject.class);
		if(cmdbSingleProject.getProject() == null){
			System.out.println("null");
		}
		
		System.out.println(cmdbSingleProject.toString());
	}
	
	//@Test
	public void testProjectAPI(){
		
		String url = Constants.CMDB_API_BASE + "/projects/{project_name}";
		url = url.replace("{project_name}", "taurus-web");
    	
    	String result = RestCallUtils.getRestCall(url, String.class);
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	
    	CmdbSingleProject to = null;
    	
		try {
			to = objectMapper.readValue(result, CmdbSingleProject.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//result = new String("\u66f2\u5e86\u795d".getBytes("UTF-8"),"UTF-8");
    	
    	System.out.println(to.toString());
    	
    	System.out.println(to.getProject().getOp_duty());
	}
	
	
}
