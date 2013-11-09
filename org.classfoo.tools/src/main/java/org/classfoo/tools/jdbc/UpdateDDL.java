package org.classfoo.tools.jdbc;

public interface UpdateDDL {

	void setTableName(String tableName);

	void addTable(String tableName, String aliasname, Object object, String aliaswhere);

	void addColumnWithExpression(String colname, String string);

	void setWhere(String mainwhere);

	void addWhereColumn(String upperCase);

	void addColumn(String columnName);

}
