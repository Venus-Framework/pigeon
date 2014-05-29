package com.dianping.pigeon.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class LangUtils {

	public static String toString(float value, int maxFactionDigits) {
		NumberFormat nf = DecimalFormat.getInstance();
		nf.setMaximumFractionDigits(maxFactionDigits);
		return nf.format(new BigDecimal(value).setScale(maxFactionDigits, BigDecimal.ROUND_HALF_UP));
	}
}
