package org.classfoo.tools.jpa;

import java.util.Map;


/**
 * 由于JPA注解类实例保存到数据库表，以及将数据库表记录取出构造JPA实例对象时，相关的insert语句，
 * select语句基本上是固定的，它们可以被重复利用。这个类将管理这些可以复用的sql，减少因为重复构
 * 造同样的sql导致的消耗
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-6-28
 */
public interface JPASqlPool {

	/**
	 * 获取保存一个JPA注解类实例所需要的sql
	 * @param table
	 * @return
	 */
	public String getJPASaveSql(JPATable table);

	/**
	 * 获取查询一个JPA注解类所需要的sql
	 * TODO 需要根据lob和lazyload的设置去避免某些字段的查询
	 * @param table
	 * @return
	 */
	public String getJPAQuerySql(JPATable table);

	/**
	 * 获取删除一个JAP注解实例所需要的sql
	 * @param table
	 * @return
	 */
	public String getJPADeleteSql(JPATable table);

	/**
	 * 获取更新一个JPA注解实例所需要的sql
	 * @param table
	 * @return
	 */
	public String getJPAUpdateSql(JPATable table);

	/**
	 * 获取一个表内两条数据拷贝时所需要的sql，这个sql在各个数据库平台下各不相同，参考wiki：
	 * http://dev.succez.com/pages/viewpage.action?pageId=84246772
	 * 将通过jdbc的相关功能获取这个sql，相关伪代码如下：
	 * UpdateDDL update = sqlFactory.createUpdateDDL();
	 * update.setTableName("test_1");
	 * update.addTable("test_1", "a", null, "a.id_=?");
	 * update.addColumnWithExpression("field1", "a.field1");
	 * update.addColumnWithExpression("field2", "a.field2");
	 * update.addColumnWithExpression("field3", "a.field3");
	 * update.setWhere("id_=?");
	 * DbMetaData dbMetaData = ...
	 * String ddl = SQLFactory.getSql(update,dbMetaData);
	 * @param table
	 * @return
	 */
	public String getJPACopyUpdateSql(JPATable table);
	
	/**
	 * 获取将一个表内数据拷贝为一条新数据的sql，这个sql在不同数据库平台下各不相同，主要就是一条
	 * insert into select语句，将由jdbc模块进行平台兼容，将使用如下接口构造sql：
	 * String com.succez.commons.jdbc.Dialect.getInsertIntoSql(String destTable, String fields, 
	 * String querySql, boolean nologging)
	 * @param table
	 * @param obj 待copyinsert的对象，将需要从中取得主键值，以避免查询结果集中出现参数问号，避免db下问题
	 * @return
	 */
	public String getJPACopyInsertSql(JPATable table, Object obj, Map<String, Object> params);

	/**
	 * 当JPASessionFactory进行重新注册后，需要将pool进行清空
	 */
	public void reset();
}
