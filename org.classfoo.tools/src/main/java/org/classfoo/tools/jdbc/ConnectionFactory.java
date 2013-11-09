package org.classfoo.tools.jdbc;

import java.sql.Connection;

public interface ConnectionFactory {

	DDLUtils getDDLUtils();

	Connection getConnection();

	DbMetaData getDbMetaData();

	Dialect getDialect();

}
