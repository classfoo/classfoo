package org.classfoo.tools.jdbc;


public interface TableMetaData {

	int getColumnCount();

	TableColumnMetaData getColumn(int i);

}
