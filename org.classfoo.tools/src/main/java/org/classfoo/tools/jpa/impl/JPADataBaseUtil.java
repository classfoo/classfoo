package org.classfoo.tools.jpa.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.classfoo.tools.jdbc.ColumnTypes;
import org.classfoo.tools.jdbc.ConnectionFactory;
import org.classfoo.tools.jdbc.CreateTable;
import org.classfoo.tools.jdbc.DDLUtils;
import org.classfoo.tools.jdbc.DbMetaData;
import org.classfoo.tools.jdbc.TableMetaData;
import org.classfoo.tools.jpa.JPAField;
import org.classfoo.tools.jpa.JPASession;
import org.classfoo.tools.jpa.JPASessionFactory;
import org.classfoo.tools.jpa.JPATable;

/**
 * 通过JPA注解生成数据库表，修改数据库表的工具类
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-6-28
 */
public class JPADataBaseUtil {

	private static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * 根据JPATable解析的内容创建数据库表
	 * @param tables
	 * @throws SQLException
	 */
	public static void createDbTables(ConnectionFactory connectionFactory, ArrayList<JPATable> tables)
			throws SQLException {
		DDLUtils ddlutils = connectionFactory.getDDLUtils();
		Connection conn = connectionFactory.getConnection();
		try {
			for (int i = 0; i < tables.size(); i++) {
				JPATable table = tables.get(i);
				String dbTableName = table.getTableName();
				if (dbTableName == null) {
					continue;
				}
				boolean hasTable = ddlutils.isTable(conn, dbTableName);
				if (hasTable) {//暂时不重构表
					//从数据库表中读取不存在的字段，加载进入JPATable中，以便支持对它们的读写
					DbMetaData meta = connectionFactory.getDbMetaData();
					TableMetaData tableMeta = meta.getTableMetaData(conn, dbTableName);
					((JPATableImpl) table).addExtraFields(tableMeta);
					continue;
				}
				CreateTable provider = ddlutils.getCreateTableProvider(conn, dbTableName);
				List<String> fieldNames = table.getFieldNames();
				ArrayList<String> primaryKeys = new ArrayList<String>();
				for (int j = 0; j < fieldNames.size(); j++) {
					String fieldName = fieldNames.get(j);
					JPAField field = table.getField(fieldName);
					char coltype = field.getColumnType();
					int collen = field.getColumnLength();
					int colscale = field.getColumnScale();
					boolean nullable = field.isColumnNullable();
					boolean unique = field.isColumnUnique();
					String colname = field.getColumnName();
					provider.addColumn(colname, coltype, collen, colscale, null, nullable, unique, null);
					if (field.isPrimary()) {
						primaryKeys.add(colname);
					}
				}
				if (!primaryKeys.isEmpty()) {//为空时设置了，会导致建表失败
					provider.setPrimaryKey(primaryKeys.toArray(new String[] {}));
				}
				provider.repairTable();
			}
		}
		finally {
			conn.close();
		}
	}

	/**
	 * 通过查询的ResultSet，构造特定类型的对象
	 * @param factory
	 * @param type
	 * @param table
	 * @param result
	 * @param session
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 * @throws IOException
	 */
	public static <T> T createObjectWithResultSet(JPASessionFactory factory, Class<T> type, JPATable table,
			ResultSet result, JPASession session) throws InstantiationException, IllegalAccessException, SQLException,
			IOException {
		T instance = JPAUtil.createInstance(type, table, factory, session);
		Map<String, Object> beanMap = JPAUtil.getObjectMap(instance);
		ResultSetMetaData md = result.getMetaData();
		int columnCount = md.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			String columnName = md.getColumnLabel(i + 1);
			JPAField jpaField = table.getFieldByColumnName(columnName);
			if (jpaField == null) {
				continue;
			}
			Class<?> fieldType = jpaField.getFieldType();
			Object value = getResultWithType(result, i + 1, fieldType);
			if (value == null) {
				continue;
			}
			String fieldName = jpaField.getName();
			try {
				beanMap.put(fieldName, value);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return instance;
	}

	/**
	 * 通过查询的ResultSet，构造特定类型的对象。将只会把查询的第一列数据填充到type中
	 * @param type
	 * @param result
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 * @throws IOException 
	 */
	public static <T> T createObjectWithResultSet(Class<T> type, ResultSet result) throws InstantiationException,
			IllegalAccessException, SQLException, IOException {
		/**
		 * 20120724
		 * 过去这里通过result的columncount来判断是否要使用beanmap对type的值进行填充，但由于接口
		 * 传入的sql经过了几次包装，例如加入分页设置，导致select的column数可能与原始sql不一致，
		 * 并且传入的type也不一定存在无参数构造器，因此这里简化处理，凡是传入非jpaTable的type，都
		 * 只取查询语句第一列进行值填充
		 */
		Object value = getResultWithType(result, 1, type);
		if (value == null) {
			return null;
		}
		return type.cast(value);
	}

	private static Object getResultWithType(ResultSet result, int index, Class<?> type) throws SQLException {
		if (String.class.equals(type)) {
			return result.getString(index);
		}
		if (Long.class.equals(type) || long.class.equals(type)) {
			return result.getLong(index);
		}
		if (Integer.class.equals(type) || int.class.equals(type)) {
			return result.getInt(index);
		}
		if (Double.class.equals(type) || double.class.equals(type)) {
			return result.getDouble(index);
		}
		if (Float.class.equals(type) || float.class.equals(type)) {
			return result.getFloat(index);
		}
		if (Byte.class.equals(type) || byte.class.equals(type)) {
			return result.getByte(index);
		}
		if (Byte[].class.equals(type) || byte[].class.equals(type)) {
			return result.getBytes(index);
		}
		if (Boolean.class.equals(type) || boolean.class.equals(type)) {
			return result.getBoolean(index);
		}
		if (Blob.class.equals(type)) {
			return result.getBlob(index);
		}
		if (Clob.class.equals(type)) {
			return result.getClob(index);
		}
		return result.getObject(index);
	}

	//	private static Object convertColumnToFieldType(Object value, int columnType, Class<?> fieldType)
	//			throws IOException, SQLException {
	//		if (columnType == Types.BLOB || columnType == Types.LONGVARBINARY) {
	//			Blob blob = (Blob) value;
	//			Object object = JPADataBaseUtil.getBlobValue(blob, fieldType);
	//			return object;
	//		}
	//		else if (columnType == Types.CLOB||columnType == Types.LONGVARCHAR) {
	//			Clob clob = (Clob) value;
	//			Object object = JPADataBaseUtil.getClobValue(clob, fieldType);
	//			return object;
	//		}
	//		else if(boolean.class.equals(fieldType)||Boolean.class.equals(fieldType)){
	//			return BooleanUtils.toBoolean(value);
	//		}
	////		else if(columnType == Types.BIT||columnType == Types.BOOLEAN){
	////			Boolean bool = (Boolean)value;
	////			return bool;
	////		}
	//		return value;
	//	}

	/**
	 * 将Blob对象转换为特定的对象，支持Blob，String和byte[]的转换
	 * @param blob
	 * @param fieldType
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static Object getBlobValue(Blob blob, Class<?> fieldType) throws IOException, SQLException {
		if (Blob.class.equals(fieldType)) {
			return fieldType.cast(blob);
		}
		else if (String.class.equals(fieldType)) {
			InputStream is = blob.getBinaryStream();
			try {
				String str = IOUtils.toString(is, DEFAULT_ENCODING);
				return str;
			}
			finally {
				is.close();
			}
		}
		else if (byte[].class.equals(fieldType)) {
			InputStream is = blob.getBinaryStream();
			try {
				byte[] value = IOUtils.toByteArray(is);
				return value;
			}
			finally {
				is.close();
			}
		}
		throw new RuntimeException("不支持将Blob类型转换为" + fieldType + "类型！");
	}

	/**
	 * 将Clob对象转换为特定的对象，支持Clob，String和byte[]的转换
	 * @param clob
	 * @param fieldType
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 */
	public static Object getClobValue(Clob clob, Class<?> fieldType) throws IOException, SQLException {
		if (Clob.class.equals(fieldType)) {
			return clob;
		}
		else if (String.class.equals(fieldType)) {
			Reader reader = clob.getCharacterStream();
			try {
				String value = IOUtils.toString(reader);
				return value;
			}
			finally {
				reader.close();
			}
		}
		else if (byte[].class.equals(fieldType)) {
			Reader reader = clob.getCharacterStream();
			try {
				byte[] value = IOUtils.toByteArray(reader, DEFAULT_ENCODING);
				return value;
			}
			finally {
				reader.close();
			}
		}
		throw new RuntimeException("不支持将Blob类型转换为" + fieldType + "类型！");
	}

	/**
	 * 对select * from，且type中包含延迟加载属性的sql改造为延迟加载的查询sql，即是将＊替换为非延
	 * 迟加载字段列表
	 * @param factory
	 * @param type
	 * @param sql
	 * @return
	 */
	public static String patternLazyLoaderSql(JPASessionFactory factory, Class<?> type, String sql) {
		if (!sql.startsWith("select * from")) {//进行延迟加载改造
			return sql;
		}
		JPATable jpaTable = factory.getJPATable(type);
		if (jpaTable == null) {
			return sql;
		}
		List<String> lazyLoaders = jpaTable.getLazyLoadFieldNames();
		if (lazyLoaders == null || lazyLoaders.isEmpty()) {
			return sql;
		}
		List<String> fieldNames = jpaTable.getFieldNames();
		boolean hasSel = false;
		int fieldSize = fieldNames.size();
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		for (int i = 0; i < fieldSize; i++) {
			String fieldName = fieldNames.get(i);
			JPAField jpaField = jpaTable.getField(fieldName);
			if (jpaField.isLazyLoad()) {
				continue;
			}
			if (i != 0 && hasSel) {
				sb.append(',');
			}
			hasSel = true;
			sb.append(jpaField.getColumnName());
		}
		sb.append(" from ");
		sb.append(sql.substring(13));
		return sb.toString();
	}

	/**
	 * 对sql中的entity名称进行表名替换，sql中将以${entityName}方式来代替表名，这个方法会检测出sql
	 * 字符串中所有的entity名称，将之替换为对应的数据库表名称后返回
	 * @param factory
	 * @param sql
	 * @param throwException 设置替换表时如果表不存在是否抛出异常，为false时，遇到表不存在，直接返回原sql
	 * @return
	 */
	public static String patternEntityNameSql(JPASessionFactory factory, String sql, boolean throwException) {
		if (sql == null) {
			return null;
		}
		int pos = sql.indexOf("${");
		if (pos == -1) {
			return sql;
		}
		StringBuilder sb = new StringBuilder(sql.length() + 100);
		sb.append(sql.subSequence(0, pos));
		boolean loop = true;
		while (loop) {
			int length = sql.length();
			if (length < pos + 3) {
				sb.append(sql.subSequence(pos, length));
				break;
			}
			int closeindex = sql.indexOf('}', pos + 2);
			if (closeindex == -1) {
				sb.append(sql.substring(pos, sql.length()));
				break;
			}
			String entityName = sql.substring(pos + 2, closeindex);
			JPATable table = factory.getJPATable(entityName);
			if (table == null) {//如果不存在，原样输出
				if (throwException) {
					throw new RuntimeException("解析sql：" + sql + "失败， JPA中没有注册名为" + entityName + "的注解类！");
				}
				//不抛异常，且表不存在，直接返回原sql
				return sql;
			}
			String tablename = table.getTableName();
			sb.append(tablename);
			pos = closeindex;
			int nextpos = sql.indexOf("${", pos + 1);
			if (nextpos == -1) {
				sb.append(sql.substring(pos + 1, sql.length()));
				break;
			}
			sb.append(sql.substring(pos + 1, nextpos));
			pos = nextpos;
		}
		return sb.toString();
	}

	/**
	 * 读取ResultSet，检测其是否只有一条记录，多于一条记录即抛出异常。如果只有一条记录，将其字段
	 * 内容抽取为单个Object，或者Object数组
	 * @param result
	 * @return
	 * @throws SQLException
	 */
	public static Object convertResultSetToUnique(ResultSet result) throws SQLException {
		boolean hasNext = result.next();
		if (!hasNext) {
			return null;
		}
		ResultSetMetaData md = result.getMetaData();
		int columnCount = md.getColumnCount();
		if (columnCount == 1) {
			Object obj = result.getObject(1);
			hasNext = result.next();
			if (hasNext) {
				throw new RuntimeException("执行queryUnique，查询结果不止一条记录！");
			}
			return obj;
		}
		else {
			Object[] list = new Object[columnCount];
			for (int i = 0; i < columnCount; i++) {
				Object obj = result.getObject(i + 1);
				list[i] = obj;
			}
			hasNext = result.next();
			if (hasNext) {
				throw new RuntimeException("执行queryUnique，查询结果不止一条记录！");
			}
			return list;
		}
	}

	/**
	 * 读取ResultSet，检测其是否只有一条记录，多于一条记录即抛出异常。如果只有一条记录，将其字段
	 * 内容抽取为单个Object，或者Object数组
	 * @param factory
	 * @param type
	 * @param result
	 * @param session
	 * @return
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public static <T> T convertResultSetToUnique(JPASessionFactory factory, Class<T> type, JPATable table,
			ResultSet result, JPASession session) throws SQLException, InstantiationException, IllegalAccessException,
			IOException {
		boolean hasNext = result.next();
		if (!hasNext) {
			return null;
		}
		T instance = null;
		if (table == null) {//兼容非JPA注册类型的对象，这时使用字段名对应属性名
			instance = createObjectWithResultSet(type, result);
		}
		else {
			instance = createObjectWithResultSet(factory, type, table, result, session);
		}
		hasNext = result.next();
		if (hasNext) {
			throw new RuntimeException("执行queryUnique，查询结果不止一条记录！");
		}
		return instance;
	}

	/**
	 * 将一个ResultSet对象内容提取，组装到一个Object列表中，每一个Object项代表了一条数据行，而
	 * Object项根据查询sql的column值，如果column只有一个，将是一个Object对象，否则是一个Object
	 * 列表
	 * @param result
	 * @return
	 * @throws SQLException
	 */
	public static List<Object> convertResultSetToList(ResultSet result) throws SQLException {
		ResultSetMetaData md = result.getMetaData();
		int columnCount = md.getColumnCount();
		List<Object> list = new ArrayList<Object>();
		while (result.next()) {
			if (columnCount == 1) {
				Object obj = result.getObject(1);
				list.add(obj);
			}
			else {
				Object[] columns = new Object[columnCount];
				for (int i = 0; i < columnCount; i++) {
					Object obj = result.getObject(i + 1);
					columns[i] = obj;
				}
				list.add(columns);
			}
		}
		return list;
	}

	/**
	 * 将一个ResultSet对象内容提取，组装到一个Object列表中，每一个Object项代表了一条数据行，而
	 * Object项根据查询sql的column值，如果column只有一个，将是一个Object对象，否则是一个Object
	 * 列表
	 * @param factory
	 * @param type
	 * @param result
	 * @param session
	 * @return
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	public static <T> List<T> convertResultSetToList(JPASessionFactory factory, Class<T> type, JPATable table,
			ResultSet result, JPASession session) throws SQLException, InstantiationException, IllegalAccessException,
			IOException {
		List<T> list = new ArrayList<T>();
		while (result.next()) {
			if (table == null) {//如果type没有被注册，那么将把查询结果中第一列填充到该对象中
				T instance = createObjectWithResultSet(type, result);
				list.add(instance);
			}
			else {
				T instance = createObjectWithResultSet(factory, type, table, result, session);
				list.add(instance);
			}
		}
		return list;
	}

	/**
	 * 根据数据库字段获取一个适合的java类型
	 * @param type
	 * @return
	 */
	public static Class<?> getFieldTypeByColumnType(char type) {
		switch (type) {
			case ColumnTypes.BLOB:
				return Blob.class;
			case ColumnTypes.CLOB:
				return Clob.class;
			case ColumnTypes.TIMESTAMP:
				return Timestamp.class;
			case ColumnTypes.VARCHAR:
				return String.class;
			case ColumnTypes.NUMERIC:
				return Double.class;
			case ColumnTypes.INTEGER:
				return Long.class;
			default:
				return String.class;
		}
	}
}
