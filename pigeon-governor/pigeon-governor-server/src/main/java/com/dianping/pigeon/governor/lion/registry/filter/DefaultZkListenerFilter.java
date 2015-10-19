package com.dianping.pigeon.governor.lion.registry.filter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;

import com.dianping.pigeon.governor.lion.registry.CuratorClientFactory;
import com.dianping.pigeon.governor.lion.registry.DefaultCuratorClient;
import com.dianping.pigeon.governor.lion.registry.ZkListenerChain;
import com.dianping.pigeon.governor.lion.registry.ZkListenerContext;

public class DefaultZkListenerFilter implements ZkListenerFilter {
	
	private static ExecutorService addThreadPool = new ThreadPoolExecutor(2, 4, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	
	private static DefaultCuratorClient updateToZkClient;

	@Override
	public void invoke(final ZkListenerContext context, final ZkListenerChain chain) throws Throwable {
		
		updateToZkClient = CuratorClientFactory.pigeonZkClient;
		
		final PathChildrenCache cache = new PathChildrenCache(context.getCuratorClient(), context.getListenerPath(), true);
		
		try {
			cache.start(StartMode.POST_INITIALIZED_EVENT);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
							System.out.println("CHILD_ADDED: " + path);
							updateToZkClient.setByte(path, event.getData().getData());
							break;
						case CHILD_REMOVED:
							path = event.getData().getPath();
							System.out.println("CHILD_REMOVED: " + path);
							updateToZkClient.delete(path);
							break;
						case CHILD_UPDATED:
							path = event.getData().getPath();
							System.out.println("CHILD_UPDATED: " + path);
							updateToZkClient.setByte(path, event.getData().getData());
							break;
						case INITIALIZED:
							try {
								chain.dofilter(context);
							} catch (Throwable e) {
								e.printStackTrace();
							}
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
