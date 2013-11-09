package org.classfoo.tools.jpa.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.springframework.beans.BeanUtils;

/**
 * 这个类中封装了一些javaben的操作工具方法，主要用于处理经过cglib处理后的java bean操作，因为cglib
 * 在代理时一方面会将java bean的Class变化，一方面会在内部插入一些特殊属性
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-7-6
 */
public class JPABeanUtils {

	private static final String CALLBACKS = "callbacks";

	/**
	 * 判断object是否为type的实现，兼顾object是cglib代理类情况
	 * @param object
	 * @param type
	 * @return
	 */
	public static final boolean instanceOf(Object object, Class<?> type) {
		Class<?> clazz = object.getClass();
		if (clazz.equals(type)) {
			return true;
		}
		String clazzname = clazz.getSimpleName();
		int index = clazzname.indexOf("$$EnhancerByCGLIB$$");
		if (index == -1) {
			return false;
		}
		String shortname = clazzname.substring(0, index);
		return type.getSimpleName().equals(shortname);
	}

	/**
	 * 通过方法对象获取其对应的set，get的类属性名称
	 * @param method
	 * @return
	 */
	public static String getFieldName(Method method) {
		PropertyDescriptor p = BeanUtils.findPropertyForMethod(method);
		String fieldName = p.getName();
		return fieldName;
	}

//	@SuppressWarnings("unchecked")
//	public static Collection<String> getBeanMapKeySet(Object map) {
//		if (map instanceof JPAObject) {//如果为JPAObject，直接通过JPATable获取值
//
//		}
//		Set<String> keyset = map.keySet();
//		boolean hasCallBack = keyset.contains(CALLBACKS);
//		if (!hasCallBack) {
//			return keyset;
//		}
//		Class<?> type = map.getPropertyType(CALLBACKS);
//		if (!Callback[].class.isAssignableFrom(type)) {
//			return keyset;
//		}
//		int size = keyset.size() - 1;
//		if (size == 0) {
//			return Collections.emptySet();
//		}
//		ArrayList<String> list = new ArrayList<String>(size);
//		Iterator<String> it = keyset.iterator();
//		while (it.hasNext()) {
//			String key = it.next();
//			if (CALLBACKS.equals(key)) {
//				continue;
//			}
//			list.add(key);
//		}
//		return list;
//	}
}
