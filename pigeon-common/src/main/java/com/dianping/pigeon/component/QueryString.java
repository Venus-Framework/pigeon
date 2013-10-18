package com.dianping.pigeon.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.util.StringUtils;

public class QueryString {

	public static final String PREFIX = "?";
	public static final String PREFIX_REGEXP = "\\?";
	public static final String PARAMETER_DELIMITER = "&";
	public static final String KEY_VALUE_DELIMITER = "=";

	private SortedMap<String, String> parameters = new TreeMap<String, String>();

	public QueryString() {
	}

	public QueryString(String queryString) {
		String[] segments = queryString.split(PARAMETER_DELIMITER);
		for (String s : segments) {
			if (StringUtils.hasText(s)) {
				String[] parts = s.split(KEY_VALUE_DELIMITER, 2);
				addParameter(parts[0], parts[1]);
			}
		}
	}

	public String toString() {
		Collection<String> buffer = new ArrayList<String>();
		for (Map.Entry<String, String> parameter : parameters.entrySet()) {
			buffer.add(parameter.getKey() + KEY_VALUE_DELIMITER + parameter.getValue());
		}
		return StringUtils.collectionToDelimitedString(buffer, PARAMETER_DELIMITER);
	}

	public QueryString addParameter(String key, String value) {
		if (key == null || value == null) {
			throw new IllegalArgumentException("key and/or value cannot be null");
		}
		parameters.put(key, value);
		return this;
	}

	public String getParameter(String key) {
		return (String) parameters.get(key);
	}

	public void removeParameter(String key) {
		parameters.remove(key);
	}

	public boolean isEmpty() {
		return parameters.isEmpty();
	}
}
