package org.classfoo.tools.jdbc;

import java.sql.Connection;

public interface DDLUtils {

	boolean isTable(Connection conn, String dbTableName);

	CreateTable getCreateTableProvider(Connection conn, String dbTableName);

}
