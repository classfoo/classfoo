package org.classfoo.tools.jpa.impl;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.classfoo.tools.jdbc.ConnectionFactory;
import org.classfoo.tools.jdbc.DbMetaData;
import org.classfoo.tools.jdbc.Dialect;
import org.classfoo.tools.jdbc.SQLFactory;
import org.classfoo.tools.jdbc.TableMetaData;
import org.classfoo.tools.jpa.JPAField;
import org.classfoo.tools.jpa.JPAObject;
import org.classfoo.tools.jpa.JPASession;
import org.classfoo.tools.jpa.JPASessionFactory;
import org.classfoo.tools.jpa.JPASqlPool;
import org.classfoo.tools.jpa.JPATable;

/**
 * JPA会话接口实现，它封装了一个JDBC的数据库连接，可以在会话中进行数据的增，删，改，查以及事务
 * 相关操作
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-6-28
 */
public class JPASessionImpl implements JPASession {

	/**
	 * 数据库连接，一个JPA会话代表了对一个Connection的操作封装
	 */
	private Connection conn;

	/**
	 * JPA会话工厂类
	 */
	private JPASessionFactory factory;

	/**
	 * 保存对象的批量操作PrepareStatement缓存表
	 */
	private Map<String, PreparedStatement> saveStatements;

	private Map<String, Integer> saveStatementsSize;

	/**
	 * 更新对象的批量操作PrepareStatement缓存表
	 */
	private Map<String, PreparedStatement> updateStatements;

	private Map<String, Integer> updateStatementsSize;

	/**
	 * 删除对象的批量操作PrepareStatement缓存表
	 */
	private Map<String, PreparedStatement> deleteStatements;

	private Map<String, Integer> deleteStatementsSize;

	/**
	 * 拷贝对象的批量操作PrepareStatement缓存表
	 */
	private Map<String, PreparedStatement> copyUpdateStatements;

	private Map<String, Integer> copyUpdateStatementsSize;

	/**
	 * 拷贝对象并insert的批量操作PrepareStatement缓存表
	 */
	private Map<String, PreparedStatement> copyInsertStatements;

	private Map<String, Integer> copyInsertStatementsSize;

	/**
	 * 针对JPA注解类实例的insert，select，update，delete的sql的缓存池
	 */
	private JPASqlPool jpaSqlPool;

	/**
	 * 是否处于事务状态的标记
	 */
	private boolean INTRANSACTION = false;

	private boolean BATCHMODE = false;

	private int BATCHSIZE = DEFAULT_BATCH_SIZE;

	private SQLFactory sqlFactory;

	private ConnectionFactory connFactory;

	public JPASessionImpl(JPASessionFactory factory, ConnectionFactory connFactory, SQLFactory sqlFactory,
			Connection conn, JPASqlPool jpaSqlPool) {
		this.conn = conn;
		this.connFactory = connFactory;
		this.sqlFactory = sqlFactory;
		this.factory = factory;
		this.jpaSqlPool = jpaSqlPool;
	}

	public boolean isTableExist(String tableName) {
		try {
			DbMetaData meta = this.connFactory.getDbMetaData();
			TableMetaData table = meta.getTableMetaData(this.conn, tableName);
			return table != null;
		}
		catch (Exception e) {
			return false;
		}
	}

	public <T> T get(Class<T> type, Object primaryKey) {
		JPATable jpaTable = this.factory.getJPATable(type);
		if (jpaTable == null) {
			throw new RuntimeException("类型" + type + "尚未注册到JPA会话工厂中！");
		}
		List<String> pkNames = jpaTable.getPrimaryFieldNames();
		if (pkNames == null || pkNames.isEmpty()) {
			pkNames = jpaTable.getFieldNames();
		}
		int pkSize = pkNames.size();
		if (pkSize == 1) {
			String pkName = pkNames.get(0);
			return getBySinglePrimary(type, primaryKey, jpaTable, pkName);
		}
		return getByMultiPrimaryKeys(type, primaryKey, jpaTable, pkNames, pkSize);
	}

	public <T extends JPAObject> T get(Class<T> type, JPATable table, Object primaryKey) {
		List<String> pkNames = table.getPrimaryFieldNames();
		if (pkNames == null || pkNames.isEmpty()) {
			pkNames = table.getFieldNames();
		}
		int pkSize = pkNames.size();
		if (pkSize == 1) {
			String pkName = pkNames.get(0);
			return getBySinglePrimary(type, primaryKey, table, pkName);
		}
		return getByMultiPrimaryKeys(type, primaryKey, table, pkNames, pkSize);
	}

	/**
	 * 当JPA对象有多个主键设置时，将从传入的primaryKey中根据组件属性名称读取值，作为sql的参数执行
	 * 查询，得到结果
	 * @param type
	 * @param primaryKey
	 * @param jpaTable
	 * @param pkNames
	 * @param pkSize
	 * @return
	 */
	private <T> T getByMultiPrimaryKeys(Class<T> type, Object primaryKey, JPATable jpaTable, List<String> pkNames,
			int pkSize) {
		String sql = this.jpaSqlPool.getJPAQuerySql(jpaTable);
		try {
			PreparedStatement pst = conn.prepareStatement(sql);
			try {
				Map<String, Object> objectMap = JPAUtil.getObjectMap(primaryKey);
				for (int i = 0; i < pkSize; i++) {
					String pkName = pkNames.get(i);
					if (!objectMap.containsKey(pkName)) {
						throw new RuntimeException("不合法的主键对象" + primaryKey + "，不存在属性" + pkName);
					}
					Object value = objectMap.get(pkName);
					pst.setObject(i + 1, value);
				}
				ResultSet result = pst.executeQuery();
				boolean hasNext = result.next();
				return hasNext ? JPADataBaseUtil.createObjectWithResultSet(this.factory, type, jpaTable, result, this)
						: null;
			}
			finally {
				pst.close();
			}
		}
		catch (Exception e) {
			throw new RuntimeException("无法从数据库中查询JPA注解类实例", e);
		}
	}

	/**
	 * 当JPA对象只有一个主键时，将根据primayKey的类型进行判断，如果它就是组件字段的java类型时，
	 * 将直接作为参数进行sql查询，否则将从其内部同名属性值作为参数进行sql查询
	 * @param type
	 * @param primaryKey
	 * @param jpaTable
	 * @param pkName
	 * @return
	 */
	private <T> T getBySinglePrimary(Class<T> type, Object primaryKey, JPATable jpaTable, String pkName) {
		String sql = this.jpaSqlPool.getJPAQuerySql(jpaTable);
		try {
			PreparedStatement pst = conn.prepareStatement(sql);
			try {
				Class<?> jpaType = jpaTable.getJPAType();
				if (JPABeanUtils.instanceOf(primaryKey, jpaType)) {
					Map<String, Object> objectMap = JPAUtil.getObjectMap(primaryKey);
					if (!objectMap.containsKey(pkName)) {
						throw new RuntimeException("不合法的主键对象" + primaryKey + "，不存在属性" + pkName);
					}
					Object value = objectMap.get(pkName);
					pst.setObject(1, value);
				}
				else {
					pst.setObject(1, primaryKey);
				}
				ResultSet result = pst.executeQuery();
				boolean hasNext = result.next();
				return hasNext ? JPADataBaseUtil.createObjectWithResultSet(this.factory, type, jpaTable, result, this)
						: null;
			}
			finally {
				pst.close();
			}
		}
		catch (Exception e) {
			throw new RuntimeException("无法从数据库中查询JPA注解类实例", e);
		}
	}

	public Object executeQueryUnique(String sql, Object... params) {
		try {
			sql = JPADataBaseUtil.patternEntityNameSql(this.factory, sql, true);
			if (params == null || params.length == 0) {
				return this.queryUniqueByStatement(sql);
			}
			return this.queryUniqueByPreparedStatement(sql, params);
		}
		catch (SQLException e) {
			throw new RuntimeException("执行queryUnique失败,sql：" + sql + "，参数：" + params, e);
		}
	}

	/**
	 * 查询无参数的sql，期待其返回唯一结果
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	private Object queryUniqueByStatement(String sql) throws SQLException {
		Statement stat = conn.createStatement();
		try {
			ResultSet result = stat.executeQuery(sql);
			return JPADataBaseUtil.convertResultSetToUnique(result);
		}
		finally {
			stat.close();
		}
	}

	/**
	 * 查询带参数的sql，期待其返回唯一结果
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	private Object queryUniqueByPreparedStatement(String sql, Object[] params) throws SQLException {
		PreparedStatement pst = conn.prepareStatement(sql);
		try {
			int size = params.length;
			for (int i = 0; i < size; i++) {
				Object param = params[i];
				pst.setObject(i + 1, param);
			}
			ResultSet result = pst.executeQuery();
			return JPADataBaseUtil.convertResultSetToUnique(result);
		}
		finally {
			pst.close();
		}
	}

	public <T> T executeQueryUnique(Class<T> type, String sql, Object... params) {
		try {
			sql = JPADataBaseUtil.patternEntityNameSql(this.factory, sql, true);
			sql = JPADataBaseUtil.patternLazyLoaderSql(this.factory, type, sql);
			JPATable table = this.factory.getJPATable(type);
			if (params == null || params.length == 0) {
				return this.queryUniqueByStatement(type, table, sql);
			}
			return this.queryUniqueByPreparedStatement(type, table, sql, params);
		}
		catch (Exception e) {
			throw new RuntimeException("执行queryUnique失败,sql：" + sql + "，参数：" + params, e);
		}
	}

	public <T extends JPAObject> T executeQueryUnique(Class<T> type, JPATable table, String sql, Object... params) {
		try {
			sql = JPADataBaseUtil.patternEntityNameSql(this.factory, sql, true);
			sql = JPADataBaseUtil.patternLazyLoaderSql(this.factory, table.getJPAType(), sql);
			if (params == null || params.length == 0) {
				return this.queryUniqueByStatement(type, table, sql);
			}
			return this.queryUniqueByPreparedStatement(type, table, sql, params);
		}
		catch (Exception e) {
			throw new RuntimeException("执行queryUnique失败,sql：" + sql + "，参数：" + params, e);
		}
	}

	/**
	 * 查询无参数的sql，期待其返回唯一结果
	 * @param sql
	 * @return
	 * @throws SQLException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 */
	private <T> T queryUniqueByStatement(Class<T> type, JPATable table, String sql) throws SQLException,
			InstantiationException, IllegalAccessException, IOException {
		Statement stat = conn.createStatement();
		try {
			ResultSet result = stat.executeQuery(sql);
			return JPADataBaseUtil.convertResultSetToUnique(this.factory, type, table, result, this);
		}
		finally {
			stat.close();
		}
	}

	/**
	 * 查询带参数的sql，期待其返回唯一结果
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 */
	private <T> T queryUniqueByPreparedStatement(Class<T> type, JPATable table, String sql, Object[] params)
			throws SQLException, InstantiationException, IllegalAccessException, IOException {
		PreparedStatement pst = conn.prepareStatement(sql);
		try {
			int size = params.length;
			for (int i = 0; i < size; i++) {
				Object param = params[i];
				pst.setObject(i + 1, param);
			}
			ResultSet result = pst.executeQuery();
			return JPADataBaseUtil.convertResultSetToUnique(this.factory, type, table, result, this);
		}
		finally {
			pst.close();
		}
	}

	public List<?> executeQueryList(String sql, int startindex, int pagesize, Object... params) {
		sql = JPADataBaseUtil.patternEntityNameSql(factory, sql, true);
		sql = this.connFactory.getDialect().getLimitString(sql, startindex, pagesize);
		try {
			if (params == null || params.length == 0) {
				return queryListByStatement(sql, startindex, pagesize);
			}
			return this.queryListByPreparedStatement(sql, startindex, pagesize, params);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 查询不带参数的SQL，预期获取列表类型的查询结果
	 * @param sql
	 * @param startindex
	 * @param pagesize
	 * @return
	 */
	private List<?> queryListByStatement(String sql, int startindex, int pagesize) {
		try {
			Statement stat = this.conn.createStatement();
			try {
				ResultSet result = stat.executeQuery(sql);
				return JPADataBaseUtil.convertResultSetToList(result);
			}
			finally {
				stat.close();
			}
		}
		catch (SQLException e) {
			throw new RuntimeException("查询SQL失败：" + sql + ", 参数为空");
		}
	}

	/**
	 * 查询带参数的SQL，预期获取列表类型的查询结果
	 * @param sql
	 * @param startindex
	 * @param pagesize
	 * @param params
	 * @return
	 * @throws SQLException 
	 */
	private List<?> queryListByPreparedStatement(String sql, int startindex, int pagesize, Object[] params)
			throws SQLException {
		PreparedStatement pst = this.conn.prepareStatement(sql);
		try {
			int size = params.length;
			for (int i = 0; i < size; i++) {
				Object param = params[i];
				pst.setObject(i + 1, param);
			}
			ResultSet result = pst.executeQuery();
			List<Object> list = JPADataBaseUtil.convertResultSetToList(result);
			return list;
		}
		finally {
			pst.close();
		}
	}

	public <T> List<T> executeQueryList(Class<T> type, String sql, int startindex, int pagesize, Object... params) {
		sql = JPADataBaseUtil.patternEntityNameSql(factory, sql, true);
		sql = JPADataBaseUtil.patternLazyLoaderSql(this.factory, type, sql);
		sql = this.connFactory.getDialect().getLimitString(sql, startindex, pagesize);
		try {
			JPATable table = this.factory.getJPATable(type);
			if (params == null || params.length == 0) {
				return queryListByStatement(type, table, sql, startindex, pagesize);
			}
			return this.queryListByPreparedStatement(type, table, sql, startindex, pagesize, params);
		}
		catch (Exception e) {
			JPAUtil.throwSqlException(sql, e, params);
			return null;
		}
	}

	public <T extends JPAObject> List<T> executeQueryList(Class<T> type, JPATable table, String sql, int startindex,
			int pagesize, Object... params) {
		sql = JPADataBaseUtil.patternEntityNameSql(factory, sql, true);
		sql = JPADataBaseUtil.patternLazyLoaderSql(this.factory, table.getJPAType(), sql);
		sql = this.connFactory.getDialect().getLimitString(sql, startindex, pagesize);
		try {
			if (params == null || params.length == 0) {
				return queryListByStatement(type, table, sql, startindex, pagesize);
			}
			return this.queryListByPreparedStatement(type, table, sql, startindex, pagesize, params);
		}
		catch (Exception e) {
			JPAUtil.throwSqlException(sql, e, params);
			return null;
		}
	}

	/**
	 * 查询不带参数的SQL，预期获取列表类型的查询结果
	 * @param sql
	 * @param startindex
	 * @param pagesize
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private <T> List<T> queryListByStatement(Class<T> type, JPATable table, String sql, int startindex, int pagesize)
			throws SQLException, InstantiationException, IllegalAccessException, IOException {
		Statement stat = this.conn.createStatement();
		try {
			ResultSet result = stat.executeQuery(sql);
			return JPADataBaseUtil.convertResultSetToList(this.factory, type, table, result, this);
		}
		finally {
			stat.close();
		}
	}

	/**
	 * 查询带参数的SQL，预期获取列表类型的查询结果
	 * @param sql
	 * @param startindex
	 * @param pagesize
	 * @param params
	 * @return
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 */
	private <T> List<T> queryListByPreparedStatement(Class<T> type, JPATable table, String sql, int startindex,
			int pagesize, Object[] params) throws SQLException, InstantiationException, IllegalAccessException,
			IOException {
		PreparedStatement pst = this.conn.prepareStatement(sql);
		try {
			int size = params.length;
			for (int i = 0; i < size; i++) {
				Object param = params[i];
				pst.setObject(i + 1, param);
			}
			ResultSet result = pst.executeQuery();
			List<T> list = JPADataBaseUtil.convertResultSetToList(this.factory, type, table, result, this);
			return list;
		}
		finally {
			pst.close();
		}
	}

	public void save(Object object) {
		try {
			JPATable jpaTable = JPAUtil.getJPATableFromObject(this.factory, object);
			PreparedStatement pst = this.getSavePreparedStatement(jpaTable);
			try {
				List<String> fieldNames = jpaTable.getFieldNames();
				Map<String, Object> bean = JPAUtil.getObjectMap(object);//BeanMap.create(object);
				for (int i = 0; i < fieldNames.size(); i++) {
					String fieldName = fieldNames.get(i);
					JPAField jpaField = jpaTable.getField(fieldName);
					if (jpaField.isVersion()) {//乐观锁字段，保存后初始为0
						pst.setObject(i + 1, 0);
						bean.put(fieldName, 0);
						continue;
					}
					Object value = bean.get(fieldName);
					if (value == null) {
						if (jpaField.isPrimary()) {
							throw new RuntimeException("对象" + object + "的主键属性" + fieldName + "不能为空！");
						}
						if (!jpaField.isColumnNullable()) {
							throw new RuntimeException("对象" + object + "的非空属性" + fieldName + "不能为空！");
						}
					}
					pst.setObject(i + 1, value);
				}
				if (this.INTRANSACTION && BATCHMODE) {
					if (this.saveStatementsSize == null) {
						this.saveStatementsSize = new HashMap<String, Integer>();
					}
					this.addOrExecuteBatch(pst, jpaTable, this.saveStatementsSize);
				}
				else {
					pst.executeUpdate();
				}
			}
			finally {
				if (!this.INTRANSACTION || !BATCHMODE) {
					pst.close();
				}
			}
		}
		catch (SQLException e) {
			throw new RuntimeException("保存JPA注解类实例" + object + "失败！", e);
		}
	}

	public void saveOrUpdate(Object object) {
		JPATable jpaTable = JPAUtil.getJPATableFromObject(this.factory, object);
		if (jpaTable == null) {
			Class<? extends Object> type = object.getClass();
			throw new RuntimeException("待操作对象" + object + "的类型" + type + "没有注册到数据库会话工厂中！");
		}
		Object result = this.get(object.getClass(), object);
		if (result == null) {
			this.save(object);
		}
		else {
			this.update(object);
		}
	}

	@SuppressWarnings("deprecation")
	public void update(Object object) {
		try {
			JPATable jpaTable = JPAUtil.getJPATableFromObject(this.factory, object);
			Map<String, Object> objectMap = JPAUtil.getObjectMap(object);
			String versionField = jpaTable.getVersionFieldName();
			boolean hasVersion = versionField != null;
			Long versionValue = hasVersion ? ((Long) objectMap.get(versionField)) + 1 : -1;
			PreparedStatement pst = this.getUpdatePreparedStatement(jpaTable);
			try {
				List<String> fieldNames = jpaTable.getFieldNames();
				int index = 1;
				int fieldsize = fieldNames.size();
				for (int i = 0; i < fieldsize; i++) {
					String fieldName = fieldNames.get(i);
					JPAField jpaField = jpaTable.getField(fieldName);
					if (jpaField.isPrimary()) {
						continue;
					}
					if (jpaField.isVersion()) {//增加乐观锁数字
						pst.setObject(index++, versionValue);
						continue;
					}
					Object value = objectMap.get(fieldName);
					pst.setObject(index++, value);
				}
				List<String> pkNames = jpaTable.getPrimaryFieldNames();
				if (pkNames == null || pkNames.isEmpty()) {
					pkNames = jpaTable.getFieldNames();
				}
				int pksize = pkNames.size();
				for (int i = 0; i < pksize; i++) {
					String pkName = pkNames.get(i);
					JPAField jpaField = jpaTable.getField(pkName);
					Object value = objectMap.get(pkName);
					if (jpaField.isPrimary() && value == null) {
						throw new RuntimeException("对象" + object + "的主键属性" + pkName + "不能为空！");
					}
					pst.setObject(index + i, value);
				}
				if (hasVersion) {//判断乐观锁是否匹配
					Long opt = (Long) objectMap.get(versionField);
					pst.setObject(fieldsize + 1, opt);
				}
				if (this.INTRANSACTION && this.BATCHMODE) {
					if (this.updateStatementsSize == null) {
						this.updateStatementsSize = new HashMap<String, Integer>();
					}
					this.addOrExecuteBatch(pst, jpaTable, this.updateStatementsSize);
				}
				else {
					int count = pst.executeUpdate();
					if (hasVersion) {
						if (count != 1) {
							throw new RuntimeException("RuntimeException.JPASessionImpl.OptimisticLocking.failed");
						}
						objectMap.put(versionField, versionValue);
					}
				}
			}
			finally {
				if (!this.INTRANSACTION || !this.BATCHMODE) {
					pst.close();
				}
			}
		}
		catch (SQLException e) {
			throw new RuntimeException("更新JPA注解类实例" + object + "失败！", e);
		}
	}

	public int executeUpdate(String sql, Object... params) {
		sql = JPADataBaseUtil.patternEntityNameSql(factory, sql, true);
		try {
			if (params == null || params.length == 0) {
				return this.updateByStatement(sql);
			}
			return this.updateByPreparedStatement(sql, params);
		}
		catch (SQLException e) {
			JPAUtil.throwSqlException(sql, e, params);
			return -1;
		}
	}

	/**
	 * 当执行sql没有任何参数的时候，直接使用Statement来执行操作
	 * @param sql
	 * @return
	 * @throws SQLException 
	 */
	private int updateByStatement(String sql) throws SQLException {
		Statement stat = this.conn.createStatement();
		try {
			int count = stat.executeUpdate(sql);
			return count;
		}
		finally {
			stat.close();
		}
	}

	/**
	 * 当执行sql时存在参数时，需要使用PreparedStatement来执行操作 
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException 
	 */
	private int updateByPreparedStatement(String sql, Object[] params) throws SQLException {
		PreparedStatement pst = this.conn.prepareStatement(sql);
		try {
			int size = params.length;
			for (int i = 0; i < size; i++) {
				Object param = params[i];
				pst.setObject(i + 1, param);
			}
			return pst.executeUpdate();
		}
		finally {
			pst.close();
		}
	}

	public void delete(Object object) {
		try {
			JPATable jpaTable = JPAUtil.getJPATableFromObject(this.factory, object);
			PreparedStatement pst = this.getDeletePreparedStatement(jpaTable);
			try {
				List<String> pkNames = jpaTable.getPrimaryFieldNames();
				if (pkNames == null || pkNames.isEmpty()) {
					pkNames = jpaTable.getFieldNames();
				}
				Map<String, Object> objectMap = JPAUtil.getObjectMap(object);
				int pkSize = pkNames.size();
				for (int i = 0; i < pkSize; i++) {
					String pkName = pkNames.get(i);
					JPAField jpaField = jpaTable.getField(pkName);
					Object value = objectMap.get(pkName);
					if (jpaField.isPrimary() && value == null) {
						throw new RuntimeException("对象" + object + "的主键属性" + pkName + "不能为空！");
					}
					pst.setObject(i + 1, value);
				}
				if (this.INTRANSACTION && this.BATCHMODE) {
					if (this.deleteStatementsSize == null) {
						this.deleteStatementsSize = new HashMap<String, Integer>();
					}
					this.addOrExecuteBatch(pst, jpaTable, this.deleteStatementsSize);
				}
				else {
					try {
						pst.executeUpdate();
					}
					finally {
						pst.close();
					}
				}
			}
			finally {
				if (!this.INTRANSACTION || !this.BATCHMODE) {
					pst.close();
				}
			}
		}
		catch (SQLException e) {
			throw new RuntimeException("删除JPA注解类实例" + object + "失败！", e);
		}
	}

	public void copy(Object to, Object from) {
		this.copyUpdate(to, from);
	}

	public void copyInsert(Object to, Object from, Map<String, Object> params) {
		JPATable toTable = JPAUtil.getJPATableFromObject(this.factory, to);
		JPATable fromTable = JPAUtil.getJPATableFromObject(this.factory, from);
		if (toTable == null || fromTable == null) {
			throw new RuntimeException("不支持尚未注册为JPA的数据拷贝！");
		}
		if (!toTable.equals(fromTable)) {
			throw new RuntimeException("不支持表间拷贝！");
		}
		try {
			PreparedStatement pst = this.getCopyInsertPreparedStatement(toTable, to, params);
			//将二者的组件进行设置
			try {
				List<String> pknames = toTable.getPrimaryFieldNames();
				if (pknames == null || pknames.isEmpty()) {
					pknames = toTable.getFieldNames();
				}
				int pksize = pknames.size();
				Map<String, Object> fromMap = JPAUtil.getObjectMap(from);
				for (int i = 0; i < pksize; i++) {
					String pkname = pknames.get(i);
					Object value = fromMap.get(pkname);
					pst.setObject(i + 1, value);
				}
				if (this.INTRANSACTION && this.BATCHMODE) {
					if (this.copyInsertStatementsSize == null) {
						this.copyInsertStatementsSize = new HashMap<String, Integer>();
					}
					this.addOrExecuteBatch(pst, toTable, this.copyInsertStatementsSize);
				}
				else {
					try {
						pst.executeUpdate();
					}
					finally {
						pst.close();
					}
				}
			}
			finally {
				if (!this.INTRANSACTION || !this.BATCHMODE) {
					pst.close();
				}
			}
		}
		catch (SQLException e) {
			throw new RuntimeException("表内拷贝数据失败！" + e);
		}
	}

	public void copyUpdate(Object to, Object from) {
		JPATable toTable = JPAUtil.getJPATableFromObject(this.factory, to);
		JPATable fromTable = JPAUtil.getJPATableFromObject(this.factory, from);
		if (toTable == null || fromTable == null) {
			throw new RuntimeException("不支持尚未注册为JPA的数据拷贝！");
		}
		if (!toTable.equals(fromTable)) {
			throw new RuntimeException("不支持表间拷贝！");
		}
		try {
			PreparedStatement pst = this.getCopyPreparedStatement(toTable);
			//将二者的组件进行设置
			try {
				List<String> pknames = toTable.getPrimaryFieldNames();
				if (pknames == null || pknames.isEmpty()) {
					pknames = toTable.getFieldNames();
				}
				int pksize = pknames.size();
				Map<String, Object> fromMap = JPAUtil.getObjectMap(from);
				for (int i = 0; i < pksize; i++) {
					String pkname = pknames.get(i);
					Object value = fromMap.get(pkname);
					pst.setObject(i + 1, value);
				}
				Map<String, Object> toMap = JPAUtil.getObjectMap(to);
				for (int i = 0; i < pksize; i++) {
					String pkname = pknames.get(i);
					Object value = toMap.get(pkname);
					pst.setObject(i + 1 + pksize, value);
				}
				if (this.INTRANSACTION && this.BATCHMODE) {
					if (this.copyUpdateStatementsSize == null) {
						this.copyUpdateStatementsSize = new HashMap<String, Integer>();
					}
					this.addOrExecuteBatch(pst, toTable, this.copyUpdateStatementsSize);
				}
				else {
					try {
						pst.executeUpdate();
					}
					finally {
						pst.close();
					}
				}
			}
			finally {
				if (!this.INTRANSACTION || !this.BATCHMODE) {
					pst.close();
				}
			}
		}
		catch (SQLException e) {
			throw new RuntimeException("表内拷贝数据失败！" + e);
		}
	}

	public Blob createBlob(byte[] bytes) {
		//		try {
		//			//Blob blob = conn.createBlob();
		//			blob.setBytes(0, bytes);
		//			return blob;
		//		}
		//		catch (SQLException e) {
		//			throw new RuntimeException("创建Blob对象失败！", e);
		//		}
		return null;
	}

	public Clob createClob(String str) {
		//		try {
		//			Clob clob = conn.createClob();
		//			clob.setString(0, str);
		//			return clob;
		//		}
		//		catch (SQLException e) {
		//			throw new RuntimeException("创建Clob对象失败！", e);
		//		}
		return null;
	}

	public void setBatchSize(int size) {
		this.BATCHSIZE = size;
	}

	public int getBatchSize() {
		return this.BATCHSIZE;
	}

	public void beginTransaction(boolean batchMode) {
		try {
			this.conn.setAutoCommit(false);
			this.INTRANSACTION = true;
			this.BATCHMODE = batchMode;
		}
		catch (SQLException e) {
			throw new RuntimeException("设置JPA会话连接的autoCommit失败，无法开启事务！");
		}
	}

	public void rollback() {
		try {
			this.cleanTransaction();
		}
		catch (SQLException e) {
			throw new RuntimeException("无法回滚JPA会话持有的数据库事务！", e);
		}
		finally {
			try {
				//只有非autocommit时才允许rollback
				if (!conn.getAutoCommit()) {
					conn.rollback();
				}
			}
			catch (SQLException e) {
				throw new RuntimeException("无法回滚JPA会话持有的数据库事务！", e);
			}
		}
	}

	private void cleanTransaction() throws SQLException {
		if (this.INTRANSACTION) {
			this.cleanTransaction(this.saveStatements, this.deleteStatementsSize);
			this.cleanTransaction(this.updateStatements, this.updateStatementsSize);
			this.cleanTransaction(this.deleteStatements, this.deleteStatementsSize);
			this.INTRANSACTION = false;
		}
	}

	private void cleanTransaction(Map<String, PreparedStatement> statements, Map<String, Integer> statementsSize)
			throws SQLException {
		if (statements == null || statements.isEmpty()) {
			return;
		}
		Iterator<PreparedStatement> it = statements.values().iterator();
		while (it.hasNext()) {
			PreparedStatement pst = it.next();
			pst.close();
		}
		statements.clear();
		if (statementsSize == null || statementsSize.isEmpty()) {
			return;
		}
		statementsSize.clear();
	}

	public void commit() {
		if (this.INTRANSACTION && this.BATCHMODE) {
			try {
				this.executeAllBatches(this.saveStatements, this.saveStatementsSize);
				this.executeAllBatches(this.updateStatements, this.updateStatementsSize);
				this.executeAllBatches(this.deleteStatements, this.deleteStatementsSize);
				this.executeAllBatches(this.copyUpdateStatements, this.copyUpdateStatementsSize);
				this.executeAllBatches(this.copyInsertStatements, this.copyInsertStatementsSize);
				this.cleanTransaction();
			}
			catch (SQLException e) {
				throw new RuntimeException("执行JPA对象的批量提交操作失败！", e);
			}
		}
		try {
			this.conn.commit();
		}
		catch (SQLException e) {
			throw new RuntimeException("提交JPA操作失败！", e);
		}

	}

	public void close() {
		try {
			this.cleanTransaction();
		}
		catch (SQLException e) {
			throw new RuntimeException("无法关闭JPA会话持有的数据库连接！", e);
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (SQLException e) {
					throw new RuntimeException("无法关闭JPA会话持有的数据库连接！", e);
				}
			}
		}
	}

	public boolean isOpen() {
		if (this.conn == null) {
			return false;
		}
		try {
			return !this.conn.isClosed();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据batchSize的记录决定是执行PreparedStatement的addBatch还是executeBatch方法
	 * @param pst
	 * @param jpaTable
	 * @param sizeMap
	 * @throws SQLException
	 */
	private void addOrExecuteBatch(PreparedStatement pst, JPATable jpaTable, Map<String, Integer> sizeMap)
			throws SQLException {
		String entityName = jpaTable.getEntityName();
		Integer count = sizeMap.get(entityName);
		if (count != null && count + 1 == this.BATCHSIZE) {
			pst.addBatch();
			pst.executeBatch();
			sizeMap.put(entityName, 0);
		}
		else {
			pst.addBatch();
			sizeMap.put(entityName, count == null ? 1 : count + 1);
		}
	}

	/**
	 * 将缓存着的statement进行批量操作executeBatch
	 * @return
	 * @throws SQLException 
	 */
	private void executeAllBatches(Map<String, PreparedStatement> statementsMap, Map<String, Integer> sizeMap)
			throws SQLException {
		if (statementsMap == null || sizeMap == null || statementsMap.isEmpty() || sizeMap.isEmpty()) {
			return;
		}
		Set<String> keys = statementsMap.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String entityName = it.next();
			Integer count = sizeMap.get(entityName);
			if (count == null || count == 0) {
				continue;
			}
			PreparedStatement stat = statementsMap.get(entityName);
			stat.executeBatch();
		}
	}

	/**
	 * 根据是否开启事务，去创建或者获取已有的一个用于保存JPA对象的PrepareStatement对象
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement getSavePreparedStatement(JPATable table) throws SQLException {
		if (!this.INTRANSACTION || !BATCHMODE) {
			String sql = this.jpaSqlPool.getJPASaveSql(table);
			return this.conn.prepareStatement(sql);
		}
		String entityName = table.getEntityName();
		if (this.saveStatements == null) {
			this.saveStatements = new HashMap<String, PreparedStatement>();
		}
		else {
			PreparedStatement stat = this.saveStatements.get(entityName);
			if (stat != null) {
				return stat;
			}
		}
		String sql = this.jpaSqlPool.getJPASaveSql(table);
		PreparedStatement stat = this.conn.prepareStatement(sql);
		this.saveStatements.put(entityName, stat);
		return stat;
	}

	/**
	 * 根据是否开启事务，去创建或者获取已有的一个用于更新JPA对象的PrepareStatement对象
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement getUpdatePreparedStatement(JPATable table) throws SQLException {
		if (!this.INTRANSACTION || !BATCHMODE) {
			String sql = this.jpaSqlPool.getJPAUpdateSql(table);
			return this.conn.prepareStatement(sql);
		}
		String entityName = table.getEntityName();
		if (this.updateStatements == null) {
			this.updateStatements = new HashMap<String, PreparedStatement>();
		}
		else {
			PreparedStatement stat = this.updateStatements.get(entityName);
			if (stat != null) {
				return stat;
			}
		}
		String sql = this.jpaSqlPool.getJPAUpdateSql(table);
		PreparedStatement stat = this.conn.prepareStatement(sql);
		this.updateStatements.put(entityName, stat);
		return stat;
	}

	/**
	 * 根据是否开启事务，去创建或者获取已有的一个用于删除JPA对象的PrepareStatement对象
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement getDeletePreparedStatement(JPATable table) throws SQLException {
		if (!this.INTRANSACTION || !this.BATCHMODE) {
			String sql = this.jpaSqlPool.getJPADeleteSql(table);
			return this.conn.prepareStatement(sql);
		}
		String entityName = table.getEntityName();
		if (this.deleteStatements == null) {
			this.deleteStatements = new HashMap<String, PreparedStatement>();
		}
		else {
			PreparedStatement stat = this.deleteStatements.get(entityName);
			if (stat != null) {
				return stat;
			}
		}
		String sql = this.jpaSqlPool.getJPADeleteSql(table);
		PreparedStatement stat = this.conn.prepareStatement(sql);
		this.deleteStatements.put(entityName, stat);
		return stat;
	}

	/**
	 * 根据是否开启事务，去创建或者获取已有的一个用户表内数据拷贝的PrepareStatement对象
	 * @param jpaTable
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement getCopyPreparedStatement(JPATable jpaTable) throws SQLException {
		if (!this.INTRANSACTION || !this.BATCHMODE) {
			String sql = this.jpaSqlPool.getJPACopyUpdateSql(jpaTable);
			return this.conn.prepareStatement(sql);
		}
		String entityName = jpaTable.getEntityName();
		if (this.copyUpdateStatements == null) {
			this.copyUpdateStatements = new HashMap<String, PreparedStatement>();
		}
		else {
			PreparedStatement stat = this.copyUpdateStatements.get(entityName);
			if (stat != null) {
				return stat;
			}
		}
		String sql = this.jpaSqlPool.getJPACopyUpdateSql(jpaTable);
		PreparedStatement stat = this.conn.prepareStatement(sql);
		this.copyUpdateStatements.put(entityName, stat);
		return stat;
	}

	/**
	 * 根据是否开启事务，去创建或者获取已有的以恶搞用户表内数据拷贝insert into select的PrepareStatement对象
	 * @param jpaTable
	 * @param to 
	 * @param params copyinsert时可以指定一些特殊的field为某个特定的值，只支持普通字段
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement getCopyInsertPreparedStatement(JPATable jpaTable, Object to, Map<String, Object> params)
			throws SQLException {
		if (!this.INTRANSACTION || !this.BATCHMODE) {
			String sql = this.jpaSqlPool.getJPACopyInsertSql(jpaTable, to, params);
			return this.conn.prepareStatement(sql);
		}
		String entityName = jpaTable.getEntityName();
		if (this.copyInsertStatements == null) {
			this.copyInsertStatements = new HashMap<String, PreparedStatement>();
		}
		else {
			PreparedStatement stat = this.copyInsertStatements.get(entityName);
			if (stat != null) {
				return stat;
			}
		}
		String sql = this.jpaSqlPool.getJPACopyInsertSql(jpaTable, to, params);
		PreparedStatement stat = this.conn.prepareStatement(sql);
		this.copyInsertStatements.put(entityName, stat);
		return stat;
	}

	public String rendSqlFunction(String function, String... args) {
		return this.connFactory.getDialect().renderFunction(function, args);
	}

	public String formatConstValue(String value) {
		Dialect dialect = this.connFactory.getDialect();
		return dialect.formatConstValue(value);
	}

	public ConnectionFactory getConnectionFactory() {
		return this.connFactory;
	}

	public Connection getConnection() {
		return this.conn;
	}
}
