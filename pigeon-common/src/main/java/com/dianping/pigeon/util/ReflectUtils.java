package com.dianping.pigeon.util;

import java.lang.reflect.Field;

public class ReflectUtils {

	public static Field getDeclaredField(Class cls, String fieldName, boolean forceAccess) {
		if (cls == null) {
			throw new IllegalArgumentException("The class must not be null");
		}
		if (fieldName == null) {
			throw new IllegalArgumentException("The field name must not be null");
		}
		try {
			// only consider the specified class by using getDeclaredField()
			Field field = cls.getDeclaredField(fieldName);
			if (forceAccess && !field.isAccessible()) {
				field.setAccessible(true);
			} else {
				return null;
			}
			return field;
		} catch (NoSuchFieldException e) {
		}
		return null;
	}

	public static Object readDeclaredField(Object target, String fieldName, boolean forceAccess)
			throws IllegalAccessException {
		if (target == null) {
			throw new IllegalArgumentException("target object must not be null");
		}
		Field field = getDeclaredField(target.getClass(), fieldName, forceAccess);
		if (field == null) {
			throw new IllegalArgumentException("Cannot locate declared field " + target.getClass().getName() + "."
					+ fieldName);
		}
		if (forceAccess && !field.isAccessible()) {
			field.setAccessible(true);
		}
		return field.get(target);
	}

	public static void writeDeclaredField(Object target, String fieldName, Object value, boolean forceAccess)
			throws IllegalAccessException {
		if (target == null) {
			throw new IllegalArgumentException("target object must not be null");
		}
		Field field = getDeclaredField(target.getClass(), fieldName, forceAccess);
		if (field == null) {
			throw new IllegalArgumentException("Cannot locate declared field " + target.getClass().getName() + "."
					+ fieldName);
		}
		if (forceAccess && !field.isAccessible()) {
			field.setAccessible(true);
		}
		field.set(target, value);
	}
}
