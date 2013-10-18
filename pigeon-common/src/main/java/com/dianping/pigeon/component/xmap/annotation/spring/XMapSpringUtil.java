package com.dianping.pigeon.component.xmap.annotation.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.w3c.dom.Element;

import com.dianping.pigeon.component.xmap.DOMHelper;
import com.dianping.pigeon.component.xmap.XAnnotatedMember;

public class XMapSpringUtil {

	@SuppressWarnings("rawtypes")
	public static Object getSpringObject(Class type, String beanName, ApplicationContext applicationContext) {
		if (type == Resource.class) {
			return applicationContext.getResource(beanName);
		} else {
			return applicationContext.getBean(beanName, type);
		}
	}

	public static Object getSpringOjbect(XAnnotatedMember xam, ApplicationContext applicationContext, Element base) {
		String val = DOMHelper.getNodeValue(base, xam.path);
		if (val != null && val.length() > 0) {
			if (xam.trim) {
				val = val.trim();
			}
			return getSpringObject(xam.type, val, applicationContext);
		}
		return null;
	}
}
