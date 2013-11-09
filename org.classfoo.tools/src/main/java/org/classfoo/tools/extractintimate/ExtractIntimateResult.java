package org.classfoo.tools.extractintimate;

import java.util.List;

public interface ExtractIntimateResult {

	/**
	 * 获取角色列表
	 * @return
	 */
	public List<Role> getRoles();

	/**
	 * 获取多个个角色之间的关联程度，即是在同一个句子中出现的频次
	 * @param roles
	 * @param exactly 为true的时候仅返回与有且仅有roles在一个句中的频次，为false时返回有roles在一个句子中的频次
	 * @return
	 */
	public int getIntimate(boolean exactly, Role... roles);
}
