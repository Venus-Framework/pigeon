package com.dianping.pigeon.component.xmap.annotation.spring;

import org.w3c.dom.Element;

import com.dianping.pigeon.component.xmap.Context;
import com.dianping.pigeon.component.xmap.Path;
import com.dianping.pigeon.component.xmap.XAnnotatedMember;
import com.dianping.pigeon.component.xmap.XGetter;
import com.dianping.pigeon.component.xmap.XMap;
import com.dianping.pigeon.component.xmap.XSetter;
import com.dianping.pigeon.component.xmap.spring.XNodeSpring;

public class XAnnotatedSpring extends XAnnotatedMember {

	public XAnnotatedSpringObject xaso;

	protected XAnnotatedSpring(XMap xmap, XSetter setter, XGetter getter) {
		super(xmap, setter, getter);
	}

	/**
	 * 
	 * @param xmap
	 *            <code>XMap</code>
	 * @param setter
	 *            <code>XSetter</code>
	 * @param getter
	 *            <code>XGetter</code>
	 * @param anno
	 *            <code>XNodeSrping</code>
	 * @param xaso
	 *            <code>XAnnotatedSpringObject</coed>
	 */
	public XAnnotatedSpring(XMap xmap, XSetter setter, XGetter getter, XNodeSpring anno, XAnnotatedSpringObject xaso) {
		super(xmap, setter, getter);
		path = new Path(anno.value());
		trim = anno.trim();
		type = setter.getType();
		this.xaso = xaso;
	}

	@Override
	protected Object getValue(Context ctx, Element base) throws Exception {
		// scalar field
		if (type == Element.class) {
			// allow DOM elements as values
			return base;
		}

		return XMapSpringUtil.getSpringOjbect(this, xaso.getApplicationContext(), base);
	}
}