package org.classfoo.tools.extractintimate;

import java.util.List;

/**
 * 角色接口，描述了文章中的人物或者物品
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-5
 */
public interface Role extends Object {

	/**
	 * 获取角色性别
	 * @return
	 */
	public int getSex();

	/**
	 * 获取角色年龄
	 * @return
	 */
	public int getAge();

	/**
	 * 获取角色教育程度
	 * @return
	 */
	public int getEducation();

	/**
	 * 获取角色的兴趣爱好
	 * @return
	 */
	public List<String> getHobbits();

	/**
	 * 获取角色的工作，第一个为主要工作，按照顺序排列重要程度
	 * @return
	 */
	public List<String> getJobs();
}
