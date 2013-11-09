package org.classfoo.tools.jdbc;

import java.sql.Connection;

public interface DbMetaData {

	TableMetaData getTableMetaData(Connection conn, String dbTableName);

	TableMetaData getTableMetaData(String tablename);

}
