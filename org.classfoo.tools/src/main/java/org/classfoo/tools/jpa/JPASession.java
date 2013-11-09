package org.classfoo.tools.jpa;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.classfoo.tools.jdbc.ConnectionFactory;

/**
 * 数据库访问会话接口，这个接口将用于增删改查通过JPA注解注册到{@link JPASessionFactory}中的数据库表
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-6-27
 */
public interface JPASession {
	
	/**
	 * 默认的批量操作提交条数限额
	 */
	public static final int DEFAULT_BATCH_SIZE = 200;

	/**
	 * 判断数据库表是否存在
	 * @param tableName
	 * @return
	 */
	public boolean isTableExist(String tableName);

	/**
	 * 查询sql,获取唯一的结果
	 * @param sql
	 * @param params
	 * @return
	 */
	public Object executeQueryUnique(String sql, Object... params);

	/**
	 * 查询sql，获取唯一的结果，并封装到特定类型对象中
	 * @param type
	 * @param hql
	 * @param params
	 * @return
	 */
	public <T extends Object> T executeQueryUnique(Class<T> type, String sql, Object... params);

	public <T extends JPAObject> T executeQueryUnique(Class<T> type, JPATable table, String sql, Object... params);

	/**
	 * 查询sql,获取列表结果
	 * @param sql
	 * @param startindex
	 * @param pagesize
	 * @param params
	 * @return
	 */
	public List<?> executeQueryList(String sql, int startindex, int pagesize, Object... params);

	/**
	 * 查询sql，获取列表结果，结果是一个封装到特定类型对象的列表
	 * @param type
	 * @param hql
	 * @param startindex
	 * @param pagesize
	 * @param params
	 * @return
	 */
	public <T extends Object> List<T> executeQueryList(Class<T> type, String sql, int startindex, int pagesize,
			Object... params);

	public <T extends JPAObject> List<T> executeQueryList(Class<T> type, JPATable table, String sql, int startindex, int pagesize,
			Object... params);
	
	/**
	 * 这个方法可以用于执行sql的update，insert语句，返回执行生效的行数
	 * @param sql
	 * @param params
	 * @return
	 */
	public int executeUpdate(String sql, Object... params);

	/**
	 * 通过封装表的主键值的序列化对象，查询特定注解类对应数据库表，将查询结果封装为注解类实例返
	 * 回。如果没有查询到记录将返回null
	 * @param type
	 * @param primaryKeys
	 * @return
	 */
	public <T extends Object> T get(Class<T> type, Object primaryKeys);
	
	public <T extends JPAObject> T get(Class<T> type, JPATable table, Object primaryKeys);

	/**
	 * 根据object的类型查找到对应的数据库表，将object值提取保存
	 * @param object
	 */
	public void save(Object object);

	/**
	 * 根据object类型查找到对应的数据库表，将object值保存或者更新.如果object定义了@Version注解
	 * 的字段，那么在保存时应该检查其乐观锁版本，版本不符合则抛出异常
	 * @param object
	 */
	public void saveOrUpdate(Object object);

	/**
	 * 根据object类型查找到对应的数据库表，将object值更新.如果object定义了@Version注解的字段，
	 * 那么在保存时应该检查其乐观锁版本，版本不符合则抛出异常
	 * @param object
	 */
	public void update(Object object);

	/**
	 * 根据object值查找到对应的数据库表，将object值对应记录删除
	 * @param mo
	 */
	public void delete(Object mo);
	
	/**
	 * 将from这个object代表的数据记录拷贝到to代表的数据记录中，需要保证from和to都是JPA注解类的
	 * 实例，将从中提取组件信息，并根据to记录是否存在去执行update select语句或者insert into select
	 * 语句
	 * @param to
	 * @param from
	 */
	public void copy(Object to, Object from);
	
	/**
	 * 将from这个object代表的数据记录拷贝到to代表的数据记录中，要保证from和to都是已经注册的JPA
	 * 注解类实例，并保证from，to都是已经存在的数据，将执行update select语句进行表内拷贝
	 * @param to
	 * @param from
	 */
	public void copyUpdate(Object to, Object from);
	
	/**
	 * 将from这个object代表的数据记录拷贝到to代表的数据记录中，包保证from和to都是已经注册的JPA
	 * 注解类实例，其中to应该尚不存在记录，from记录应该已经存在，将执行insert into select语句进行
	 * 表内拷贝
	 * @param to
	 * @param from
	 * @param params 提供对select语句中指定一些特殊字段值功能
	 */
	public void copyInsert(Object to, Object from, Map<String, Object> params);

	/**
	 * 创建一个Blob对象
	 * @param bytes
	 * @return
	 */
	public Blob createBlob(byte[] bytes);

	/**
	 * 创建一个Clob对象
	 * @param str
	 * @return
	 */
	public Clob createClob(String str);
	
	/**
	 * 设置批量执行的条数限制，以便微调批量操作的限制。默认将有一个size，它的值为
	 * {@link #DEFAULT_BATCH_SIZE}
	 * @param size
	 */
	public void setBatchSize(int size);
	
	/**
	 * 获取当前的批量执行条数限制
	 * @return
	 */
	public int getBatchSize();

	/**
	 * 开始数据库事务，执行这个方法后，在commit前的操作都会纳入一个事务中提交
	 * @param 是否启用批量操作模式，当batchMode为true，将会同类型对象save，update，delete操作进行批量处理
	 */
	public void beginTransaction(boolean batchMode);

	/**
	 * 回滚数据库事务
	 */
	public void rollback();

	/**
	 * 提交数据库事务
	 */
	public void commit();

	/**
	 * 关闭数据库会话
	 */
	public void close();

	/**
	 * 数据库会话是否已经开启，通常JPASession创建后就是isOpen状态，执行close后调用本方法返回false
	 * @return
	 */
	public boolean isOpen();

	/**
	 * 获取屏蔽数据库差异的sql函数字符串
	 * @param function
	 * @param args
	 * @return
	 */
	public String rendSqlFunction(String function, String... args);

	/**
	 * 格式化字符串避免sql注入的接口
	 * @param ppath
	 * @return
	 */
	public String formatConstValue(String value);

	/**
	 * 获取JPASession基于的连接池Factory类
	 * @return
	 */
	public ConnectionFactory getConnectionFactory();

	/**
	 * 获取JPASession内部包装的数据库连接Connection
	 * @return
	 */
	public Connection getConnection();
}
