package org.classfoo.tools.jpa.impl;

import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Table;

import net.sf.cglib.beans.BeanMap;
import net.sf.cglib.proxy.Enhancer;

import org.classfoo.tools.jpa.JPAObject;
import org.classfoo.tools.jpa.JPASession;
import org.classfoo.tools.jpa.JPASessionFactory;
import org.classfoo.tools.jpa.JPATable;

/**
 * JPA工具类，提供基本的注解解析判断工具方法
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-6-27
 */
public class JPAUtil {

	/**
	 * 根据Entity注解获取JPATable对应的数据库表名称，如果注解的name没有设置，那么返回注解类的
	 * simple name
	 * @param annotatedClass
	 * @return
	 */
	public static String getTableName(Class<?> annotatedClass) {
		boolean hasTable = annotatedClass.isAnnotationPresent(Table.class);
		if (!hasTable) {
			return null;
		}
		Table table = annotatedClass.getAnnotation(Table.class);
		String tableName = table.name();
		if (tableName != null) {
			return tableName;
		}
		return annotatedClass.getSimpleName();
	}

	/**
	 * 根据Entity注解获取JPA唯一标识，如果注解的name没有设置，那么返回注解类的simple name
	 * @param annotatedClass
	 * @return
	 */
	public static String getEntityName(Class<?> annotatedClass) {
		boolean isEntity = annotatedClass.isAnnotationPresent(Entity.class);
		if (isEntity) {
			Entity entity = annotatedClass.getAnnotation(Entity.class);
			String entityname = entity.name();
			return (entityname == null || entityname.length() == 0) ? getClassName(annotatedClass) : entityname;
		}
		else {
			return getClassName(annotatedClass);
		}
	}

	/**
	 * 获取Class的名称，主要针对内部类进行处理，例如Namespace$PrimaryKey的读取
	 * 
	 * @param clazz
	 * @return
	 */
	public static String getClassName(Class<?> clazz) {
		String name = clazz.getName();
		int index = name.lastIndexOf(".");
		String classname = name.substring(index + 1, name.length());
		return classname;
	}

	/**
	 * 将hql的异常信息打印出来,以方便调试.
	 * TODO:这样的异常信息可以放入国际化中
	 * @param hql
	 * @param e
	 * @param params
	 */
	public static void throwSqlException(String sql, Exception e, Object... params) {
		StringBuilder info = new StringBuilder(50);
		info.append("执行SQL:").append(sql).append("出错,参数为[");
		if (params == null || params.length == 0) {
			info.append(']');
			throw new RuntimeException(info.toString(), e);
		}
		for (int i = 0; i < params.length; i++) {
			Object param = params[i];
			info.append(param == null ? "null" : param.toString());
			if (i != params.length - 1) {
				info.append(',');
			}
		}
		info.append(']');
		throw new RuntimeException(info.toString(), e);
	}

	/**
	 * 获取一个Map来访问Object内部属性，如果object为JPAObject的实现类，将直接返回，否则通过
	 * cglib的BeanMap进行包装后返回
	 * @param object
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final Map<String, Object> getObjectMap(Object object) {
		if (object instanceof JPAObject) {//对JPAObject做特殊处理
			return (JPAObject) object;
		}
		return BeanMap.create(object);
	}

	/**
	 * 通过object的class类型获取其JPA注册对象JPATable实例，如果尚未注册则抛出异常
	 * @param object
	 * @return
	 */
	public static final JPATable getJPATableFromObject(JPASessionFactory factory, Object object) {
		if (object instanceof JPAObject) {//对JPAObject特殊处理
			JPAObject jpaObject = (JPAObject) object;
			String name = jpaObject.getJPATable();
			return factory.getJPATable(name);
		}
		Class<?> type = object.getClass();
		JPATable jpaTable = factory.getJPATable(type);
		if (jpaTable == null) {
			Class<?> supertype = type.getSuperclass();
			jpaTable = factory.getJPATable(supertype);
			if (jpaTable != null) {
				return jpaTable;
			}
			throw new RuntimeException("待操作对象" + object + "的类型" + type + "没有注册到数据库会话工厂中！");
		}
		return jpaTable;
	}

	/**
	 * 创建对象实例，允许传入任意class类型，如果type是已经注册的JPA注解类，那么将会根据其是否有
	 * 延迟加载属性去创建相应的代理实例
	 * @param type
	 * @param session 可以为空，不为空且没有关闭时将尽量使用它去进行延迟加载
	 * @return
	 */
	public static final <T> T createInstance(Class<T> type, JPATable table, JPASessionFactory factory,
			JPASession session) {
		List<String> lazy = table.getLazyLoadFieldNames();
		if (lazy == null || lazy.isEmpty()) {
			return createInstance(table, type);
		}
		return createProxyInstance(table, type, factory, session);
	}

	/**
	 * 创建经过JPALazyLoadProxy包装的对象
	 * @param jpaTable
	 * @param type
	 * @param factory
	 * @param session
	 * @return
	 */
	private static <T> T createProxyInstance(JPATable jpaTable, Class<T> type, JPASessionFactory factory,
			JPASession session) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(type);
		enhancer.setCallbackType(JPALazyLoadProxy.class);
		JPALazyLoadProxy proxy = new JPALazyLoadProxy(factory.getName(), factory.getService(), factory, session);
		enhancer.setCallback(proxy);
		Object obj = enhancer.create();
		if (obj instanceof JPAObject) {
			((JPAObject) obj).setJPATable(jpaTable.getEntityName());
		}
		return type.cast(obj);
	}

	/**
	 * 直接通过type进行java实例创建
	 * @param type
	 * @return
	 */
	private static final <T> T createInstance(JPATable table, Class<T> type) {
		try {
			T instance = type.newInstance();
			if (instance instanceof JPAObject) {
				((JPAObject) instance).setJPATable(table.getEntityName());
			}
			return instance;
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
