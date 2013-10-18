/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.registry.listener;

/**
 * 服务动态调整信息listener
 * 
 * @author marsqing
 * 
 */
public interface ServiceProviderChangeListener {

	/**
	 * 新增provider
	 * 
	 * @param event
	 */
	void providerAdded(ServiceProviderChangeEvent event);

	/**
	 * 删除provider
	 * 
	 * @param event
	 */
	void providerRemoved(ServiceProviderChangeEvent event);

	/**
	 * host权重发生变化
	 * 
	 * @param event
	 */
	void hostWeightChanged(ServiceProviderChangeEvent event);

}
