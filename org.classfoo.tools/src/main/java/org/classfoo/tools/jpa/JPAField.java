package org.classfoo.tools.jpa;

/**
 * JPA属性注解信息封装，一个JPAField对应了一个Column注解修饰的注解类属性，也对应了相应数据库
 * 表中的一个字段
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-6-27
 */
public interface JPAField {
	
	/**
	 * 获取属性名称
	 * @return
	 */
	public String getName();
	
	/**
	 * 获取对应的JPA注解类属性的类型
	 * @return
	 */
	public Class<?> getFieldType();
	
	/**
	 * 获取数据库表字段名称
	 * @return
	 */
	public String getColumnName();
	
	/**
	 * 获取字段长度
	 * @return
	 */
	public int getColumnLength();

	/**
	 * 获取字段类型，将是{@link java.sql.Types}
	 * @return
	 */
	public char getColumnType();
	
	/**
	 * 获取字段的规模
	 * @return
	 */
	public int getColumnScale();
	
	/**
	 * 获取字段的精度
	 * @return
	 */
	public int getColumnPrecision();
	
	/**
	 * 获取字段定义，Column注解允许直接设置字段定义
	 * @return
	 */
	public String getColumnDefinition();
	
	/**
	 * 字段是否有唯一性约束
	 * @return
	 */
	public boolean isColumnUnique();
	
	/**
	 * 字段是否允许为空
	 * @return
	 */
	public boolean isColumnNullable();
	
	/**
	 * 字段是否允许插入
	 * @return
	 */
	public boolean isColumnInsertable();
	
	/**
	 * 字段是否允许更新
	 * @return
	 */
	public boolean isColumnUpdatable();
	
	/**
	 * 字段是否为lob字段
	 * @return
	 */
	public boolean isLob();
	
	/**
	 * 字段是否为主键
	 * @return
	 */
	public boolean isPrimary();
	
	/**
	 * 字段是否被加入乐观锁
	 * @return
	 */
	public boolean isVersion();

	/**
	 * 字段是否是延迟加载字段
	 * @return
	 */
	public boolean isLazyLoad();
}
