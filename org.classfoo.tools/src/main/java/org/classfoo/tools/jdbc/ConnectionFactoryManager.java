package org.classfoo.tools.jdbc;

public interface ConnectionFactoryManager {

	ConnectionFactory get(String datasource);

	ConnectionFactory getDefaultConnectionFactory();

}
