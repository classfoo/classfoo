package org.classfoo.tools.microblog;

import java.util.List;

/**
 * 微博用户，即是在微博中活跃的人物
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-10
 */
public interface MicroBlogUser {

	/**
	 * 微博用户名
	 * @return
	 */
	public String getName();

	/**
	 * 获取微博用户的性别
	 * @return
	 */
	public int getGender();

	/**
	 * 获取微博用户的年龄，不一定能够获取到
	 * @return
	 */
	public int getAge();

	/**
	 * 获取微博用户的标签列表，一个用户可能常常修改自己的标签
	 * @return
	 */
	public List<String> getLabels();

	/**
	 * 微博用户名称可能发生变化，可以一个人或者机构具有多个账号
	 * @return
	 */
	public List<String> getAliasName();
}
