package com.dianping.pigeon.remoting.invoker.route.quality;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenchongze on 16/5/20.
 */
public enum RequestQualityManager {

    INSTANCE;
    private RequestQualityManager() {

    }

    private final ConfigManager configManager = ConfigManagerLoader.getConfigManager();

    private static final String KEY_REQUEST_QUALITY_AUTO = "pigeon.invoker.request.quality.auto";

    private static int REQURL_QUALITY_GOOD = 0;
    private static int REQURL_QUALITY_NORNAL = 1;
    private static int REQURL_QUALITY_BAD = 2;

    // hosts --> second --> ( requestUrl:serviceName#method --> { total, failed } )
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, ConcurrentHashMap<String, Quality>>>
            addrSecondReqUrlQualities = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, ConcurrentHashMap<String, Quality>>>();

    // hosts --> ( requestUrl:serviceName#method --> { total, failed } )
    private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, Quality>> addrReqUrlQualities = null;

    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, ConcurrentHashMap<String, Quality>>> getAddrSecondReqUrlQualities() {
        return addrSecondReqUrlQualities;
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, Quality>> getAddrReqUrlQualities() {
        return addrReqUrlQualities;
    }

    public void setAddrReqUrlQualities(ConcurrentHashMap<String, ConcurrentHashMap<String, Quality>> addrReqUrlQualities) {
        this.addrReqUrlQualities = addrReqUrlQualities;
    }

    public void addClientRequest(InvokerContext context, boolean failed) {
        if(configManager.getBooleanValue(KEY_REQUEST_QUALITY_AUTO, false)) {

            String address = context.getClient().getAddress();
            ConcurrentHashMap<Integer, ConcurrentHashMap<String, Quality>>
                    secondRequestQuality = addrSecondReqUrlQualities.get(address);
            if (secondRequestQuality == null) {
                secondRequestQuality = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, Quality>>();
                ConcurrentHashMap<Integer, ConcurrentHashMap<String, Quality>>
                        last = addrSecondReqUrlQualities.putIfAbsent(address, secondRequestQuality);
                if (last != null) {
                    secondRequestQuality = last;
                }
            }

            int currentSecond = Calendar.getInstance().get(Calendar.SECOND);
            ConcurrentHashMap<String, Quality> requestQuality = secondRequestQuality.get(currentSecond);
            if (requestQuality == null) {
                requestQuality = new ConcurrentHashMap<String, Quality>();
                ConcurrentHashMap<String, Quality> last = secondRequestQuality.putIfAbsent(currentSecond, requestQuality);
                if (last != null) {
                    requestQuality = last;
                }
            }

            String requestUrl = getRequestUrl(context);
            Quality quality = requestQuality.get(requestUrl);
            if(quality == null) {
                quality = new Quality(0, 0);
                Quality last = requestQuality.putIfAbsent(requestUrl, quality);
                if(last != null) {
                    quality = last;
                }
            }

            quality.total.incrementAndGet();
            if (failed) {
                quality.failed.incrementAndGet();
            }
        }
    }

    public void removeClientQualities(String address) {
        addrSecondReqUrlQualities.remove(address);
    }

    private String getRequestUrl(InvokerContext context) {
        return context.getInvokerConfig().getUrl() + "#" + context.getMethodName();
    }

    private String getRequestUrl(InvocationRequest request) {
        return request.getServiceName() + "#" + request.getMethodName();
    }

    public List<Client> getQualityPreferClients(List<Client> clientList, InvocationRequest request) {
        String requestUrl = getRequestUrl(request);

        //TODO 筛选good，normal，bad clients
        //TODO 直接进行服务质量路由,先只保留服务质量good的，如果不够（比如少于1个），加入服务质量normal+bad的

        return clientList;
    }

    public static class Quality {

        private int quality = REQURL_QUALITY_GOOD;
        private AtomicInteger failed = new AtomicInteger();
        private AtomicInteger total = new AtomicInteger();

        public Quality() {}

        public Quality(int total, int failed) {
            this.total.set(total);
            this.failed.set(failed);
        }

        public AtomicInteger getFailed() {
            return failed;
        }

        public int getFailedValue() {
            return failed.get();
        }

        public void setFailed(int failed) {
            this.failed.set(failed);
        }

        public AtomicInteger getTotal() {
            return total;
        }

        public int getTotalValue() {
            return total.get();
        }

        public void setTotal(int total) {
            this.total.set(total);
        }

        public float getFailedPercent() {
            if (total.get() > 0) {
                return failed.get() * 100 / total.get();
            } else {
                return 0;
            }
        }

        public void clear() {
            total.set(0);
            failed.set(0);
            quality = REQURL_QUALITY_GOOD;
        }

        public int getQuality() {
            float failedRate = getFailedPercent();

            if(failedRate < 1) {
                quality = REQURL_QUALITY_GOOD;
            } else if(failedRate >= 1 && failedRate < 5) {
                quality = REQURL_QUALITY_NORNAL;
            } else if(failedRate >=5 ) {
                quality = REQURL_QUALITY_BAD;
            }

            return quality;
        }
    }

    public static void main(String[] args) {
        Quality quality = new Quality(105, 1);
        System.out.println(quality.getQuality());
        final ConcurrentHashMap<String, ConcurrentHashMap<String, Quality>> addrReqUrlQualities = new ConcurrentHashMap<String, ConcurrentHashMap<String, Quality>>();
        new Thread() {
            @Override
            public void run() {
                RequestQualityManager.INSTANCE.setAddrReqUrlQualities(addrReqUrlQualities);
            }
        }.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(RequestQualityManager.INSTANCE.getAddrReqUrlQualities());
    }
}
