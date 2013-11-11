package org.classfoo.tools.bot;

/**
 * 网络机器人
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-10
 */
public interface Bot extends Runnable {

	/**
	 * 获取机器人名称
	 * @return
	 */
	public String getName();

	/**
	 * 获取机器人的制造工厂
	 * @return
	 */
	public BotFactory getFactory();

	/**
	 * 关闭机器人
	 */
	public void close();
}
