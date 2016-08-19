package com.dianping.pigeon.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class LangUtils {

	public static String toString(float value, int maxFactionDigits) {
		NumberFormat nf = DecimalFormat.getInstance();
		nf.setMaximumFractionDigits(maxFactionDigits);
		return nf.format(BigDecimal.valueOf(value).setScale(maxFactionDigits, BigDecimal.ROUND_HALF_UP));
	}

	public static int hash(String str, int mid, int range) {
		int hash, i;
		for (hash = str.length(), i = 0; i < str.length(); ++i) {
			hash = (hash << 4) ^ (hash >> 28) ^ str.charAt(i);
		}
		return mid + (hash % range);
	}

	public static String getFullStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		try {
			String str = sw.getBuffer().toString();
			return str;
		} finally {
			pw.close();
		}
	}
}
