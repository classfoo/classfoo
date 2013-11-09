package org.classfoo.tools.jdbc;

public interface DeleteDDL {

	void setTableName(String tableName);

	void addWhereColumn(String columnName);

}
