package com.dianping.pigeon.component.xmap;

import java.util.ArrayList;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class Path {

	public static final String[] EMPTY_SEGMENTS = new String[0];

	public final String path;
	public String[] segments;
	public String attribute;

	public Path(String path) {
		this.path = path;
		parse(path);
	}

	@Override
	public String toString() {
		return path;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Path) {
			return ((Path) obj).path.equals(path);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	private void parse(String path) {
		ArrayList<String> seg = new ArrayList<String>();
		StringBuffer buf = new StringBuffer();
		char[] chars = path.toCharArray();
		boolean attr = false;
		for (char c : chars) {
			switch (c) {
			case '/':
				seg.add(buf.toString());
				buf.setLength(0);
				break;
			case '@':
				attr = true;
				seg.add(buf.toString());
				buf.setLength(0);
				break;
			default:
				buf.append(c);
				break;
			}
		}
		if (buf.length() > 0) {
			if (attr) {
				attribute = buf.toString();
			} else {
				seg.add(buf.toString());
			}
		}
		int size = seg.size();
		if (size == 1 && seg.get(0).length() == 0) {
			segments = EMPTY_SEGMENTS;
		} else {
			segments = seg.toArray(new String[size]);
		}
	}

}
