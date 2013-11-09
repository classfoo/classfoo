package org.classfoo.tools.jdbc;

public interface CreateTable {

	void addColumn(String colname, char coltype, int collen, int colscale, Object object, boolean nullable,
			boolean unique, Object object2);

	void setPrimaryKey(String[] array);

	void repairTable();

}
