package org.classfoo.tools.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringContext {

	public static final ApplicationContext initSpringContext() {
		return new ClassPathXmlApplicationContext("/META-INF/org.classfoo.tools.spring.xml");
	}
}
