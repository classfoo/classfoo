package org.classfoo.tools.extractintimate.impl;

import java.util.Collections;
import java.util.List;

import org.classfoo.tools.extractintimate.Role;

/**
 * @see Role
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-3
 */
public class RoleImpl extends ObjectImpl implements Role {

	public RoleImpl(String name, List<String> aliasnames, int type) {
		super(name, aliasnames, type);
	}

	public int getSex() {
		return 0;
	}

	public int getAge() {
		return 0;
	}

	public int getEducation() {
		return 0;
	}

	public List<String> getHobbits() {
		return null;
	}

	public List<String> getJobs() {
		return null;
	}
}
