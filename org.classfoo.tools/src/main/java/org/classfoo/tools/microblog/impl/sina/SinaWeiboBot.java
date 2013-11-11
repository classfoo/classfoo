package org.classfoo.tools.microblog.impl.sina;

import org.classfoo.tools.bot.Bot;
import org.classfoo.tools.bot.BotFactory;

/**
 * 获取新浪微博机器人
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-10
 */
public class SinaWeiboBot implements Bot {

	private String name;

	private SinaWeiboBotFactory factory;

	public SinaWeiboBot(String name, SinaWeiboBotFactory factory) {
		this.name = name;
		this.factory = factory;
	}

	public String getName() {
		return this.name;
	}

	public BotFactory getFactory() {
		return this.factory;
	}

	public void run() {
		
	}

	public void close() {

	}

}
