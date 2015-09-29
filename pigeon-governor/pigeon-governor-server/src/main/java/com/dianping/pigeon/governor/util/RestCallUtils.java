package com.dianping.pigeon.governor.util;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author chenchongze
 *
 */
public class RestCallUtils {

	public static <T> T getRestCall(String targetUrl, Class<T> responseType){
    	return getRestCall(targetUrl, responseType, 1000, 1000);
	}
	
	public static <T> T getRestCall(String targetUrl, Class<T> responseType, Integer connectTimeout, Integer readTimeout){
		ClientConfig configuration = new ClientConfig();
    	configuration.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout);
    	configuration.property(ClientProperties.READ_TIMEOUT, readTimeout);
    	Client client = ClientBuilder.newClient(configuration);
    	WebTarget target = client.target(targetUrl);
    	
    	if(responseType == String.class) {
    		return target.request().get(responseType);
    	}
    	
    	String resStr = target.request().get(String.class);
    	T result = getBeanFromJson(resStr, responseType);
    	
    	return result;
	}
	
	public static <T> T getBeanFromJson(String json, Class<T> responseType) {
		ObjectMapper objectMapper = new ObjectMapper();
    	T result = null;
    	try {
			result = objectMapper.readValue(json, responseType);
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
    	
    	return result;
	}
}
