package org.classfoo.tools.jpa;

import java.util.List;


/**
 * JPA数据持久化会话工厂接口，这个接口提供了注册JPA注解类，以及开启数据库访问会话的功能。将通
 * 过spring bean方式供元数据使用.JPA数据持久化会话将使用默认连接池，将在实现类中自动处理默认连
 * 接池的设置
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-6-27
 */
public interface JPASessionFactory {

	/**
	 * 获取注册到JPASessionFactoryService后的名称
	 * @return
	 */
	public String getName();

	/**
	 * 获取单例的JPASessionFactoryService对象
	 * @return
	 */
	public JPASessionFactoryService getService();

	/**
	 * 设置JPASesionFactory基于的数据源，如果不设置将使用默认数据源。regist方法将会注册数据库表
	 * 到设置的数据源中。通常情况下设置数据源后不要去切换数据源
	 * @param datasource
	 */
	public void setDataSource(String datasource);

	/**
	 * 获取JPASessionFactory基于的数据源名称
	 * @return
	 */
	public String getDataSource();

	/**
	 * 将JPA注解类进行注册，注册过程中将进行如下处理：
	 * 1，根据JPA注解创建数据库表，或者修改已有数据库表结构
	 * 2，提取JPA信息，增加或者更新内存对该注解类结构分析的缓存
	 * 3，针对并发进行处理，注册操作完成后再通过并发锁定去进行新旧表替换，JPA信息更新等操作
	 * 
	 * 注册时需要能够处理JPA的部分注解，包括如下注解：
	 * 1, Entity:只有具有Entity注解的类才被视作JPA注解类，可以指定名称，用于唯一标识一个Entity
	 * 2, Table: 为JPA注解类指定数据库表名称
	 * 3, Column：为类属性指定表字段设置
	 * 4, Id：设置类属性为主键字段
	 * 5, Version：设置类属性为乐观锁字段
	 * 6, MappedSuperclass：用于为JPA注解类指定父类，存在此注解的父类也需要分析其属性的注解
	 * 7, Inheritance：指定JPA创建表方式，是多个类公用数据库表，还是每个类单独一张表
	 * 8, Transient：指定类属性不被处理为数据库表字段
	 * 9, Lob: 设置类属性为lob字段
	 * @param annotatedClasses
	 */
	public List<JPATable> regist(Class<?>... annotatedClasses);

	/**
	 * 开启对数据库的一个操作会话
	 * @return
	 */
	public JPASession openSession();
	
	/**
	 * 通过{@link #regist(Class...)}注册到JPASession中的Class会被解析注解，转换为内存结构信息存在
	 * 于内存中，以便在进行ORM操作时能够快速建立数据库表到对象的关系。通常一个注解类会被解析为
	 * 一个JPATable，这个方法提供了通过注解类Class对象获取已注册JPATable的功能，如果相关JPATable
	 * 不存在或者尚未注册，将返回null
	 * @param type
	 * @return
	 */
	public JPATable getJPATable(Class<?> type);
	public JPATable getJPATable(String name);
	
	public String[] getJPATableNames();
	
	public boolean isRegisted(Class<?> type);
	public boolean isRegisted(String name);

}
