package org.classfoo.tools.jdbc.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import org.classfoo.tools.jdbc.ConnectionManager;
import org.springframework.stereotype.Component;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 对数据库连接的包装
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-6
 */
@Component
public class ConnectionManagerImpl implements ConnectionManager {

	private ComboPooledDataSource ds;

	public ConnectionManagerImpl() {
		try {
			Properties props = loadProperties();
			ds = new ComboPooledDataSource();
			ds.setDriverClass(props.getProperty("driverClass"));
			ds.setJdbcUrl(props.getProperty("jdbcUrl"));
			ds.setUser(props.getProperty("user"));
			ds.setPassword(props.getProperty("password"));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Properties loadProperties() throws IOException {
		Properties props = new Properties();
		InputStream in = ConnectionManagerImpl.class.getResourceAsStream("config.properties");
		try {
			props.load(in);
		}
		finally {
			in.close();
		}
		return props;
	}

	public Connection getConnection() {
		Connection conn = null;
		try {
			conn = ds.getConnection();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return conn;
	}
}
