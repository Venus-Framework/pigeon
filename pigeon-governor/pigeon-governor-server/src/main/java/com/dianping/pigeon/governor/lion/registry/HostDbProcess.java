package com.dianping.pigeon.governor.lion.registry;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.model.Host;
import com.dianping.pigeon.governor.service.HostService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenchongze on 15/10/23.
 */
public class HostDbProcess {

    private Logger logger = LogManager.getLogger();

    @Autowired
    private HostService hostService;

    private ExecutorService dbThreadPool = new ThreadPoolExecutor(2, 4, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private ExecutorService addThreadPool = new ThreadPoolExecutor(2, 4, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private DefaultCuratorClient lionZkClient;
    private List<PathChildrenCache> cacheList = new ArrayList<PathChildrenCache>();

    private static final String CHARSET = "UTF-8";


    public void init(){
        try {
            lionZkClient = new DefaultCuratorClient(Lion.get("pigeon-governor-server.lion.zkserver"));
        } catch (Exception e) {
            logger.error("Failed to create curatorClient",e);
            return ;
        }

        initCache();
    }

    private void initCache(){

        logger.info("注册监听节点");
        LinkedList<String> paths = new LinkedList<String>();
        paths.add("/DP/WEIGHT");
        addListener(paths);
    }

    private void addListener(final LinkedList<String> paths){

        if(paths.size() == 0){
            logger.info("no other path");
            return;
        }


        String path = paths.pop();
        final PathChildrenCache cache = new PathChildrenCache(lionZkClient.getClient(), path, true);
        cacheList.add(cache);

        try {
            cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        } catch (Exception e) {
            logger.error("Failed to create pathChildrenCache");
            return;
        }

        cache.getListenable().addListener(
                new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client, final PathChildrenCacheEvent event)
                            throws Exception {
                        String path = null;
                        switch (event.getType()) {
                            case CHILD_ADDED:
                                path = event.getData().getPath();
                                logger.info("CHILD_ADDED: " + path);

                                final String finalPath = path;
                                Runnable dbRun = new Runnable() {
                                    @Override
                                    public void run() {
                                        String ipPort = finalPath.split("/")[3];
                                        String apppath = finalPath.replace("WEIGHT", "APP");
                                        String appname = null;
                                        try {
                                            appname = lionZkClient.get(apppath, false);
                                        } catch (Exception e) {
                                            logger.error(e);
                                        }
                                        String versionpath = finalPath.replace("WEIGHT", "VERSION");
                                        String version = null;
                                        try {
                                            version = lionZkClient.get(versionpath,false);
                                        } catch (Exception e) {
                                            logger.error(e);
                                        }
                                        Host host = new Host();
                                        host.setIpport(ipPort);
                                        host.setAppname(appname);
                                        host.setVersion(version);
                                        //host.setRegistry(0);
                                        hostService.create(host);
                                    }
                                };
                                dbThreadPool.execute(dbRun);


                                break;
                            case CHILD_REMOVED:
                                path = event.getData().getPath();
                                logger.info("CHILD_REMOVED: " + path);

                                break;
                            case CHILD_UPDATED:
                                path = event.getData().getPath();
                                logger.info("CHILD_UPDATED: " + path);

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
