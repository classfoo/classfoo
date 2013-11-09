package org.classfoo.tools.jdbc;

public interface TableColumnMetaData {

	String getColumnName();

	char getType();

	int getLength();

	boolean isUnique();

	boolean isNullable();

	int getScale();

}
