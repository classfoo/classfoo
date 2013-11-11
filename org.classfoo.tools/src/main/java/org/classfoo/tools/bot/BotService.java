package org.classfoo.tools.bot;

import java.util.List;

/**
 * 网络机器人服务类
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-10
 */
public interface BotService {

	/**
	 * 获取特定名称的机器人工厂
	 * @param name
	 * @return
	 */
	public BotFactory getBotFactory(String name);

	/**
	 * 将机器人纳入执行队列
	 * @param bot
	 */
	public void schedule(Bot bot);

	/**
	 * 获取当前正在允许的机器人列表
	 * @return
	 */
	public List<Bot> getRunningBots();
}
