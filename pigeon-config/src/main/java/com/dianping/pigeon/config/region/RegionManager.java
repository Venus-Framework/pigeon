package com.dianping.pigeon.config.region;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.util.NetUtils;
import com.dianping.pigeon.util.TrieNode;

public class RegionManager {

	private static final String KEY_REGION_ENABLE = "pigeon.regions.enable";
	private static final String KEY_REGION_CONFIG = "pigeon.regions";
	
	private static RegionManager INSTANCE = new RegionManager();
	
	private ConfigManager configManager;
	private boolean regionEnable;
	private String regionConfig;
	private String localAddress;
	private String localRegion;
	private TrieNode<Integer, String> regionTrie;
	
	private RegionManager() {
		configManager = ConfigManagerLoader.getConfigManager();
		regionEnable = configManager.getBooleanValue(KEY_REGION_ENABLE, false);
		regionConfig = configManager.getStringValue(KEY_REGION_CONFIG);
		if(regionEnable && regionConfig != null) {
			regionTrie = parseRegionConfig(regionConfig);
		}
		localAddress = NetUtils.getFirstLocalIp();
		if(localAddress != null) {
			localRegion = _getRegion(localAddress);
		}
	}

	/*
	 * region1:10.66,172.24;region2:192.168;region3:10.128
	 */
	private TrieNode<Integer, String> parseRegionConfig(String regionConfig) {
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
	
	public static RegionManager getInstance() {
		return INSTANCE;
	}
	
	public boolean isRegionEanbled() {
		return regionEnable;
	}
	
	public String getLocalRegion() {
		return localRegion;
	}

	public boolean isInLocalRegion(String address) {
		if(address == null) {
			throw new NullPointerException("address is null");
		}
		if(localAddress.equals(address)) {
			return true;
		}
		if(localRegion == null) {
			return false;
		}
		String region = _getRegion(address);
		return localRegion.equalsIgnoreCase(region);
	}
	
	public String getRegion(String address) {
		if(address == null) {
			throw new NullPointerException("address is null");
		}
		return _getRegion(address);
	}
	
	private String _getRegion(String address) {
		if(regionTrie == null) 
			return null;
		String[] dots = address.split("\\.");
		TrieNode<Integer, String> parentNode = regionTrie;
		for(String dot : dots) {
			try {
				Integer key = Integer.valueOf(dot.trim());
				TrieNode<Integer, String> node = parentNode.getChild(key);
				if(node != null) {
					parentNode = node;
				} else {
					break;
				}
			} catch(NumberFormatException nfe) {
				break;
			}
		}
		return parentNode.getValue();
	}

	public boolean isInRegion(String address, String region) {
		if(address == null) {
			throw new NullPointerException("address is null");
		}
		if(region == null) {
			throw new NullPointerException("region is null");
		}
		String region2 = _getRegion(address);
		return region.equalsIgnoreCase(region2);
	}
	
	public boolean isInSameRegion(String address1, String address2) {
		if(address1 == null || address2 == null) {
			throw new NullPointerException("address is null");
		}
		if(address1.equals(address2)) {
			return true;
		}
		String region1 = _getRegion(address1);
		if(region1 == null) {
			return false;
		}
		String region2 = _getRegion(address2);
		if(region2 == null) {
			return false;
		}
		return region1.equalsIgnoreCase(region2);
	}
	
	public List<String> filterLocalAddress(List<String> addressList) {
		if(addressList == null) {
			throw new NullPointerException("address list is null");
		}
		if(localRegion == null) {
			return Collections.emptyList();
		}
		return _filterAddressByRegion(addressList, localRegion);
	}
	
	/**
	 * Filter addresses in local region
	 *     
	 * @param addressList comma  separated address list, e.g. "1.1.1.1:1111,2.2.2.2,3.3.3.3:3333,4.4.4.4"
	 * @return comma  separated address list or <br/>null if no address in local region
	 */
	public String filterLocalAddress(String addressList) {
		if(addressList == null) {
			throw new NullPointerException("address list is null");
		}
		if(localRegion == null) {
			return null;
		}
		List<String> filteredList = _filterAddressByRegion(addressList.split(","), localRegion);
		return _toString(filteredList);
	}
	
	public List<String> filterAddressByRegion(List<String> addressList, String region) {
		if(addressList == null) {
			throw new NullPointerException("address list is null");
		}
		if(region == null) {
			throw new NullPointerException("region is null");
		}
		return _filterAddressByRegion(addressList, region);
	}
	
	public String filterAddressByRegion(String addressList, String region) {
		if(addressList == null) {
			throw new NullPointerException("address list is null");
		}
		if(region == null) {
			throw new NullPointerException("region is null");
		}
		List<String> filteredList = _filterAddressByRegion(addressList.split(","), region);
		return _toString(filteredList);
	}
	
	public List<String> filterAddressByAddress(List<String> addressList, String address) {
		if(address == null) {
			throw new NullPointerException("address is null");
		}
		if(addressList == null) {
			throw new NullPointerException("address list is null");
		}
		String region = _getRegion(address);
		if(region == null) {
			return Collections.emptyList();
		}
		return _filterAddressByRegion(addressList, region);
	}
	
	public String filterAddressByAddress(String addressList, String address) {
		if(address == null) {
			throw new NullPointerException("address is null");
		}
		if(addressList == null) {
			throw new NullPointerException("address list is null");
		}
		String region = _getRegion(address);
		if(region == null) {
			return null;
		}
		List<String> filteredList = _filterAddressByRegion(addressList.split(","), region);
		return _toString(filteredList);
	}
	
	private String _toString(List<String> addressList) {
		if(addressList == null || addressList.size() <= 0) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		result.append(addressList.get(0));
		if(addressList.size() > 1) {
			for(int i=1; i<addressList.size(); i++) {
				result.append(',').append(addressList.get(i));
			}
		}
		return result.toString();
	}
	
	private List<String> _filterAddressByRegion(List<String> addressList, String region) {
		List<String> filteredAddressList = new ArrayList<String>();
		for(String address : addressList) {
			int idx = address.indexOf(':');
			String ip = idx == -1 ? address : address.substring(0, idx);
			if(region.equalsIgnoreCase(_getRegion(ip))) {
				filteredAddressList.add(address);
			}
		}
		return filteredAddressList;
	}
	
	private List<String> _filterAddressByRegion(String[] addressList, String region) {
		List<String> filteredAddressList = new ArrayList<String>();
		for(String address : addressList) {
			int idx = address.indexOf(':');
			String ip = idx == -1 ? address : address.substring(0, idx);
			if(region.equalsIgnoreCase(_getRegion(ip))) {
				filteredAddressList.add(address);
			}
		}
		return filteredAddressList;
	}

}
