/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.extension;

import org.w3c.dom.Element;

public interface Extension {

	ExtensionName getComponentName();

	ExtensionName getTargetComponentName();

	String getExtensionPoint();

	Element getElement();

	Object[] getContributions();

	/**
	 * Identifies the extension inside the contributing component. The id should
	 * be unique in the application. It is recommended to use the following name
	 * convention for the ID: 'component_name#contribution_name'
	 * <p>
	 * The id is never null. If the user is not specifying an ID, one will be
	 * generated as follow: componentName#targetExtensionPoint.randomNumber
	 * 
	 * @return
	 */
	String getId();

	String getDocumentation();

}
