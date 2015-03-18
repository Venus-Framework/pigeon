package com.dianping.pigeon.governor.util;

import org.apache.commons.lang.StringUtils;

public class AddressUtils {

	public static class Address {
		String ip;
		int port;
		boolean valid;

		public Address() {
		}

		public Address(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public boolean isValid() {
			return valid;
		}

		public void setValid(boolean valid) {
			this.valid = valid;
		}

	}

	public static Address toAddress(String address) {
		Address addr = new Address();
		addr.setValid(false);
		if (!StringUtils.isBlank(address)) {
			int idx = address.lastIndexOf(":");
			if (idx != -1) {
				String ip = null;
				int port = -1;
				try {
					ip = address.substring(0, idx);
					port = Integer.parseInt(address.substring(idx + 1));
				} catch (RuntimeException e) {
					addr.setValid(false);
				}
				if (ip != null && port > 0) {
					addr.setValid(true);
					addr.setIp(ip);
					addr.setPort(port);
				}
			} else {
				addr.setValid(false);
			}
		}
		return addr;
	}
}
