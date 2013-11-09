package org.classfoo.tools.jpa;

import java.util.Map;

/**
 * 一个能够通过JPASession进行读写的对象，能够支持任意的数据库表的读写
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-4-20
 */
public interface JPAObject extends Map<String, Object>, Cloneable {

	/**
	 * 获取JPATable对象，它代表了JPAObject的类型，以及其绑定的数据库表
	 * @return
	 */
	public String getJPATable();

	/**
	 * 设置JPATable对象，它代表了JPAObject的类型，以及其绑定的数据库表
	 * @param table
	 */
	public void setJPATable(String table);

	/**
	 * 克隆JPATable对象
	 * @return
	 */
	public JPAObject clone();
}
