package com.dianping.pigeon.component.xmap.annotation.spring;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.dianping.pigeon.component.xmap.Context;
import com.dianping.pigeon.component.xmap.DOMHelper;
import com.dianping.pigeon.component.xmap.Path;
import com.dianping.pigeon.component.xmap.XAnnotatedMap;
import com.dianping.pigeon.component.xmap.XAnnotatedMember;
import com.dianping.pigeon.component.xmap.XGetter;
import com.dianping.pigeon.component.xmap.XMap;
import com.dianping.pigeon.component.xmap.XSetter;
import com.dianping.pigeon.component.xmap.spring.XNodeMapSpring;

public class XAnnotatedMapSpring extends XAnnotatedMap {

	/**
	 * dom visitor
	 */
	protected static final ElementValueMapVisitor elementVisitor = new ElementValueMapVisitor();
	protected static final AttributeValueMapVisitor attributeVisitor = new AttributeValueMapVisitor();

	private XAnnotatedSpringObject xaso;

	/**
	 * 
	 * @param xmap
	 *            <code>XMap</code>
	 * @param setter
	 *            <code>XSetter</code>
	 * @param getter
	 *            <code>XGetter</code>
	 * @param anno
	 *            <code>XNodeMapSpring</code>
	 * @param xaso
	 *            <code>XAnnotatedSpringObject</code>
	 */
	public XAnnotatedMapSpring(XMap xmap, XSetter setter, XGetter getter, XNodeMapSpring anno,
			XAnnotatedSpringObject xaso) {
		super(xmap, setter, getter, null);
		this.setXaso(xaso);
		path = new Path(anno.value());
		trim = anno.trim();
		key = new Path(anno.key());
		type = anno.type();
		componentType = anno.componentType();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Object getValue(Context ctx, Element base) throws IllegalAccessException, InstantiationException {
		Map<String, Object> values = (Map) type.newInstance();
		if (path.attribute != null) {
			// attribute list
			DOMHelper.visitMapNodes(ctx, this, base, path, attributeVisitor, values);
		} else {
			// element list
			DOMHelper.visitMapNodes(ctx, this, base, path, elementVisitor, values);
		}
		return values;
	}

	public void setXaso(XAnnotatedSpringObject xaso) {
		this.xaso = xaso;
	}

	public XAnnotatedSpringObject getXaso() {
		return xaso;
	}
}

class ElementValueMapVisitor extends DOMHelper.NodeMapVisitor {
	@Override
	public void visitNode(Context ctx, XAnnotatedMember xam, Node node, String key, Map<String, Object> result) {
		String val = node.getTextContent();
		if (val != null && val.length() > 0) {
			if (xam.trim)
				val = val.trim();

			Object object = XMapSpringUtil.getSpringObject(((XAnnotatedMapSpring) xam).componentType, val,
					((XAnnotatedMapSpring) xam).getXaso().getApplicationContext());
			if (object != null)
				result.put(key, object);
		}
	}
}

class AttributeValueMapVisitor extends DOMHelper.NodeMapVisitor {
	@Override
	public void visitNode(Context ctx, XAnnotatedMember xam, Node node, String key, Map<String, Object> result) {
		String val = node.getNodeValue();
		if (val != null && val.length() > 0) {
			if (xam.trim)
				val = val.trim();

			Object object = XMapSpringUtil.getSpringObject(((XAnnotatedMapSpring) xam).componentType, val,
					((XAnnotatedMapSpring) xam).getXaso().getApplicationContext());
			if (object != null)
				result.put(key, object);
		}
	}
}