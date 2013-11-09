package org.classfoo.tools.extractintimate;

import java.util.List;

public interface ExtractIntimateProperties {

	/**
	 * 获取第三人称男性，例如”他“，"he"
	 * @return
	 */
	public char getThirdPersonMale();

	public void setThirdPersonMale(char male);

	/**
	 * 获取第三人称女性，例如"她", "she"
	 * @return
	 */
	public char getThirdPersonFemale();

	public void setThirdPersonFemale(char female);

	/**
	 * 获取表达一个句子结束的标识字符，例如'。','?'
	 * @return
	 */
	public char[] getSetenceEnds();

	public void setSetenceEnds(char[] ends);

	/**
	 * 获取需要分析的角色列表
	 * @return
	 */
	public List<Role> getRoles();

	public void setRoles(List<Role> roles);
}
