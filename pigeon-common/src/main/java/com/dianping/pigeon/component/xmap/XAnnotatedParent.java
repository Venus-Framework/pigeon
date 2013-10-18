package com.dianping.pigeon.component.xmap;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XAnnotatedParent extends XAnnotatedMember {

	/**
	 * 
	 * 
	 * @param xmap
	 *            <code>XMap</code>
	 * @param setter
	 *            <code>XSetter<code>
	 * @param getters
	 *            <code>XGetter<code>
	 */
	protected XAnnotatedParent(XMap xmap, XSetter setter, XGetter getter) {
		super(xmap, setter, getter);
	}

	@Override
	protected Object getValue(Context ctx, Element base) throws Exception {
		return ctx.getParent();
	}

	@Override
	public void decode(Object instance, Node base, Document document, List<String> filters) throws Exception {
		// do nothing
	}

}
