package com.dianping.pigeon.governor.lion.registry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.util.StringUtils;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.util.NetUtils;

public class ZkListenerFactory {
	
	private static Logger logger = LogManager.getLogger();

	private static ExecutorService addThreadPool = new ThreadPoolExecutor(2, 4, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	private static DefaultCuratorClient lionZkClient;
	private static DefaultCuratorClient pigeonZkClient;
	private static List<PathChildrenCache> cacheList = new ArrayList<PathChildrenCache>();
	
	private static boolean enable = false;
	
	private static volatile boolean isInitialized = false;
	
	public static DefaultCuratorClient getLionZkClient() {
		return lionZkClient;
	}

	public static DefaultCuratorClient getPigeonZkClient() {
		return pigeonZkClient;
	}
	
	private ZkListenerFactory(){};
	
	public static void destroy(){
		
		if(isInitialized) {
			
			for(PathChildrenCache cache : cacheList) {
				try {
					cache.close();
				} catch (IOException e) {
					logger.error("close cache error!");
					return;
				}
			}
			
			cacheList.clear();
			lionZkClient.close();
			pigeonZkClient.close();
			lionZkClient = null;
			pigeonZkClient = null;
			enable = false;
			isInitialized = false;
		}
		
	}
	
	public void init(){
		
		if (!isInitialized) {
			
			try {
				lionZkClient = new DefaultCuratorClient(Lion.get("pigeon-governor-server.lion.zkserver"));
				pigeonZkClient = new DefaultCuratorClient(Lion.get("pigeon-governor-server.pigeon.zkserver"));
			} catch (Exception e) {
				logger.error("Failed to create curatorClient");
				return ;
			}
			
			initCache();
		
			isInitialized = true;
		}
	}
	
	private static void initCache(){
		String servers = Lion.get("pigeon-governor-server.zk.listener");
		
		if(StringUtils.isBlank(servers)) {
			logger.info("服务ip列表为空");
			return;
		}
		
		String[] serverArr = servers.split(",");
		
		for(String server : serverArr){
			
			if (NetUtils.getFirstLocalIp().equals(server)) {
				enable = true;
				break;
			}
				
		}
		
		if(enable){
			logger.info("注册监听节点");
			LinkedList<String> paths = new LinkedList<String>();
			paths.add("/DP/SERVER");
			paths.add("/DP/WEIGHT");
			paths.add("/DP/APP");
			paths.add("/DP/VERSION");
			addListener(paths);
		}
	}
	
	private static void addListener(final LinkedList<String> paths){
		
		if(paths.size() == 0)
			return;
		
		String path = paths.pop();
		final PathChildrenCache cache = new PathChildrenCache(lionZkClient.getClient(), path, true);
		cacheList.add(cache);
		
		try {
			cache.start(StartMode.POST_INITIALIZED_EVENT);
		} catch (Exception e) {
			logger.error("Failed to create pathChildrenCache");
			return;
		}
		
		cache.getListenable().addListener(
			new PathChildrenCacheListener() {
				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
						throws Exception {
					String path = null;
					switch (event.getType()) {
						case CHILD_ADDED:
							path = event.getData().getPath();
							logger.info("CHILD_ADDED: " + path);
							pigeonZkClient.setByte(path, event.getData().getData());
							break;
						case CHILD_REMOVED:
							path = event.getData().getPath();
							logger.info("CHILD_REMOVED: " + path);
							pigeonZkClient.deleteWithChildren(path);
							break;
						case CHILD_UPDATED:
							path = event.getData().getPath();
							logger.info("CHILD_UPDATED: " + path);
							pigeonZkClient.setByte(path, event.getData().getData());
							break;
						case INITIALIZED:
							addListener(paths);
							break;
						default:
							break;
					}
				}
            },
            addThreadPool
		);
		
	}
}
