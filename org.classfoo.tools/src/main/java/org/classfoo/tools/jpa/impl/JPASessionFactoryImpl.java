package org.classfoo.tools.jpa.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.classfoo.tools.jdbc.ConnectionFactory;
import org.classfoo.tools.jdbc.ConnectionFactoryManager;
import org.classfoo.tools.jdbc.SQLFactory;
import org.classfoo.tools.jpa.JPAObject;
import org.classfoo.tools.jpa.JPASession;
import org.classfoo.tools.jpa.JPASessionFactory;
import org.classfoo.tools.jpa.JPASessionFactoryService;
import org.classfoo.tools.jpa.JPASqlPool;
import org.classfoo.tools.jpa.JPATable;

/**
 * JPASessionFactory的Spring环境下的实现类
 * TODO：尚未对线程并发做太多处理，有待改进
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-7-9
 */
public class JPASessionFactoryImpl implements JPASessionFactory {

	private ConnectionFactoryManager connFactoryMgr;

	private SQLFactory sqlFactory;

	private JPASqlPool jpaSqlPool;

	private String datasource = "default";

	private Map<String, JPATable> map = new HashMap<String, JPATable>(50);

	/**
	 * ISSUE BI-5975: 增加对class到JPATable的map作为cache，避免重复查询Class的name
	 */
	private Map<Class<?>, JPATable> classmap = new HashMap<Class<?>, JPATable>(50);

	private JPASessionFactoryService service;

	private String name;

	public JPASessionFactoryImpl(String name, JPASessionFactoryService service,
			ConnectionFactoryManager connFactoryMgr, SQLFactory sqlFactory, JPASqlPool jpaSqlPool) {
		this.name = name;
		this.service = service;
		this.connFactoryMgr = connFactoryMgr;
		this.sqlFactory = sqlFactory;
		this.jpaSqlPool = jpaSqlPool;
	}

	public String getName() {
		return this.name;
	}

	public JPASessionFactoryService getService() {
		return this.service;
	}

	public void setDataSource(String datasource) {
		this.datasource = datasource;
	}

	public String getDataSource() {
		return this.datasource;
	}

	public List<JPATable> regist(Class<?>... annotatedClasses) {
		if (annotatedClasses == null || annotatedClasses.length == 0) {
			return Collections.emptyList();
		}
		//第一步：将传入的注解类进行解析，得到JPATable列表
		ArrayList<JPATable> tables = JPAAnnotationUtil.parseAnnotatedClasses(annotatedClasses);
		//第二步：通过JPATable列表去创建表，总是创建新数据库表
		try {
			ConnectionFactory connectionFactory = this.getConnectionFactory();
			JPADataBaseUtil.createDbTables(connectionFactory, tables);
		}
		catch (SQLException e) {
			throw new RuntimeException("无法创建数据库表！", e);
		}
		//第三步：锁定，将JPATable列表纳入内部map，替换新旧数据库表
		for (int i = 0; i < tables.size(); i++) {
			JPATable table = tables.get(i);
			map.put(table.getEntityName(), table);
		}
		return tables;
	}

	public JPASession openSession() {
		try {
			ConnectionFactory connFactory = getConnectionFactory();
			Connection conn = connFactory.getConnection();
			return new JPASessionImpl(this, connFactory, sqlFactory, conn, this.jpaSqlPool);
		}
		catch (Exception e) {
			throw new RuntimeException((this.datasource == null || "default".equals(this.datasource) ? "默认"
					: this.datasource) + "连接池连接失败，请检查数据库是否正常", e);
		}
	}

	/**
	 * 获取数据库连接池工厂类
	 * @return
	 */
	private ConnectionFactory getConnectionFactory() {
		ConnectionFactory connFactory = this.connFactoryMgr.get(datasource);
		if (connFactory == null) {
			throw new RuntimeException("DataSource " + datasource + " Not Exist！");
		}
		return connFactory;
	}

	public JPATable getJPATable(Class<?> type) {
		if (this.classmap.containsKey(type)) {
			return this.classmap.get(type);
		}
		String entityName = JPAUtil.getEntityName(type);
		JPATable table = this.getJPATable(entityName);
		this.classmap.put(type, table);
		return table;
	}

	public JPATable getJPATable(String name) {
		if (this.map == null || this.map.isEmpty()) {
			return null;
		}
		return this.map.get(name);
	}

	public boolean isRegisted(Class<?> type) {
		if (type.isAssignableFrom(JPAObject.class)) {//如果是JPAObject
			return true;
		}
		if (this.classmap.containsKey(type)) {
			return true;
		}
		String entityName = JPAUtil.getEntityName(type);
		return this.isRegisted(entityName);
	}

	public boolean isRegisted(String name) {
		if (this.map == null || this.map.isEmpty()) {
			return false;
		}
		return this.map.containsKey(name);
	}

	public String[] getJPATableNames() {
		return map.keySet().toArray(new String[map.size()]);
	}
}
