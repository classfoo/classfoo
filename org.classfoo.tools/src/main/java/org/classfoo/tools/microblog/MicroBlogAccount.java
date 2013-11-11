package org.classfoo.tools.microblog;

/**
 * 微博账号
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-10
 */
public interface MicroBlogAccount extends MicroBlogUser {

	/**
	 * 密码
	 * @return
	 */
	public String getPassword();

	/**
	 * 邮件名
	 * @return
	 */
	public String getEmail();
}
