package com.dianping.pigeon.registry;

import com.dianping.pigeon.registry.util.Constants;

public class RegistryMeta {

	public static final RegistryMeta DEFAULT_REGISTRY_META = new RegistryMeta(
			Constants.DEFAULT_GROUP, 
			Constants.DEFAULT_WEIGHT_INT, 
			Constants.DEFAULT_AUTO_REGISTER_BOOL);
	
	private String group;
	private int weight;
	private boolean autoRegister;

	public RegistryMeta() {
		this.group = Constants.DEFAULT_GROUP;
		this.weight = Constants.DEFAULT_WEIGHT_INT;
		this.autoRegister = Constants.DEFAULT_AUTO_REGISTER_BOOL;
	}
	
	public RegistryMeta(String group, int weight, boolean autoRegister) {
		this.group = group;
		this.weight = weight;
		this.autoRegister = autoRegister;
	}
	
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public boolean isAutoRegister() {
		return autoRegister;
	}

	public void setAutoRegister(boolean autoRegister) {
		this.autoRegister = autoRegister;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder(RegistryMeta.class.getSimpleName());
		buf.append("[group=").append(group);
		buf.append(", weight=").append(weight);
		buf.append(", autoRegister=").append(autoRegister);
		buf.append("]");
		return buf.toString();
	}
}
