package org.classfoo.tools.extractintimate;

import java.util.List;

public interface Object {

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
