package com.dianping.pigeon.test.benchmark.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.dianping.avatar.cache.CacheKey;
import com.dianping.avatar.cache.CacheService;
import com.dianping.cache.exception.CacheException;
import com.dianping.pigeon.remoting.provider.config.annotation.Service;

@Service(url = "com.dianping.cache.test.EhcacheDemoService", interfaceClass = CacheTestService.class)
public class EhcacheTestService extends AbstractCacheTestService {

	@Resource
	private CacheService cacheService;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	public boolean setKeyValue(String key, String value) {
		CacheKey cacheKey = new CacheKey("myehcache", key);
		return cacheService.add(cacheKey, value);
	}

	public String getKeyValue(String key) {
		CacheKey cacheKey = new CacheKey("myehcache", key);
		return cacheService.get(cacheKey);
	}

	@Override
	public List<Object> bulkGetKeyValue(String keys) {
		List<CacheKey> cacheKeys = new ArrayList<CacheKey>();
		if (StringUtils.isNotBlank(keys)) {
			String[] keyArray = keys.split(",");
			for (String key : keyArray) {
				if (StringUtils.isNotBlank(key)) {
					CacheKey cacheKey = new CacheKey("myehcache", key);
					cacheKeys.add(cacheKey);
				}
			}
		}
		if (!CollectionUtils.isEmpty(cacheKeys)) {
			return cacheService.mGet(cacheKeys, false);
		}
		return null;
	}

	@Override
	public boolean removeKey(String key) {
		CacheKey cacheKey = new CacheKey("myehcache", key);
		return cacheService.remove(cacheKey);
	}

	@Override
	public boolean addKeyValue(String key, String value) throws CacheException, TimeoutException {
		CacheKey cacheKey = new CacheKey("myehcache", key);
		return cacheService.addIfAbsent(cacheKey, value);
	}

	public void randomGet() {
		getKeyValue("k-" + (random.nextDouble() * rows));
	}

}
