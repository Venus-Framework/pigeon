package com.dianping.pigeon.remoting.common.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.dianping.pigeon.remoting.common.util.StringizerUtils.Reflects.IMemberFilter;

public class StringizerUtils {
	public static JsonStringizer forJson() {
		return JsonStringizer.DEFAULT;
	}

	public static enum JsonStringizer {
		DEFAULT(false),

		COMPACT(true);

		private boolean m_compact;

		private JsonStringizer(boolean compact) {
			m_compact = compact;
		}

		public JsonStringizer compact() {
			return COMPACT;
		}

		public String from(Object obj) {
			return from(obj, 0, 0);
		}

		public String from(Object obj, int maxLength, int maxItemLength) {
			StringBuilder sb = new StringBuilder(1024);
			LengthLimiter limiter = new LengthLimiter(sb, maxLength,
					maxItemLength);
			Set<Object> done = new HashSet<Object>();

			try {
				fromObject(done, limiter, obj);
			} catch (RuntimeException e) {
				// expected
				sb.append("...");
			}

			return sb.toString();
		}

		private void fromArray(Set<Object> done, LengthLimiter sb, Object obj) {
			int len = Array.getLength(obj);

			sb.append('[');

			for (int i = 0; i < len; i++) {
				if (i > 0) {
					sb.append(',');

					if (!m_compact) {
						sb.append(' ');
					}
				}

				Object element = Array.get(obj, i);

				fromObject(done, sb, element);
			}

			sb.append(']');
		}

		@SuppressWarnings("unchecked")
		private void fromCollection(Set<Object> done, LengthLimiter sb,
				Object obj) {
			boolean first = true;

			sb.append('[');

			for (Object item : ((Collection<Object>) obj)) {
				if (first) {
					first = false;
				} else {
					sb.append(',');

					if (!m_compact) {
						sb.append(' ');
					}
				}

				fromObject(done, sb, item);
			}

			sb.append(']');
		}

		@SuppressWarnings("unchecked")
		private void fromMap(Set<Object> done, LengthLimiter sb, Object obj) {
			boolean first = true;

			sb.append('{');

			for (Map.Entry<Object, Object> e : ((Map<Object, Object>) obj)
					.entrySet()) {
				Object key = e.getKey();
				Object value = e.getValue();

				if (value == null) {
					continue;
				}

				if (first) {
					first = false;
				} else {
					sb.append(',');

					if (!m_compact) {
						sb.append(' ');
					}
				}

				sb.append('"').append(key).append("\":");

				if (!m_compact) {
					sb.append(' ');
				}

				fromObject(done, sb, value);
			}

			sb.append('}');
		}

		private void fromObject(Set<Object> done, LengthLimiter sb, Object obj) {
			if (obj == null) {
				return;
			}

			Class<?> type = obj.getClass();

			if (type == String.class) {
				sb.append('"').append(obj.toString(), true).append('"');
			} else if (type.isPrimitive()
					|| Number.class.isAssignableFrom(type) || type.isEnum()) {
				sb.append(obj.toString(), true);
			} else if (type == Boolean.class) {
				sb.append(obj.toString(), true);
			} else if (type == Date.class) {
				sb.append('"')
						.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(obj),
								true).append('"');
			} else if (type == Class.class) {
				sb.append('"').append(obj, true).append('"');
			} else if (done.contains(obj)) {
				sb.append("{}");
				return;
			} else {
				done.add(obj);

				if (type.isArray()) {
					fromArray(done, sb, obj);
				} else if (Collection.class.isAssignableFrom(type)) {
					fromCollection(done, sb, obj);
				} else if (Map.class.isAssignableFrom(type)) {
					fromMap(done, sb, obj);
				} else {
					fromPojo(done, sb, obj);
				}
			}
		}

		private void fromPojo(Set<Object> done, LengthLimiter sb, Object obj) {
			Class<? extends Object> type = obj.getClass();

			if (hasToString(type)) {
				fromObject(done, sb, obj.toString());
				return;
			}

			List<Method> getters = Reflects.forMethod().getMethods(type,
					new IMemberFilter<Method>() {
						@Override
						public boolean filter(Method method) {
							return Reflects.forMethod().isGetter(method);
						}
					});

			Collections.sort(getters, new Comparator<Method>() {
				@Override
				public int compare(Method m1, Method m2) {
					return m1.getName().compareTo(m2.getName());
				}
			});

			if (getters.isEmpty()) {
				// use java toString() since we can't handle it
				sb.append(obj.toString());
			} else {
				boolean first = true;

				sb.append('{');

				for (Method getter : getters) {
					String key = Reflects.forMethod().getGetterName(getter);
					Object value;

					try {
						if (!getter.isAccessible()) {
							getter.setAccessible(true);
						}

						value = getter.invoke(obj);
					} catch (Exception e) {
						// ignore it
						value = null;
					}

					if (value == null) {
						continue;
					}

					if (first) {
						first = false;
					} else {
						sb.append(',');

						if (!m_compact) {
							sb.append(' ');
						}
					}

					sb.append('"').append(key).append("\":");

					if (!m_compact) {
						sb.append(' ');
					}

					fromObject(done, sb, value);
				}

				sb.append('}');
			}
		}

		public boolean hasToString(Class<?> type) {
			try {
				Method method = type.getMethod("toString");

				if (method.getDeclaringClass() != Object.class) {
					return true;
				}
			} catch (Exception e) {
				// ignore it
			}

			return false;
		}
	}

	static class LengthLimiter {
		private int m_maxLength;
		private int m_maxItemLength;
		private int m_halfMaxItemLength;
		private StringBuilder m_sb;

		public LengthLimiter(StringBuilder sb, int maxLength, int maxItemLength) {
			m_sb = sb;
			m_maxLength = maxLength - 3;
			m_maxItemLength = maxItemLength;
			m_halfMaxItemLength = maxItemLength / 2 - 1;
		}

		public LengthLimiter append(char ch) {
			m_sb.append(ch);
			return this;
		}

		public LengthLimiter append(Object value) {
			append(value, false);
			return this;
		}

		public LengthLimiter append(Object value, boolean itemLimit) {
			int len = m_sb.length();
			String str = getString(value, itemLimit);

			if (m_maxLength > 0 && len + str.length() > m_maxLength) {
				throw new RuntimeException("Length limited.");
			} else {
				m_sb.append(str);
				return this;
			}
		}

		private String getString(Object value, boolean itemLimit) {
			String str = String.valueOf(value);

			if (itemLimit && m_maxItemLength > 0) {
				int len = str.length();

				if (len > m_maxItemLength) {
					return str.substring(0, m_halfMaxItemLength) + "..."
							+ str.substring(len - m_halfMaxItemLength, len);
				}
			}

			return str;
		}
	}

	public static class Reflects {
		private Reflects() {
		}

		public static ClassReflector forClass() {
			return ClassReflector.INSTANCE;
		}

		public static ConstructorReflector forConstructor() {
			return ConstructorReflector.INSTANCE;
		}

		public static FieldReflector forField() {
			return FieldReflector.INSTANCE;
		}

		public static MethodReflector forMethod() {
			return MethodReflector.INSTANCE;
		}

		public static ModifierReflector forModifier() {
			return ModifierReflector.INSTANCE;
		}

		public static ResourceReflector forResource() {
			return ResourceReflector.INSTANCE;
		}

		public static enum ClassReflector {
			INSTANCE;

			/**
			 * for class name like "a.b.C" or "a.b.C$D$E"
			 * 
			 * @param className
			 * @return class from current context class loader
			 */
			public Class<?> getClass(String className) {
				return getClass(className, null);
			}

			/**
			 * for class name like "a.b.C" or "a.b.C$D$E"
			 * 
			 * @param className
			 * @param classloader
			 * @return class from current context class loader
			 */
			public Class<?> getClass(String className, ClassLoader classloader) {
				Class<?> clazz = null;

				if (classloader != null) {
					try {
						clazz = classloader.loadClass(className);
					} catch (ClassNotFoundException e) {
						// ignore it
					}
				} else {
					// step1: try to load from caller class loader
					try {
						clazz = Class.forName(className);
					} catch (ClassNotFoundException e) {
						// step2: try to load from thread context class loader
						if (clazz == null) {
							clazz = getClass(className, Thread.currentThread()
									.getContextClassLoader());
						}

						// step3: try to load from current-class class loader
						if (clazz == null) {
							clazz = getClass(className,
									Reflects.class.getClassLoader());
						}
					}
				}

				return clazz;
			}

			/**
			 * for class name like "a.b.C" or "a.b.C.D.E"
			 * 
			 * @param className
			 * @return class from current context class loader
			 */
			public Class<?> getClass2(String className) {
				return getClass2(className, null);
			}

			/**
			 * for class name like "a.b.C" or "a.b.C.D.E"
			 * 
			 * @param className
			 * @return class from current context class loader
			 */
			public Class<?> getClass2(String className, ClassLoader classloader) {
				Class<?> clazz = null;
				String name = className;

				while (true) {
					clazz = getClass(name, classloader);

					if (clazz != null) {
						break;
					}

					// try with inner class name
					int pos = name.lastIndexOf('.');
					if (pos < 0) {
						break;
					}
					name = name.substring(0, pos) + '$'
							+ name.substring(pos + 1);
				}

				return clazz;
			}

			public Class<?> getNestedClass(Class<?> clazz, String simpleName) {
				if (clazz != null) {
					Class<?>[] subClasses = clazz.getDeclaredClasses();

					if (subClasses != null) {
						for (Class<?> subClass : subClasses) {
							if (subClass.getSimpleName().equals(simpleName)) {
								return subClass;
							}
						}
					}
				}

				return null;
			}

			public Class<?> getNestedClass(String className, String simpleName) {
				return getNestedClass(getClass(className), simpleName);
			}

			public Class<?> getNestedClass(String className, String simpleName,
					ClassLoader classloader) {
				return getNestedClass(getClass(className, classloader),
						simpleName);
			}
		}

		public static enum ConstructorReflector {
			INSTANCE;

			public Object createInstance(Class<?> clazz,
					Object... typesAndParameters) {
				try {
					TypeArguments typeArgs = new TypeArguments(
							typesAndParameters);

					Constructor<?> constructor = clazz.getConstructor(typeArgs
							.getTypes());

					return constructor.newInstance(typeArgs.getArguments());
				} catch (Exception e) {
					// ignore it
				}

				return null;
			}
		}

		public static enum FieldFilter implements IMemberFilter<Field> {
			PUBLIC {
				public boolean filter(Field field) {
					return ModifierReflector.INSTANCE.isPublic(field);
				}
			},

			STATIC {
				public boolean filter(Field field) {
					return ModifierReflector.INSTANCE.isStatic(field);
				}
			},

			PUBLIC_STATIC {
				public boolean filter(Field field) {
					return ModifierReflector.INSTANCE.isPublic(field)
							&& ModifierReflector.INSTANCE.isStatic(field);
				}
			};
		}

		public static enum FieldReflector {
			INSTANCE;

			public List<Field> getAllDeclaredFields(Class<?> clazz,
					IMemberFilter<Field> filter) {
				List<Field> list = new ArrayList<Field>();
				Class<?> current = clazz;

				while (current != null && current != Object.class) {
					Field[] fields = current.getDeclaredFields();

					for (Field field : fields) {
						if (filter == null || filter.filter(field)) {
							list.add(field);
						}
					}

					current = current.getSuperclass();
				}

				return list;
			}

			public Field getDeclaredField(Class<?> clazz, String fieldName) {
				if (clazz != null) {
					try {
						Field field = clazz.getDeclaredField(fieldName);

						return field;
					} catch (Exception e) {
						// ignore
					}
				}

				return null;
			}

			public List<Field> getDeclaredFields(Class<?> clazz,
					IMemberFilter<Field> filter) {
				List<Field> list = new ArrayList<Field>();
				Field[] fields = clazz.getDeclaredFields();

				for (Field field : fields) {
					if (filter == null || filter.filter(field)) {
						list.add(field);
					}
				}

				return list;
			}

			@SuppressWarnings("unchecked")
			public <T> T getDeclaredFieldValue(Class<?> clazz,
					String fieldName, Object instance) {
				Field field = getDeclaredField(clazz, fieldName);

				if (field != null) {
					try {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}

						return (T) field.get(instance);
					} catch (Exception e) {
						// ignore
					}
				}

				return null;
			}

			public boolean setDeclaredFieldValue(Class<?> clazz,
					String fieldName, Object instance, Object value) {
				Field field = getDeclaredField(clazz, fieldName);

				if (field != null) {
					try {
						if (!field.isAccessible()) {
							field.setAccessible(true);
						}

						field.set(instance, value);
						return true;
					} catch (Exception e) {
						// ignore
					}
				}

				return false;
			}

			public List<Field> getFields(Class<?> clazz,
					IMemberFilter<Field> filter) {
				List<Field> list = new ArrayList<Field>();
				Field[] fields = clazz.getFields();

				for (Field field : fields) {
					if (filter == null || filter.filter(field)) {
						list.add(field);
					}
				}

				return list;
			}

			@SuppressWarnings("unchecked")
			public <T> T getFieldValue(Object instance, String fieldName) {
				if (instance != null) {
					try {
						Field field = instance.getClass().getField(fieldName);

						return (T) field.get(instance);
					} catch (Exception e) {
						// ignore
					}
				}

				return null;
			}

			@SuppressWarnings("unchecked")
			public <T> T getStaticFieldValue(Class<?> clazz, String fieldName) {
				if (clazz != null) {
					try {
						Field field = clazz.getField(fieldName);

						return (T) field.get(null);
					} catch (Exception e) {
						// ignore
					}
				}

				return null;
			}

			@SuppressWarnings("unchecked")
			public <T> T getStaticFieldValue(String className, String fieldName) {
				try {
					Class<?> clazz = forClass().getClass(className);

					if (clazz != null) {
						return (T) getStaticFieldValue(clazz, fieldName);
					}
				} catch (Exception e) {
					// ignore it
				}

				return null;
			}

			@SuppressWarnings("unchecked")
			public <T> T getDeclaredFieldValue(Object instance,
					String... fields) {
				Object value = instance;

				for (String field : fields) {
					value = getDeclaredFieldValue(value.getClass(), field,
							value);

					if (value == null) {
						break;
					}
				}

				return (T) value;
			}
		}

		public static interface IMemberFilter<T extends Member> {
			public boolean filter(T member);
		}

		public static enum MethodFilter implements IMemberFilter<Method> {
			PUBLIC {
				public boolean filter(Method method) {
					return ModifierReflector.INSTANCE.isPublic(method);
				}
			},

			STATIC {
				public boolean filter(Method method) {
					return ModifierReflector.INSTANCE.isStatic(method);
				}
			},

			PUBLIC_STATIC {
				public boolean filter(Method method) {
					return ModifierReflector.INSTANCE.isPublic(method)
							&& ModifierReflector.INSTANCE.isStatic(method);
				}
			};
		}

		public static enum MethodReflector {
			INSTANCE;

			public Method getDeclaredMethod(Class<?> clazz, String methodName,
					Class<?>... parameterTypes) {
				try {
					return clazz.getDeclaredMethod(methodName, parameterTypes);
				} catch (Exception e) {
					// ignore it
				}

				return null;
			}

			public List<Method> getDeclaredMethods(Class<?> clazz,
					IMemberFilter<Method> filter) {
				List<Method> list = new ArrayList<Method>();

				try {
					Method[] methods = clazz.getDeclaredMethods();
					for (Method method : methods) {
						if (filter == null || filter.filter(method)) {
							list.add(method);
						}
					}
				} catch (Exception e) {
					// ignore it
				}

				return list;
			}

			public String getGetMethodName(String propertyName) {
				int len = propertyName == null ? 0 : propertyName.length();

				if (len == 0) {
					throw new IllegalArgumentException(String.format(
							"Invalid property name: %s!", propertyName));
				}

				StringBuilder sb = new StringBuilder(len + 3);

				sb.append("get");
				sb.append(Character.toUpperCase(propertyName.charAt(0)));
				sb.append(propertyName.substring(1));

				return sb.toString();
			}

			public String getGetterName(Method method) {
				String name = method.getName();
				int length = name.length();

				if (length > 3 && name.startsWith("get")) {
					return Character.toLowerCase(name.charAt(3))
							+ name.substring(4);
				} else if (length > 2 && name.startsWith("is")) {
					return Character.toLowerCase(name.charAt(2))
							+ name.substring(3);
				} else {
					return name;
				}
			}

			public Method getMethod(Class<?> clazz, String methodName,
					Class<?>... parameterTypes) {
				try {
					return clazz.getMethod(methodName, parameterTypes);
				} catch (Exception e) {
					// ignore it
				}

				return null;
			}

			public List<Method> getMethods(Class<?> clazz,
					IMemberFilter<Method> filter) {
				List<Method> list = new ArrayList<Method>();

				try {
					Method[] methods = clazz.getMethods();
					for (Method method : methods) {
						if (filter == null || filter.filter(method)) {
							list.add(method);
						}
					}
				} catch (Exception e) {
					// ignore it
				}

				return list;
			}

			@SuppressWarnings("unchecked")
			public <T> T getPropertyValue(Object instance, String propertyName) {
				String methodName = getGetMethodName(propertyName);
				Object value = invokeMethod(instance, methodName);

				return (T) value;
			}

			@SuppressWarnings("unchecked")
			public <T> T invokeDeclaredMethod(Object instance,
					String methodName, Object... typesAndParameters) {
				if (instance == null) {
					return null;
				}

				TypeArguments typeArgs = new TypeArguments(typesAndParameters);
				Method method = getDeclaredMethod(instance.getClass(),
						methodName, typeArgs.getTypes());
				if (method != null) {
					try {
						method.setAccessible(true);
						return (T) method.invoke(instance,
								typeArgs.getArguments());
					} catch (Exception e) {
						// ignore it
					}
				}

				return null;
			}

			@SuppressWarnings("unchecked")
			public <T> T invokeMethod(Object instance, String methodName,
					Object... typesAndParameters) {
				if (instance == null) {
					return null;
				}

				TypeArguments typeArgs = new TypeArguments(typesAndParameters);
				Method method = getMethod(instance.getClass(), methodName,
						typeArgs.getTypes());
				if (method != null) {
					try {
						return (T) method.invoke(instance,
								typeArgs.getArguments());
					} catch (Exception e) {
						// ignore it
					}
				}

				return null;
			}

			@SuppressWarnings("unchecked")
			public <T> T invokeStaticMethod(Class<?> clazz, String methodName,
					Object... typesAndParameters) {
				if (clazz == null) {
					return null;
				}

				TypeArguments typeArgs = new TypeArguments(typesAndParameters);
				Method method = getMethod(clazz, methodName,
						typeArgs.getTypes());
				if (method != null) {
					try {
						return (T) method.invoke(null);
					} catch (Exception e) {
						// ignore it
					}
				}

				return null;
			}

			public boolean isGetter(Method method) {
				if (method.getParameterTypes().length > 0) {
					return false;
				}

				int modifier = method.getModifiers();

				if (!Modifier.isPublic(modifier) || Modifier.isStatic(modifier)) {
					return false;
				}

				String name = method.getName();

				if (name.startsWith("get") && !name.equals("getClass")) {
					return true;
				} else if (name.startsWith("is")
						&& method.getReturnType() == Boolean.TYPE) {
					return true;
				} else {
					return false;
				}
			}
		}

		public static enum ModifierReflector {
			INSTANCE;

			public boolean isAbstract(Class<?> clazz) {
				return Modifier.isAbstract(clazz.getModifiers());
			}

			public boolean isAbstract(Member member) {
				return Modifier.isAbstract(member.getModifiers());
			}

			public boolean isPublic(Class<?> clazz) {
				return Modifier.isPublic(clazz.getModifiers());
			}

			public boolean isPublic(Member member) {
				return Modifier.isPublic(member.getModifiers());
			}

			public boolean isStatic(Class<?> clazz) {
				return Modifier.isStatic(clazz.getModifiers());
			}

			public boolean isStatic(Member member) {
				return Modifier.isStatic(member.getModifiers());
			}
		}

		public static enum ResourceReflector {
			INSTANCE;

			public Properties getResource(Class<?> anchorClass, String resName) {
				// step1: try to load from current class loader
				Properties prop = getResource(getClass().getClassLoader(),
						anchorClass, resName);

				// step2: try to load from thread context class loader
				if (prop == null) {
					ClassLoader classloader = Thread.currentThread()
							.getContextClassLoader();

					prop = getResource(classloader, anchorClass, resName);
				}

				return prop;
			}

			public Properties getResource(ClassLoader classloader,
					Class<?> anchorClass, String resName) {
				resName = getResourceName(anchorClass, resName);

				URL url = classloader.getResource(resName);

				if (url != null) {
					try {
						Properties prop = new Properties();
						prop.load(url.openStream());
						return prop;
					} catch (IOException e) {
						// ignore it
					}
				}

				return null;
			}

			public Properties getResource(String resName) {
				return getResource(Reflects.class, resName);
			}

			private String getResourceName(Class<?> clazz, String resName) {
				// Turn package name into a directory path
				if (resName.length() > 0 && resName.charAt(0) == '/')
					return resName.substring(1);

				String qualifiedClassName = clazz != null ? clazz.getName()
						: getClass().getName();
				int classIndex = qualifiedClassName.lastIndexOf('.');
				if (classIndex == -1)
					return resName; // from a default package
				return qualifiedClassName.substring(0, classIndex + 1).replace(
						'.', '/')
						+ resName;
			}
		}

		static class TypeArguments {
			private Class<?>[] m_types;

			private Object[] m_arguments;

			public TypeArguments(Object... typesAndParameters) {
				int length = typesAndParameters.length;

				if (length % 2 != 0) {
					throw new IllegalArgumentException(String.format(
							"Constrcutor argument types and data should be even"
									+ ", but was odd: %s.", length));
				}

				int half = length / 2;
				Class<?>[] types = new Class<?>[half];
				Object[] arguments = new Object[half];

				for (int i = 0; i < half; i++) {
					types[i] = (Class<?>) typesAndParameters[2 * i];
					arguments[i] = typesAndParameters[2 * i + 1];
				}

				m_types = types;
				m_arguments = arguments;
			}

			public Object[] getArguments() {
				return m_arguments;
			}

			public Class<?>[] getTypes() {
				return m_types;
			}
		}
	}
}