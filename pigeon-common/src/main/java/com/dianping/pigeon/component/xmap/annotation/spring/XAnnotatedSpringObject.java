package com.dianping.pigeon.component.xmap.annotation.spring;

import org.springframework.context.ApplicationContext;

import com.dianping.pigeon.component.xmap.XAnnotatedObject;
import com.dianping.pigeon.component.xmap.XMap;
import com.dianping.pigeon.component.xmap.annotation.XObject;

public class XAnnotatedSpringObject extends XAnnotatedObject {

	private ApplicationContext applicationContext;

	public XAnnotatedSpringObject(XMap xmap, Class<?> klass, XObject xob, ApplicationContext applicationContext) {
		super(xmap, klass, xob);
		this.applicationContext = applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

}
