package org.classfoo.tools.extractintimate.impl;

import java.util.Collections;
import java.util.List;

import org.classfoo.tools.extractintimate.ExtractIntimateProperties;
import org.classfoo.tools.extractintimate.Role;

public class ExtractIntimatePropertiesImpl implements ExtractIntimateProperties {

	private char thirdmale;

	private char thirdfemale;

	private char[] ends;

	private List<Role> roles;

	public char getThirdPersonMale() {
		return this.thirdmale;
	}

	public void setThirdPersonMale(char male) {
		this.thirdmale = male;
	}

	public char getThirdPersonFemale() {
		return this.thirdfemale;
	}

	public void setThirdPersonFemale(char female) {
		this.thirdfemale = female;
	}

	public char[] getSetenceEnds() {
		return this.ends;
	}

	public void setSetenceEnds(char[] ends) {
		this.ends = ends;
	}

	public List<Role> getRoles() {
		if (this.roles == null || this.roles.isEmpty()) {
			return Collections.emptyList();
		}
		return this.roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

}
