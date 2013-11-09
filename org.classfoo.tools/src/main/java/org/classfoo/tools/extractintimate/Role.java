package org.classfoo.tools.extractintimate;

import java.util.List;

/**
 * 角色接口，描述了文章中的人物或者物品
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-5
 */
public interface Role {

	public static final int TYPE_MALE = 0;

	public static final int TYPE_FEMALE = 1;

	public static final int TYPE_OBJECT = 2;

	/**
	 * 角色的全名
	 * @return
	 */
	public String getName();

	/**
	 * 角色的别名列表，例如贾宝玉的别名：宝玉
	 * @return
	 */
	public List<String> getAliasNames();

	/**
	 * 判定角色类型，可以为男人，女人，物品
	 * @return
	 */
	public int getType();
}
