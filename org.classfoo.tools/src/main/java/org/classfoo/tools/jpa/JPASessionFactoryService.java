package org.classfoo.tools.jpa;

import java.util.Collection;

/**
 * JPASessionFactory的服务类，用于获取一个JPASessionFactory的实例
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-4-19
 */
public interface JPASessionFactoryService {

	/**
	 * 获取给定名称的JPASessionFactory
	 * @param name
	 * @return
	 */
	public JPASessionFactory getJPASessionFactory(String name);

	/**
	 * 获取注册到JPASessionFactoryService中的所有的factory的名称列表
	 * @return
	 */
	public Collection<String> getNames();

	/**
	 * 删除某个特定的JPASessionFactory实例
	 * @param name
	 * @return
	 */
	public JPASessionFactory remove(String name);
}
