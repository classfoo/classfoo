package org.classfoo.tools.jpa.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.classfoo.tools.jdbc.ConnectionFactoryManager;
import org.classfoo.tools.jdbc.DbMetaData;
import org.classfoo.tools.jdbc.DeleteDDL;
import org.classfoo.tools.jdbc.Dialect;
import org.classfoo.tools.jdbc.InsertDDL;
import org.classfoo.tools.jdbc.SQLContext;
import org.classfoo.tools.jdbc.SQLFactory;
import org.classfoo.tools.jdbc.TableMetaData;
import org.classfoo.tools.jdbc.UpdateDDL;
import org.classfoo.tools.jpa.JPAField;
import org.classfoo.tools.jpa.JPASqlPool;
import org.classfoo.tools.jpa.JPATable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 通过JPASession的get，update，delete，save进行对象操作时，各个对象产生的sql在模型没有发生变
 * 化情况下都是固定的，因此在这里对这些sql进行简单的缓存，避免重复构造sql
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-7-9
 */
//@Component
//@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JPASqlPoolImpl implements JPASqlPool {

	@Autowired
	private ConnectionFactoryManager connFactoryMgr;

	@Autowired
	private SQLFactory sqlFactory;

	private Map<String, String> savesqls = new HashMap<String, String>();

	private Map<String, String> querysqls = new HashMap<String, String>();

	private Map<String, String> deletesqls = new HashMap<String, String>();

	private Map<String, String> updatesqls = new HashMap<String, String>();

	private Map<String, String> copysqls = new HashMap<String, String>();

	private Map<String, String> copyinsertsqls = new HashMap<String, String>();

	private SQLContext context = new SQLContext() {
		public TableMetaData getTableMetaData(String tablename) {
			DbMetaData meta = connFactoryMgr.getDefaultConnectionFactory().getDbMetaData();
			try {
				return meta.getTableMetaData(tablename);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	};

	public String getJPASaveSql(JPATable table) {
		Map<String, String> savesqls = this.savesqls;
		String entityName = table.getEntityName();
		if (savesqls.containsKey(entityName)) {
			return savesqls.get(entityName);
		}
		InsertDDL insertDDL = sqlFactory.createInsertDDL();
		insertDDL.setTableName(table.getTableName());
		List<String> fieldnames = table.getFieldNames();
		int size = fieldnames.size();
		for (int i = 0; i < size; i++) {
			String fieldname = fieldnames.get(i);
			JPAField field = table.getField(fieldname);
			String columnName = field.getColumnName();
			insertDDL.addColumn(columnName);
			insertDDL.addColumnValue(columnName, "?");
		}
		String sql = this.sqlFactory.getSql(insertDDL,
				this.connFactoryMgr.getDefaultConnectionFactory().getDbMetaData());
		savesqls.put(entityName, sql);
		return sql;
	}

	public String getJPAQuerySql(JPATable table) {
		Map<String, String> querysqls = this.querysqls;
		String entityName = table.getEntityName();
		if (querysqls.containsKey(entityName)) {
			return querysqls.get(entityName);
		}
		//		SQL sql = this.sqlFactory.createSQL();
		//		FromClause from = sql.from();
		//		Table sqltable = from.addTable();
		//		sqltable.setTableName(table.getTableName());
		//		SelectField selfield = sql.addSelectField();
		//		selfield.setField(ExpEngine.createExpression(sqltable.getAlias() + ".*"));
		//		List<Expression> conditions = sql.where().getConditions();
		//		List<String> pks = table.getPrimaryFieldNames();
		//		int size = pks.size();
		//		for (int i = 0; i < size; i++) {
		//			String pk = pks.get(i);
		//			JPAField pkField = table.getField(pk);
		//			String columnName = pkField.getColumnName();
		//			conditions.add(ExpEngine.createExpression(sqltable.getAlias() + '.' + columnName+"=1"));
		//		}
		//		SQLUtils.compile(sql, this.context);
		//		String result = sql.toString();
		//		querysqls.put(entityName, result);
		//		return result;
		StringBuilder sql = new StringBuilder(200);
		sql.append("select ");
		List<String> fieldNames = table.getFieldNames();
		int fieldSize = fieldNames.size();
		boolean hasSel = false;
		for (int i = 0; i < fieldSize; i++) {
			String fieldName = fieldNames.get(i);
			JPAField field = table.getField(fieldName);
			if (field.isLazyLoad()) {
				continue;
			}
			if (i != 0 && hasSel) {
				sql.append(',');
			}
			hasSel = true;
			String columnName = field.getColumnName();
			sql.append(columnName);
		}
		sql.append(" from ").append(table.getTableName());
		sql.append(" where ");
		List<String> pks = table.getPrimaryFieldNames();
		if (pks == null || pks.isEmpty()) {
			pks = table.getFieldNames();
		}
		int size = pks.size();
		for (int i = 0; i < size; i++) {
			String pk = pks.get(i);
			JPAField pkField = table.getField(pk);
			String columnName = pkField.getColumnName();
			sql.append(columnName).append("=?");
			if (i != size - 1) {
				sql.append(" and ");
			}
		}
		String result = sql.toString();
		querysqls.put(entityName, result);
		return result;
	}

	public String getJPADeleteSql(JPATable table) {
		Map<String, String> deletesqls = this.deletesqls;
		String entityName = table.getEntityName();
		if (deletesqls.containsKey(entityName)) {
			return deletesqls.get(entityName);
		}
		DeleteDDL deleteDDL = this.sqlFactory.createDeleteDDL();
		deleteDDL.setTableName(table.getTableName());
		List<String> pks = table.getPrimaryFieldNames();
		if(pks == null||pks.isEmpty()){
			pks = table.getFieldNames();
		}
		int size = pks.size();
		for (int i = 0; i < size; i++) {
			String pk = pks.get(i);
			JPAField jpaField = table.getField(pk);
			String columnName = jpaField.getColumnName();
			deleteDDL.addWhereColumn(columnName);
		}
		String sql = this.sqlFactory.getSql(deleteDDL,
				this.connFactoryMgr.getDefaultConnectionFactory().getDbMetaData());
		deletesqls.put(entityName, sql);
		return sql;
	}

	public String getJPAUpdateSql(JPATable table) {
		Map<String, String> updatesqls = this.updatesqls;
		String entityName = table.getEntityName();
		if (updatesqls.containsKey(entityName)) {
			return updatesqls.get(entityName);
		}
		UpdateDDL updateDDL = this.sqlFactory.createUpdateDDL();
		updateDDL.setTableName(table.getTableName());
		List<String> fieldnames = table.getFieldNames();
		int fieldsize = fieldnames.size();
		List<String> pknames = table.getPrimaryFieldNames();
		if (pknames == null || pknames.isEmpty()) {
			pknames = table.getFieldNames();
		}
		int pksize = pknames.size();
		for (int i = 0; i < fieldsize; i++) {
			String fieldname = fieldnames.get(i);
			JPAField field = table.getField(fieldname);
			if (field.isPrimary()) {
				continue;
			}
			String columnName = field.getColumnName();
			updateDDL.addColumn(columnName);
		}

		for (int i = 0; i < pksize; i++) {
			String pkfieldname = pknames.get(i);
			JPAField field = table.getField(pkfieldname);
			String columnName = field.getColumnName();
			updateDDL.addWhereColumn(columnName);
		}
		String version = table.getVersionFieldName();
		if (version != null) {
			updateDDL.addWhereColumn(version.toUpperCase());
		}
		String sql = this.sqlFactory.getSql(updateDDL,
				this.connFactoryMgr.getDefaultConnectionFactory().getDbMetaData());
		updatesqls.put(entityName, sql);
		return sql;
	}

	public String getJPACopyUpdateSql(JPATable table) {
		Map<String, String> copysqls = this.copysqls;
		String entityName = table.getEntityName();
		if (copysqls.containsKey(entityName)) {
			return copysqls.get(entityName);
		}
		UpdateDDL update = sqlFactory.createUpdateDDL();
		update.setTableName(table.getTableName());
		String aliasname = "a";
		String aliaswhere = this.getPrimaryKeysWhereConditions(table, aliasname);
		update.addTable(table.getTableName(), aliasname, null, aliaswhere);
		List<String> fields = table.getFieldNames();
		int fieldsize = fields.size();
		for (int i = 0; i < fieldsize; i++) {
			String field = fields.get(i);
			JPAField jpaField = table.getField(field);
			if (jpaField.isPrimary()) {
				continue;
			}
			String colname = jpaField.getColumnName();
			update.addColumnWithExpression(colname, aliasname + '.' + colname);
		}
		String mainwhere = this.getPrimaryKeysWhereConditions(table, null);
		update.setWhere(mainwhere);
		DbMetaData dbMetaData = this.connFactoryMgr.getDefaultConnectionFactory().getDbMetaData();
		String sql = this.sqlFactory.getSql(update, dbMetaData);
		copysqls.put(entityName, sql);
		return sql;
	}

	public String getJPACopyInsertSql(JPATable table, Object to, Map<String, Object> params) {
		Dialect dialect = this.connFactoryMgr.getDefaultConnectionFactory().getDialect();
		String tablename = table.getTableName();
		Map<String, Object> toObjectMap = JPAUtil.getObjectMap(to);
		List<String> fieldnames = table.getFieldNames();
		StringBuilder selectSql = new StringBuilder(100);
		selectSql.append("select ");
		int fieldsize = fieldnames.size();
		StringBuilder fields = new StringBuilder(100);
		for(int i = 0; i < fieldsize;i++){
			String fieldname = fieldnames.get(i);
			JPAField field = table.getField(fieldname);
			String columnname = field.getColumnName();
			if (params != null && params.containsKey(fieldname)) {//copyinsert时支持对某些特定字段指定值
				Object value = params.get(fieldname);
				if (value instanceof String) {//XXX 此处需要防止sql注入
					String str = StringEscapeUtils.escapeSql(value.toString());
					selectSql.append("'").append(str).append("'");
				}
				else {
					String str = StringEscapeUtils.escapeSql(value.toString());
					selectSql.append(str);
				}
			}
			else if (field.isPrimary() || field.isColumnUnique()) {//针对主键字段，唯一字段，必须设置值
				Object value = toObjectMap.get(fieldname);
				if (value instanceof String) {//XXX 此处需要防止sql注入
					String str = StringEscapeUtils.escapeSql(value.toString());
					selectSql.append("'").append(str).append("'");
				}
				else {
					String str = StringEscapeUtils.escapeSql(value.toString());
					selectSql.append(str);
				}
			}
			else {//普通字段，select字段名即可
				selectSql.append(columnname);
			}
			fields.append(columnname);
			if(i != fieldsize-1 ){
				selectSql.append(',');
				fields.append(',');
			}
		}
		selectSql.append(" from ").append(tablename);
		selectSql.append(" where ");
		List<String> pknames = table.getPrimaryFieldNames();
		if (pknames == null || pknames.isEmpty()) {
			pknames = table.getFieldNames();
		}
		int pksize = pknames.size();
		for(int i = 0; i < pksize;i++){
			String pkname = pknames.get(i);
			JPAField pkfield = table.getField(pkname);
			String columnname = pkfield.getColumnName();
			selectSql.append(columnname).append("=? ");
			if(i != pksize-1){
				selectSql.append(" and ");
			}
		}
		String sql = dialect.getInsertIntoSql(tablename, fields.toString(), selectSql.toString(), true);
		return sql;
	}

	/**
	 * 将table中的主键设置提取出来，组装成aliasname.pk1=? and aliasname.pk2=?...的字符串。
	 * aliasname可以为空
	 * @param table
	 * @param aliasname
	 * @return
	 */
	private String getPrimaryKeysWhereConditions(JPATable table, String aliasname) {
		List<String> pknames = table.getPrimaryFieldNames();
		if (pknames == null || pknames.isEmpty()) {
			pknames = table.getFieldNames();
		}
		int pksize = pknames.size();
		StringBuilder result = new StringBuilder(100);
		for (int i = 0; i < pksize; i++) {
			String pkname = pknames.get(i);
			if (aliasname != null) {
				result.append(aliasname).append('.');
			}
			JPAField jpaField = table.getField(pkname);
			result.append(jpaField.getColumnName()).append("=?");
			if (i != pksize - 1) {
				result.append(" and ");
			}
		}
		return result.toString();
	}

	public synchronized void reset() {
		this.querysqls = new HashMap<String, String>();
		this.savesqls = new HashMap<String, String>();
		this.deletesqls = new HashMap<String, String>();
		this.updatesqls = new HashMap<String, String>();
		this.copysqls = new HashMap<String, String>();
	}
}
