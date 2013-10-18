package com.dianping.pigeon.config;

import org.w3c.dom.Element;

import com.dianping.pigeon.component.xmap.XMap;
import com.dianping.pigeon.extension.DefaultExtension;

public class ExtensionBean {

	private String point;

	private DefaultExtension bean;

	private Element content;

	public void init() {
		XMap xmap = new XMap();
		xmap.register(bean.getDescriptorClass());
		try {
			System.out.println(content);
			Object contribution = xmap.load(content);
			bean.registerContribution(contribution, point);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getPoint() {
		return point;
	}

	public void setPoint(String point) {
		this.point = point;
	}

	public DefaultExtension getBean() {
		return bean;
	}

	public void setBean(DefaultExtension bean) {
		this.bean = bean;
	}

	public Element getContent() {
		return content;
	}

	public void setContent(Element content) {
		this.content = content;
	}
}
