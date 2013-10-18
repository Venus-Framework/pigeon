package com.dianping.pigeon.component.xmap.annotation.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;

import com.dianping.pigeon.component.xmap.XAnnotatedMember;
import com.dianping.pigeon.component.xmap.XAnnotatedObject;
import com.dianping.pigeon.component.xmap.XFieldGetter;
import com.dianping.pigeon.component.xmap.XFieldSetter;
import com.dianping.pigeon.component.xmap.XGetter;
import com.dianping.pigeon.component.xmap.XMap;
import com.dianping.pigeon.component.xmap.XMethodGetter;
import com.dianping.pigeon.component.xmap.XMethodSetter;
import com.dianping.pigeon.component.xmap.XSetter;
import com.dianping.pigeon.component.xmap.annotation.XMemberAnnotation;
import com.dianping.pigeon.component.xmap.annotation.XObject;
import com.dianping.pigeon.component.xmap.spring.XNodeListSpring;
import com.dianping.pigeon.component.xmap.spring.XNodeMapSpring;
import com.dianping.pigeon.component.xmap.spring.XNodeSpring;

public class XMapSpring extends XMap {

	public XAnnotatedObject register(Class<?> klass, ApplicationContext applicationContext) {
		XAnnotatedObject xao = objects.get(klass);
		if (xao == null) { // avoid scanning twice
			XObject xob = checkObjectAnnotation(klass, klass.getClassLoader());
			if (xob != null) {
				xao = new XAnnotatedSpringObject(this, klass, xob, applicationContext);
				objects.put(xao.klass, xao);
				scan(xao);
				String key = xob.value();
				if (key.length() > 0) {
					roots.put(xao.path.path, xao);
				}
			}
		}
		return xao;
	}

	@SuppressWarnings("rawtypes")
	private void scan(XAnnotatedObject xob) {
		Field[] fields = xob.klass.getDeclaredFields();
		for (Field field : fields) {
			Annotation anno = checkMemberAnnotation(field);
			if (anno != null) {
				XAnnotatedMember member = createFieldMember(field, anno);

				if (member == null) {
					member = createExtendFieldMember(field, anno, xob);
				}
				xob.addMember(member);
			}
		}

		Method[] methods = xob.klass.getDeclaredMethods();
		for (Method method : methods) {
			// we accept only methods with one parameter
			Class[] paramTypes = method.getParameterTypes();
			if (paramTypes.length != 1) {
				continue;
			}
			Annotation anno = checkMemberAnnotation(method);
			if (anno != null) {
				XAnnotatedMember member = createMethodMember(method, xob.klass, anno);

				if (member == null) {
					member = createExtendMethodMember(method, anno, xob);
				}
				xob.addMember(member);
			}
		}
	}

	private XAnnotatedMember createExtendFieldMember(Field field, Annotation annotation, XAnnotatedObject xob) {
		XSetter setter = new XFieldSetter(field);
		XGetter getter = new XFieldGetter(field);
		return createExtendMember(annotation, setter, getter, xob);
	}

	public final XAnnotatedMember createExtendMethodMember(Method method, Annotation annotation, XAnnotatedObject xob) {
		XSetter setter = new XMethodSetter(method);

		XGetter getter = new XMethodGetter(null, null, null);
		return createExtendMember(annotation, setter, getter, xob);
	}

	private XAnnotatedMember createExtendMember(Annotation annotation, XSetter setter, XGetter getter,
			XAnnotatedObject xob) {
		XAnnotatedMember member = null;
		int type = annotation.annotationType().getAnnotation(XMemberAnnotation.class).value();
		if (type == XMemberAnnotation.NODE_SPRING) {
			member = new XAnnotatedSpring(this, setter, getter, (XNodeSpring) annotation, (XAnnotatedSpringObject) xob);
		} else if (type == XMemberAnnotation.NODE_LIST_SPRING) {
			member = new XAnnotatedListSpring(this, setter, getter, (XNodeListSpring) annotation,
					(XAnnotatedSpringObject) xob);
		} else if (type == XMemberAnnotation.NODE_MAP_SPRING) {
			member = new XAnnotatedMapSpring(this, setter, getter, (XNodeMapSpring) annotation,
					(XAnnotatedSpringObject) xob);
		}
		return member;
	}
}
