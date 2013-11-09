package org.classfoo.tools.jdbc;

public interface InsertDDL {

	void setTableName(String tableName);

	void addColumn(String columnName);

	void addColumnValue(String columnName, String string);

}
