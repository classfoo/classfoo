package org.classfoo.tools.jpa.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.classfoo.tools.jpa.JPAField;
import org.classfoo.tools.jpa.JPAObject;
import org.classfoo.tools.jpa.JPASession;
import org.classfoo.tools.jpa.JPASessionFactory;
import org.classfoo.tools.jpa.JPASessionFactoryService;
import org.classfoo.tools.jpa.JPATable;

/**
 * JPA对象延迟加载的CGLIB的AOP代理类实现，在这个类中对get方法，且get的属性被加入@Lob注解，
 * 或者@Basic(fetch=lazy)注解时，将检测相应属性是否有值，如果没有，则发出sql查询填充值
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-7-9
 */
public class JPALazyLoadProxy implements MethodInterceptor, Serializable {

	private static final long serialVersionUID = -2197546239424757526L;

	private String jpaSessionFacoryName;

	/**
	 * XXX static处理sessionFactory可能存在隐患，但目前能够解决ehcache反序列化后延迟加载失效的
	 * 问题
	 */
	private static volatile JPASessionFactoryService sessionFactoryService;

	private transient JPASessionFactory sessionFactory;

	private transient JPASession session;

	public JPALazyLoadProxy(String jpaSessionFactoryName, JPASessionFactoryService service, JPASessionFactory sfactory,
			JPASession session) {
		sessionFactoryService = service;
		this.jpaSessionFacoryName = jpaSessionFactoryName;
		this.sessionFactory = sfactory;
		this.session = session;
	}

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		if (obj instanceof JPAObject) {
			return interceptJPAObject((JPAObject) obj, method, args, proxy);
		}
		Class<? extends Object> clazz = obj.getClass();
		JPATable jpaTable = this.getSessionFactory().getJPATable(clazz.getSuperclass());
		if (jpaTable == null) {
			return proxy.invokeSuper(obj, args);
		}

		String methodname = method.getName();
		if (!methodname.startsWith("get")) {//只对get方法做处理
			return proxy.invokeSuper(obj, args);
		}
		String fieldName = JPABeanUtils.getFieldName(method);
		JPAField field = jpaTable.getField(fieldName);
		if (field == null || !field.isLazyLoad()) {//属性不存在，属性不是延迟加载，直接调用父类get方法
			Object value = proxy.invokeSuper(obj, args);
			return value;
		}
		Object value = proxy.invokeSuper(obj, args);
		if (value != null) {//如果已经存在值，那么不查询数据库表
			return value;
		}
		//查询sql，加载相关内容，并调用set方法设置值
		Map<String, Object> bean = JPAUtil.getObjectMap(obj);
		String sql = this.createLazyLoadSql(bean, jpaTable, fieldName);
		Object[] params = this.createLazyLoadParams(bean, jpaTable);
		Class<?> type = field.getFieldType();
		if (this.session != null && this.session.isOpen()) {//根据当前session是否关闭判断
			Object result = session.executeQueryUnique(type, sql, params);
			runSetMethod(obj, methodname, type, result);
			return result;
		}
		//有些情况下不存在sesion，或者session关闭后调用get方法
		JPASession session = this.getSessionFactory().openSession();
		try {
			Object result = session.executeQueryUnique(type, sql, params);
			runSetMethod(obj, methodname, type, result);
			return result;
		}
		finally {
			session.close();
		}
	}

	/**
	 * 调用JPAObject的方法进行延迟加载
	 * @param obj
	 * @param method
	 * @param args
	 * @param proxy
	 * @return
	 * @throws Throwable
	 */
	private Object interceptJPAObject(JPAObject obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		if (!"get".equals(method.getName())) {
			return proxy.invokeSuper(obj, args);
		}
		JPATable table = this.getSessionFactory().getJPATable(obj.getJPATable());
		JPAField field = table.getField((String) args[0]);
		if (field == null || !field.isLazyLoad()) {//属性不存在，属性不是延迟加载，直接调用父类get方法
			return proxy.invokeSuper(obj, args);
		}
		Object value = proxy.invokeSuper(obj, args);
		if (value != null) {//如果已经存在值，那么不查询数据库表
			return value;
		}
		String fieldName = field.getName();
		if (obj.containsKey(fieldName)) {
			return null;
		}
		//查询sql，加载相关内容，并调用set方法设置值
		Map<String, Object> bean = JPAUtil.getObjectMap(obj);
		String sql = this.createLazyLoadSql(bean, table, fieldName);
		Object[] params = this.createLazyLoadParams(bean, table);
		Class<?> type = field.getFieldType();
		if (this.session != null && this.session.isOpen()) {//根据当前session是否关闭判断
			Object result = session.executeQueryUnique(type, sql, params);
			obj.put(fieldName, result);
			return result;
		}
		//有些情况下不存在sesion，或者session关闭后调用get方法
		JPASession session = this.getSessionFactory().openSession();
		try {
			Object result = session.executeQueryUnique(type, sql, params);
			obj.put(fieldName, result);
			return result;
		}
		finally {
			session.close();
		}
	}

	/**
	 * 获取JPASessionFactory对象，确保对象序列化后再反序列化回来时依旧能够访问到JPASessionFactory
	 * 进行数据库延迟加载相关操作
	 * @return
	 */
	private JPASessionFactory getSessionFactory() {
		if (this.sessionFactory != null) {
			return this.sessionFactory;
		}
		this.sessionFactory = sessionFactoryService.getJPASessionFactory(this.jpaSessionFacoryName);
		return this.sessionFactory;
	}

	/**
	 * 执行对属性的set方法，cglib和反射不能直接访问私有属性，将需要通过set方法来在进行延迟加载后
	 * 把值赋给相关私有属性。由于执行get方法可能调用set，因此set中不能够调用get，以免导致死循环
	 * @param obj
	 * @param getMethodName
	 * @param paramType
	 * @param paramValue
	 */
	private void runSetMethod(Object obj, String getMethodName, Class<?> paramType, Object paramValue) {
		String setMethodName = 's' + getMethodName.substring(1);
		try {
			Method setMethod = obj.getClass().getMethod(setMethodName, paramType);
			if (setMethod != null) {
				setMethod.invoke(obj, paramValue);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("对象" + obj + "的set方法无法执行！", e);
		}
	}

	/**
	 * 从JPA中提取主键属性值列表，以用于查询JPA延迟加载sql
	 * @param bean
	 * @param jpaTable
	 * @return
	 */
	private Object[] createLazyLoadParams(Map<String, Object> bean, JPATable jpaTable) {
		List<String> pknames = jpaTable.getPrimaryFieldNames();
		if (pknames == null || pknames.isEmpty()) {
			pknames = jpaTable.getFieldNames();
		}
		int pksize = pknames.size();
		Object[] params = new Object[pksize];
		for (int i = 0; i < pksize; i++) {
			String pkname = pknames.get(i);
			Object value = bean.get(pkname);
			params[i] = value;
		}
		return params;
	}

	/**
	 * 创建延迟加载JPA实例属性的sql
	 * @param bean
	 * @param jpaTable
	 * @param fieldName
	 * @return
	 */
	private String createLazyLoadSql(Map<String, Object> bean, JPATable jpaTable, String fieldName) {
		StringBuilder sql = new StringBuilder(100);
		JPAField selField = jpaTable.getField(fieldName);
		sql.append("select ").append(selField.getColumnName());
		sql.append(" from ").append(jpaTable.getTableName());
		sql.append(" where ");
		List<String> pknames = jpaTable.getPrimaryFieldNames();
		if (pknames == null || pknames.isEmpty()) {
			pknames = jpaTable.getFieldNames();
		}
		int pksize = pknames.size();
		for (int i = 0; i < pksize; i++) {
			String pkname = pknames.get(i);
			JPAField pkField = jpaTable.getField(pkname);
			sql.append(pkField.getColumnName()).append("=?");
			if (i != pksize - 1) {
				sql.append(" and ");
			}
		}
		return sql.toString();
	}

}
