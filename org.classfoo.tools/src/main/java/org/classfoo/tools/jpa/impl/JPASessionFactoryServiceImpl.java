package org.classfoo.tools.jpa.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.classfoo.tools.jdbc.ConnectionFactoryManager;
import org.classfoo.tools.jdbc.SQLFactory;
import org.classfoo.tools.jpa.JPASessionFactory;
import org.classfoo.tools.jpa.JPASessionFactoryService;
import org.classfoo.tools.jpa.JPASqlPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPASessionFactoryService服务的实现，内部持有一个Map，同名称来唯一标识一个JPASessionFactory
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-4-19
 */
public class JPASessionFactoryServiceImpl implements JPASessionFactoryService {

	@Autowired
	private ConnectionFactoryManager connFactoryMgr;

	@Autowired
	private SQLFactory sqlFactory;

	@Autowired
	private JPASqlPool jpaSqlPool;

	private Map<String, JPASessionFactory> map = new HashMap<String, JPASessionFactory>(5);

	public synchronized JPASessionFactory getJPASessionFactory(String name) {
		if (map.containsKey(name)) {
			return map.get(name);
		}
		JPASessionFactoryImpl factory = new JPASessionFactoryImpl(name, this, connFactoryMgr, sqlFactory, jpaSqlPool);
		map.put(name, factory);
		return factory;
	}

	public synchronized Collection<String> getNames() {
		return Collections.unmodifiableSet(map.keySet());
	}

	public synchronized JPASessionFactory remove(String name) {
		if (!map.containsKey(name)) {
			return null;
		}
		return map.remove(name);
	}
}
