package com.dianping.pigeon.component.xmap;

import java.io.IOException;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;

import com.dianping.pigeon.component.xmap.annotation.XContent;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;

public class XAnnotatedContent extends XAnnotatedMember {

	private static final OutputFormat DEFAULT_FORMAT = new OutputFormat();

	static {
		DEFAULT_FORMAT.setOmitXMLDeclaration(true);
		DEFAULT_FORMAT.setIndenting(true);
		DEFAULT_FORMAT.setMethod("xml");
		DEFAULT_FORMAT.setEncoding("UTF-8");
	}

	public XAnnotatedContent(XMap xmap, XSetter setter, XGetter getter, XContent anno) {
		super(xmap, setter, getter);
		this.path = new Path(anno.value());
		this.type = setter.getType();
		this.valueFactory = xmap.getValueFactory(this.type);
		this.xao = xmap.register(this.type);
	}

	@Override
	protected Object getValue(Context ctx, Element base) throws IOException {
		Element el = (Element) DOMHelper.getElementNode(base, path);
		if (el == null) {
			return null;
		}
		el.normalize();
		Node node = el.getFirstChild();
		if (node == null) {
			return "";
		}
		Range range = ((DocumentRange) el.getOwnerDocument()).createRange();
		range.setStartBefore(node);
		range.setEndAfter(el.getLastChild());
		DocumentFragment fragment = range.cloneContents();
		boolean asDOM = setter.getType() == DocumentFragment.class;
		return asDOM ? fragment : DOMSerializer.toString(fragment, DEFAULT_FORMAT);
	}

	@Override
	public void decode(Object instance, Node base, Document document, List<String> filters) throws Exception {
		// do nothing
	}

}
