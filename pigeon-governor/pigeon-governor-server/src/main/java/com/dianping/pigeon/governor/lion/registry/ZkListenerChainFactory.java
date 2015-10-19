package com.dianping.pigeon.governor.lion.registry;

import java.util.LinkedList;

import com.dianping.pigeon.governor.lion.registry.filter.DefaultZkListenerFilter;
import com.dianping.pigeon.governor.lion.registry.filter.ZkListenerFilter;

public class ZkListenerChainFactory {

	private static LinkedList<ZkListenerFilter> filters = new LinkedList<ZkListenerFilter>();
	
	private static volatile boolean isInitialized = false;
	
	private ZkListenerChainFactory(){};
	
	public static void init(){
		if (!isInitialized) {
			registerFilter(new DefaultZkListenerFilter());
			
			isInitialized = true;
		}
	}
	
	public static ZkListenerChain createZkListenerChain() {
			
		return new ZkListenerChain(){
			@Override
			public void dofilter(ZkListenerContext context) throws Throwable {
				ZkListenerFilter filter = filters.pop();
				
				if(filter != null)
					filter.invoke(context, this);
			}
		};

	}
	
	public static void registerFilter(ZkListenerFilter filter){
		filters.add(filter);
	}
	
	public void clearFilters(){
		filters.clear();
	}
}
