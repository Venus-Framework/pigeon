package com.dianping.pigeon.component.xmap;

import java.util.Collection;
import java.util.Iterator;

public final class PrimitiveArrays {

	// Utility class.
	private PrimitiveArrays() {
	}

	public static Object toPrimitiveArray(Collection<Object> col, Class<?> primitiveArrayType) {
		if (primitiveArrayType == Integer.TYPE) {
			return toIntArray(col);
		} else if (primitiveArrayType == Long.TYPE) {
			return toLongArray(col);
		} else if (primitiveArrayType == Double.TYPE) {
			return toDoubleArray(col);
		} else if (primitiveArrayType == Float.TYPE) {
			return toFloatArray(col);
		} else if (primitiveArrayType == Boolean.TYPE) {
			return toBooleanArray(col);
		} else if (primitiveArrayType == Byte.TYPE) {
			return toByteArray(col);
		} else if (primitiveArrayType == Character.TYPE) {
			return toCharArray(col);
		} else if (primitiveArrayType == Short.TYPE) {
			return toShortArray(col);
		}
		return null;
	}

	public static int[] toIntArray(Collection<Object> col) {
		int size = col.size();
		int[] ar = new int[size];
		Iterator<Object> it = col.iterator();
		int i = 0;
		while (it.hasNext()) {
			ar[i++] = (Integer) it.next();
		}
		return ar;
	}

	public static long[] toLongArray(Collection<Object> col) {
		int size = col.size();
		long[] ar = new long[size];
		Iterator<Object> it = col.iterator();
		int i = 0;
		while (it.hasNext()) {
			ar[i++] = (Long) it.next();
		}
		return ar;
	}

	public static double[] toDoubleArray(Collection<Object> col) {
		int size = col.size();
		double[] ar = new double[size];
		Iterator<Object> it = col.iterator();
		int i = 0;
		while (it.hasNext()) {
			ar[i++] = (Double) it.next();
		}
		return ar;
	}

	public static float[] toFloatArray(Collection<Object> col) {
		int size = col.size();
		float[] ar = new float[size];
		Iterator<Object> it = col.iterator();
		int i = 0;
		while (it.hasNext()) {
			ar[i++] = (Float) it.next();
		}
		return ar;
	}

	public static boolean[] toBooleanArray(Collection<Object> col) {
		int size = col.size();
		boolean[] ar = new boolean[size];
		Iterator<Object> it = col.iterator();
		int i = 0;
		while (it.hasNext()) {
			ar[i++] = (Boolean) it.next();
		}
		return ar;
	}

	public static short[] toShortArray(Collection<Object> col) {
		int size = col.size();
		short[] ar = new short[size];
		Iterator<Object> it = col.iterator();
		int i = 0;
		while (it.hasNext()) {
			ar[i++] = (Short) it.next();
		}
		return ar;
	}

	public static byte[] toByteArray(Collection<Object> col) {
		int size = col.size();
		byte[] ar = new byte[size];
		Iterator<Object> it = col.iterator();
		int i = 0;
		while (it.hasNext()) {
			ar[i++] = (Byte) it.next();
		}
		return ar;
	}

	public static char[] toCharArray(Collection<Object> col) {
		int size = col.size();
		char[] ar = new char[size];
		Iterator<Object> it = col.iterator();
		int i = 0;
		while (it.hasNext()) {
			ar[i++] = (Character) it.next();
		}
		return ar;
	}

}
