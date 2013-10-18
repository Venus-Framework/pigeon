/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension;

public class DefaultExtension implements Extensible {

	protected Class<?> descriptorClass;

	public void registerExtension(Extension extension) throws Exception {
		Object[] contribs = extension.getContributions();
		if (contribs == null) {
			return;
		}
		for (Object contrib : contribs) {
			registerContribution(contrib, extension.getExtensionPoint());
		}
	}

	public void unregisterExtension(Extension extension) throws Exception {
		Object[] contribs = extension.getContributions();
		if (contribs == null) {
			return;
		}
		for (Object contrib : contribs) {
			unregisterContribution(contrib, extension.getExtensionPoint());
		}
	}

	/**
	 * xml bean 要去实现的方法。
	 * 
	 * @param contribution
	 * @param extensionPoint
	 */
	public void registerContribution(Object contribution, String extensionPoint) {
	}

	public void unregisterContribution(Object contribution, String extensionPoint) {
	}

	public Class<?> getDescriptorClass() {
		return descriptorClass;
	}
}
