package com.dianping.pigeon.remoting.invoker.route.quality;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationRequest;
import com.dianping.pigeon.remoting.invoker.Client;
import com.dianping.pigeon.remoting.invoker.domain.InvokerContext;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by chenchongze on 16/5/20.
 */
public enum RequestQualityManager {

    INSTANCE;
    private RequestQualityManager() {

    }

    private static final ConfigManager configManager = ConfigManagerLoader.getConfigManager();
    private static final String KEY_REQUEST_QUALITY_AUTO = "pigeon.invoker.request.quality.auto";
    private static final String KEY_REQUEST_QUALITY_FAILED_PERCENT_GOOD = "pigeon.invoker.request.quality.failed.percent.good";
    private static final String KEY_REQUEST_QUALITY_FAILED_PERCENT_NORMAL = "pigeon.invoker.request.quality.failed.percent.normal";
    private static final String KEY_REQUEST_QUALITY_THRESHOLD_TOTAL = "pigeon.invoker.request.quality.threshold.total";

    static {
        configManager.getBooleanValue(KEY_REQUEST_QUALITY_AUTO, false);
        configManager.getIntValue(KEY_REQUEST_QUALITY_THRESHOLD_TOTAL, 20);
        configManager.getFloatValue(KEY_REQUEST_QUALITY_FAILED_PERCENT_GOOD, 1f);
        configManager.getFloatValue(KEY_REQUEST_QUALITY_FAILED_PERCENT_NORMAL, 5f);
    }

    // hosts --> ( requestUrl:serviceName#method --> second --> { total, failed } )
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<Integer, Quality>>>
            addrReqUrlSecondQualities = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<Integer, Quality>>>();

    // hosts --> ( requestUrl:serviceName#method --> { total, failed } )
    private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, Quality>> addrReqUrlQualities = null;

    public ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<Integer, Quality>>> getAddrReqUrlSecondQualities() {
        return addrReqUrlSecondQualities;
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
            ConcurrentHashMap<String, ConcurrentHashMap<Integer, Quality>>
                    requestSecondQuality = addrReqUrlSecondQualities.get(address);
            if (requestSecondQuality == null) {
                requestSecondQuality = new ConcurrentHashMap<String, ConcurrentHashMap<Integer, Quality>>();
                ConcurrentHashMap<String, ConcurrentHashMap<Integer, Quality>>
                        last = addrReqUrlSecondQualities.putIfAbsent(address, requestSecondQuality);
                if (last != null) {
                    requestSecondQuality = last;
                }
            }

            String requestUrl = getRequestUrl(context);
            ConcurrentHashMap<Integer, Quality> secondQuality = requestSecondQuality.get(requestUrl);
            if (secondQuality == null) {
                secondQuality = new ConcurrentHashMap<Integer, Quality>();
                ConcurrentHashMap<Integer, Quality> last = requestSecondQuality.putIfAbsent(requestUrl, secondQuality);
                if (last != null) {
                    secondQuality = last;
                }
            }

            int currentSecond = Calendar.getInstance().get(Calendar.SECOND);
            Quality quality = secondQuality.get(currentSecond);
            if(quality == null) {
                quality = new Quality(0, 0);
                Quality last = secondQuality.putIfAbsent(currentSecond, quality);
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
        addrReqUrlSecondQualities.remove(address);
    }

    private String getRequestUrl(InvokerContext context) {
        return context.getInvokerConfig().getUrl() + "#" + context.getMethodName();
    }

    private String getRequestUrl(InvocationRequest request) {
        return request.getServiceName() + "#" + request.getMethodName();
    }

    /**
     * 根据方法的服务质量过滤，优先保留服务质量good的clients，数量低于least时加入服务质量normal的clients
     * @param clientList
     * @param request
     * @param least 最少保留个数
     * @return
     */
    public List<Client> getQualityPreferClients(List<Client> clientList, InvocationRequest request, float least) {
        // 筛选good，normal，bad clients
        // 直接进行服务质量路由,先只保留服务质量good的，如果不够（比如少于1个），加入服务质量normal的
        if (!CollectionUtils.isEmpty(addrReqUrlQualities)) {
            String requestUrl = getRequestUrl(request);

            Map<RequrlQuality, List<Client>> filterQualityClientsMap = new HashMap<RequrlQuality, List<Client>>();
            for(RequrlQuality reqQuality : RequrlQuality.values()) {
                filterQualityClientsMap.put(reqQuality, new ArrayList<Client>());
            }

            for(Client client : clientList) {
                if(addrReqUrlQualities.containsKey(client.getAddress())) {
                    ConcurrentHashMap<String, Quality> reqUrlQualities = addrReqUrlQualities.get(client.getAddress());
                    if(reqUrlQualities.containsKey(requestUrl)) {
                        Quality quality = reqUrlQualities.get(requestUrl);

                        switch (quality.getQuality()) {
                            case REQURL_QUALITY_GOOD:
                                filterQualityClientsMap.get(RequrlQuality.REQURL_QUALITY_GOOD).add(client);
                                break;
                            case REQURL_QUALITY_NORNAL:
                                filterQualityClientsMap.get(RequrlQuality.REQURL_QUALITY_NORNAL).add(client);
                                break;
                            case REQURL_QUALITY_BAD:
                                filterQualityClientsMap.get(RequrlQuality.REQURL_QUALITY_BAD).add(client);
                                break;
                            default:
                                // never be here
                                break;
                        }
                    }
                }
            }

            List<Client> filterQualityClients = new ArrayList<Client>();
            filterQualityClients.addAll(filterQualityClientsMap.get(RequrlQuality.REQURL_QUALITY_GOOD));

            if(filterQualityClients.size() < least) {
                filterQualityClients.addAll(filterQualityClientsMap.get(RequrlQuality.REQURL_QUALITY_NORNAL));
            }

            return filterQualityClients;
        }

        return clientList;
    }

    public boolean isEnableRequestQualityRoute() {
        return configManager.getBooleanValue(KEY_REQUEST_QUALITY_AUTO, false);
    }

    public static class Quality {

        private RequrlQuality quality = RequrlQuality.REQURL_QUALITY_GOOD;
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
            quality = RequrlQuality.REQURL_QUALITY_GOOD;
        }

        public RequrlQuality getQuality() {

            if(getTotalValue() > configManager.getIntValue(KEY_REQUEST_QUALITY_THRESHOLD_TOTAL, 20)) {
                float failedRate = getFailedPercent();

                if(failedRate < configManager.getFloatValue(KEY_REQUEST_QUALITY_FAILED_PERCENT_GOOD, 1f)) {
                    quality = RequrlQuality.REQURL_QUALITY_GOOD;
                } else if(failedRate >= configManager.getFloatValue(KEY_REQUEST_QUALITY_FAILED_PERCENT_GOOD, 1f)
                        && failedRate < configManager.getFloatValue(KEY_REQUEST_QUALITY_FAILED_PERCENT_NORMAL, 5f)) {
                    quality = RequrlQuality.REQURL_QUALITY_NORNAL;
                } else if(failedRate >= configManager.getFloatValue(KEY_REQUEST_QUALITY_FAILED_PERCENT_NORMAL, 5f)) {
                    quality = RequrlQuality.REQURL_QUALITY_BAD;
                }
            }

            return quality;
        }

        public int getQualityValue() {
            return getQuality().getValue();
        }
    }

    private enum RequrlQuality {
        REQURL_QUALITY_GOOD(0),
        REQURL_QUALITY_NORNAL(1),
        REQURL_QUALITY_BAD(2);

        private int value;

        private RequrlQuality(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public static void main(String[] args) {
        Quality quality = new Quality(105, 1);
        System.out.println(quality.getQualityValue());
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
