package com.dianping.pigeon.governor.service;

import java.util.List;

import com.dianping.pigeon.registry.exception.RegistryException;

/**
 * pigeon注册信息服务
 * @author xiangwu
 *
 */
public interface RegistrationInfoService {

	/**
	 * 获取服务的应用名称
	 * @param url 服务名称，标示一个服务的url
	 * @param group 泳道名称，没有填null
	 * @return 应用名称
	 * @throws RegistryException
	 */
	String getAppOfService(String url, String group) throws RegistryException;

	/**
	 * 获取服务的应用名称
	 * @param url 服务名称，标示一个服务的url
	 * @return 应用名称
	 * @throws RegistryException
	 */
	String getAppOfService(String url) throws RegistryException;

	/**
	 * 获取服务地址的权重
	 * @param address 服务地址，格式ip:port
	 * @return 权重
	 * @throws RegistryException
	 */
	String getWeightOfAddress(String address) throws RegistryException;

	/**
	 * 获取服务地址的应用名称
	 * @param address 服务地址，格式ip:port
	 * @return 应用名称
	 * @throws RegistryException
	 */
	String getAppOfAddress(String address) throws RegistryException;

	/**
	 * 获取服务的地址列表
	 * @param url 服务名称，标示一个服务的url
	 * @param group 泳道，没有填null
	 * @return 逗号分隔的地址列表，地址格式ip:port
	 * @throws RegistryException
	 */
	List<String> getAddressListOfService(String url, String group) throws RegistryException;

	/**
	 * 获取服务的地址列表
	 * @param url 服务名称，标示一个服务的url
	 * @return 逗号分隔的地址列表，地址格式ip:port
	 * @throws RegistryException
	 */
	List<String> getAddressListOfService(String url) throws RegistryException;
}
