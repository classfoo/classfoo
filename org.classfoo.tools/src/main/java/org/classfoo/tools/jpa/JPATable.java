package org.classfoo.tools.jpa;

import java.util.List;

/**
 * JPA Entity注解信息封装，一个JPATable对应了一个Entity注解修饰的JPA注解类，通常它也对应了一张
 * 数据库表
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-6-27
 */
public interface JPATable {

	/**
	 * 获取通过Entity注解设置的JPA注解类唯一标识，如果注解中没有设置值，那么将使用类名作为唯一标
	 * 识
	 * @return
	 */
	public String getEntityName();

	/**
	 * 获取通过Table注解设置的数据库表名称，如果没有注解，或者注解中没有设置值，那么将使用类名
	 * 作为表名
	 * @return
	 */
	public String getTableName();

	/**
	 * 获取通过Column注解修饰的属性名称列表
	 * @return
	 */
	public List<String> getFieldNames();

	/**
	 * 获取被Id注解修饰的属性名称列表
	 * @return
	 */
	public List<String> getPrimaryFieldNames();
	
	/**
	 * 获取被Lob注解修饰的属性名称列表
	 * @return
	 */
	public List<String> getLobFieldNames();
	
	/**
	 * 获取被Basic注解修饰并设置fetch=lazy的属性名称列表
	 * @return
	 */
	public List<String> getLazyLoadFieldNames();

	/**
	 * 获取被Version注解修饰的属性名称
	 * @return
	 */
	public String getVersionFieldName();

	/**
	 * 通过属性名称获取JPAField对象
	 * @param name
	 * @return
	 */
	public JPAField getField(String name);

	/**
	 * 通过属性字段名称获取JPAField对象
	 * @param name
	 * @return
	 */
	public JPAField getFieldByColumnName(String name);

	/**
	 * 获取JPATable对应的注解类
	 * @return
	 */
	public Class<?> getJPAType();
}
