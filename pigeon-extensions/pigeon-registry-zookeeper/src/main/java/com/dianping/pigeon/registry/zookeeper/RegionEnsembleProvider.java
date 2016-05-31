package com.dianping.pigeon.registry.zookeeper;

import java.io.IOException;

import org.apache.curator.ensemble.EnsembleProvider;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.util.NetUtils;
import com.dianping.pigeon.util.TrieNode;

public class RegionEnsembleProvider implements EnsembleProvider {

	private String defaultConnectionString;
	private String regionConnectionString;

	RegionEnsembleProvider() {}
	
	public RegionEnsembleProvider(String connectionString) {
		if(connectionString == null) {
			throw new NullPointerException("connection string is null");
		}
		this.defaultConnectionString = connectionString;
		init();
	}
	
	private void init() {
		ConfigManager configManager = ConfigManagerLoader.getConfigManager();
		boolean enableRegion = configManager.getBooleanValue("pigeon.regions.enable", false);
		String regionConfig = configManager.getStringValue("pigeon.regions");
		if(enableRegion && regionConfig != null) {
			regionConnectionString = getRegionConnectString(defaultConnectionString, regionConfig);
		}
	}
	
	String getRegionConnectString(String defaultConnectionString, String regionConfig) {
		String regionConnectionString = null;
		TrieNode<Integer, String> regionTrie = parseRegionConfig(regionConfig);
		String localIp = NetUtils.getFirstLocalIp();
		String currentRegion = getRegion(regionTrie, localIp);
		if(currentRegion != null) {
			StringBuilder buf = new StringBuilder();
			String[] addresses = defaultConnectionString.split(",");
			for(String address : addresses) {
				int idx = address.indexOf(':');
				String ip = idx == -1 ? address : address.substring(0, idx);
				String region = getRegion(regionTrie, ip);
				if(currentRegion.equals(region)) {
					buf.append(address).append(',');
				}
			}
			if(buf.length() > 0) {
				regionConnectionString = buf.substring(0, buf.length()-1).toString();
			}
		}
		return regionConnectionString;
	}

	/*
	 * value: region1:10.66,172.24;region2:192.168;region3:10.128
	 */
	TrieNode<Integer, String> parseRegionConfig(String regionConfig) {
		TrieNode<Integer, String> rootNode = new TrieNode<Integer, String>();
		String[] regions = regionConfig.split(";");
		for(String region : regions) {
			int idx = region.indexOf(':');
			if(idx != -1) {
				String regionName = region.substring(0, idx).trim();
				String regionIpList = region.substring(idx+1);
				String[] ips = regionIpList.split(",");
				for(String ip : ips) {
					String[] dots = ip.split("\\.");
					TrieNode<Integer, String> parentNode = rootNode;
					for(String dot : dots) {
						Integer key = Integer.valueOf(dot.trim());
						TrieNode<Integer, String> node = parentNode.getChild(key);
						if(node == null) {
							node = new TrieNode<Integer, String>(key);
							parentNode.addChild(node);
						}
						parentNode = node;
					}
					parentNode.setValue(regionName);
				}
			}
		}
		return rootNode;
	}

	String getRegion(TrieNode<Integer, String> regionTrie, String address) {
		if(regionTrie == null) 
			return null;
		String[] dots = address.split("\\.");
		TrieNode<Integer, String> parentNode = regionTrie;
		for(String dot : dots) {
			Integer key = Integer.valueOf(dot.trim());
			TrieNode<Integer, String> node = parentNode.getChild(key);
			if(node != null) {
				parentNode = node;
			}
		}
		return parentNode.getValue();
	}
	
	@Override
	public void start() throws Exception {
		// NOP
	}

	@Override
	public String getConnectionString() {
		return regionConnectionString == null ? defaultConnectionString : regionConnectionString;
	}

	@Override
	public void close() throws IOException {
		// NOP
	}
	
}
