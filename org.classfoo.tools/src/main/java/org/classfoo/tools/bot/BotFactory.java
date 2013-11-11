package org.classfoo.tools.bot;

import java.util.Map;

/**
 * 网络机器人工厂
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-10
 */
public interface BotFactory {

	/**
	 * 获取工厂名称
	 * @return
	 */
	public String getName();

	/**
	 * 创建特定名称的机器人
	 * @param name
	 * @param options
	 * @return
	 */
	public Bot createBot(String name, Map<String, String> options);

}
