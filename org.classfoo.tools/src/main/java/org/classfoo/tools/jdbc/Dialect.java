package org.classfoo.tools.jdbc;

public interface Dialect {

	String renderFunction(String function, String[] args);

	String formatConstValue(String value);

	String getLimitString(String sql, int startindex, int pagesize);

	String getInsertIntoSql(String tablename, String string, String string2, boolean b);

}
