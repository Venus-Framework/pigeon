/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.remoting.common.codec.protobuf;

import java.lang.reflect.Field;

import org.apache.commons.lang.SerializationException;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public class ProtobufHelper {

	public static Message toMessage(String serviceInterface, String methodName, Class<?>[] parameterTypes,
			Object[] paramters) {
		FileDescriptorProto.Builder fileDescriptorProtoBuilder = FileDescriptorProto.newBuilder();
		DescriptorProto.Builder messageProtoBuilder = DescriptorProto.newBuilder();
		String messageName = serviceInterface.replaceAll("[.]", "_") + "_" + methodName;
		messageProtoBuilder.setName(messageName);
		fileDescriptorProtoBuilder.addMessageType(messageProtoBuilder);
		FileDescriptorProto fileDescriptorProto = fileDescriptorProtoBuilder.build();
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> type = parameterTypes[i];
			String fieldName = type.getName();
			FieldDescriptorProto.Builder fieldBuilder = FieldDescriptorProto.newBuilder();
			messageProtoBuilder.addField(fieldBuilder.setName(fieldName).setNumber(i + 1).setTypeName(type.getName())
					.build());
		}
		FileDescriptor fileDescriptor;
		try {
			fileDescriptor = FileDescriptor.buildFrom(fileDescriptorProto, new FileDescriptor[0]);
		} catch (DescriptorValidationException e) {
			throw new SerializationException(e);
		}
		DynamicMessage.Builder message = DynamicMessage.newBuilder(fileDescriptor.findMessageTypeByName(messageName));
		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> type = parameterTypes[i];
			String fieldName = type.getName();
			Object value = valueOf(serviceInterface, methodName, paramters[i]);
			if (value != null) {
				message.setField(fileDescriptor.findMessageTypeByName(messageName).findFieldByName(fieldName), value);
			}
		}
		return message.build();
	}

	public static Object valueOf(String serviceInterface, String methodName, Object value) {
		if (value == null) {
			return null;
		}
		Class<?> type = value.getClass();
		if (type == double.class || type == Double.class) {
			return value;
		} else if (type == float.class || type == Float.class) {
			return value;
		} else if (type == long.class || type == Long.class) {
			return value;
		} else if (type == int.class || type == Integer.class) {
			return value;
		} else if (type == boolean.class || type == Boolean.class) {
			return value;
		} else if (type == String.class) {
			return value;
		} else if (type == byte.class || type == Byte.class) {
			return FieldDescriptorProto.Type.TYPE_BYTES;
		} else if (type.isEnum()) {
			return value;
		} else {
			Field[] fields = value.getClass().getFields();
			Class<?>[] parameterTypes = new Class<?>[fields.length];
			Object[] values = new Object[fields.length];
			for (int i = 0; i < fields.length; i++) {
				parameterTypes[i] = fields[i].getType();
				try {
					values[i] = fields[i].get(value);
				} catch (IllegalArgumentException e) {
					throw new SerializationException(e);
				} catch (IllegalAccessException e) {
					throw new SerializationException(e);
				}
			}
			return toMessage(serviceInterface, methodName, parameterTypes, values);
		}
	}

	public static Type typeOf(Class<?> type) {
		if (type == double.class || type == Double.class) {
			return FieldDescriptorProto.Type.TYPE_DOUBLE;
		} else if (type == float.class || type == Float.class) {
			return FieldDescriptorProto.Type.TYPE_FLOAT;
		} else if (type == long.class || type == Long.class) {
			return FieldDescriptorProto.Type.TYPE_INT64;
		} else if (type == int.class || type == Integer.class) {
			return FieldDescriptorProto.Type.TYPE_INT32;
		} else if (type == boolean.class || type == Boolean.class) {
			return FieldDescriptorProto.Type.TYPE_BOOL;
		} else if (type == String.class) {
			return FieldDescriptorProto.Type.TYPE_STRING;
		} else if (type == byte.class || type == Byte.class) {
			return FieldDescriptorProto.Type.TYPE_BYTES;
		} else if (type.isEnum()) {
			return FieldDescriptorProto.Type.TYPE_ENUM;
		} else {
			return FieldDescriptorProto.Type.TYPE_MESSAGE;
		}
	}
}
