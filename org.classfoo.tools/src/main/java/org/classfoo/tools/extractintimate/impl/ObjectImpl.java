package org.classfoo.tools.extractintimate.impl;

import java.util.Collections;
import java.util.List;

import org.classfoo.tools.extractintimate.Object;

public class ObjectImpl implements Object {

	private String name;

	private List<String> aliasnames;

	private int type;

	public ObjectImpl(String name, List<String> aliasnames, int type) {
		this.name = name;
		this.aliasnames = aliasnames;
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public List<String> getAliasNames() {
		if (this.aliasnames == null || this.aliasnames.isEmpty()) {
			return Collections.emptyList();
		}
		return this.aliasnames;
	}

	public int getType() {
		return this.type;
	}

}
