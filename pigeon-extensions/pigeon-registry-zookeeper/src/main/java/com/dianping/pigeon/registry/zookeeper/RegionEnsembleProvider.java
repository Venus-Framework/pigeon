package com.dianping.pigeon.registry.zookeeper;

import java.io.IOException;

import org.apache.curator.ensemble.EnsembleProvider;

import com.dianping.pigeon.config.region.RegionManager;

public class RegionEnsembleProvider implements EnsembleProvider {

	private String defaultConnectionString;
	private String regionConnectionString;

	private RegionManager regionManager = RegionManager.getInstance();
	
	RegionEnsembleProvider() {}
	
	public RegionEnsembleProvider(String connectionString) {
		if(connectionString == null) {
			throw new NullPointerException("connection string is null");
		}
		this.defaultConnectionString = connectionString;
		init();
	}
	
	private void init() {
		if(regionManager.isRegionEanbled()) {
			regionConnectionString = getRegionConnectString(defaultConnectionString);
		}
	}
	
	String getRegionConnectString(String defaultConnectionString) {
		return regionManager.filterLocalAddress(defaultConnectionString);
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
