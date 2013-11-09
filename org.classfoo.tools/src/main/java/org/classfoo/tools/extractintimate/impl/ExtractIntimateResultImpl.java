package org.classfoo.tools.extractintimate.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.classfoo.tools.extractintimate.ExtractIntimateProperties;
import org.classfoo.tools.extractintimate.ExtractIntimateResult;
import org.classfoo.tools.extractintimate.Role;

/**
 * @see ExtractIntimateResult
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-3
 */
public class ExtractIntimateResultImpl implements ExtractIntimateResult {

	private List<Role> roles;

	private Map<String, Integer> result = new HashMap<String, Integer>(100);

	public ExtractIntimateResultImpl(ExtractIntimateProperties properties) {
		this.roles = properties.getRoles();
	}

	public List<Role> getRoles() {
		if (this.roles == null) {
			return Collections.emptyList();
		}
		return this.roles;
	}

	public int getIntimate(boolean exactly, Role... roles) {
		List<Role> list = Arrays.asList(roles);
		String key = this.getRolesKey(list, exactly);
		Integer value = result.get(key);
		return value == null ? 0 : value;
	}

	/**
	 * 将任务之间的关系频度记录到内部的map中，以供查询
	 * @param matches
	 */
	public void writeIntimates(List<Role> matches) {
		if (matches == null || matches.isEmpty()) {
			return;
		}
		int size = matches.size();
		ArrayList<Role> temp = new ArrayList<Role>(size);
		for (int i = 0; i < size; i++) {
			temp.add(matches.get(i));
			if (i == size - 1) {//有且仅有temp个，记录一次
				writeIntimatesInner(temp, true);
			}
			//temp个，以及更多，记录一次
			writeIntimatesInner(temp, false);
		}
	}

	private void writeIntimatesInner(ArrayList<Role> temp, boolean exactly) {
		String key = this.getRolesKey(temp, exactly);
		Integer value = this.result.get(key);
		if (value == null) {
			this.result.put(key, 1);
		}
		else {
			this.result.put(key, value + 1);
		}
	}

	/**
	 * 按照顺序排列任务名称，组成key
	 * @param list
	 * @param exactly
	 * @return
	 */
	private String getRolesKey(List<Role> list, boolean exactly) {
		this.sortRoles(list);
		int size = list.size();
		StringBuilder sb = new StringBuilder(size * 10);
		for (int i = 0; i < size; i++) {
			sb.append(list.get(i).getName()).append('_');
		}
		sb.append(String.valueOf(exactly));
		return sb.toString();
	}

	/**
	 * 按照名称排序角色列表
	 * @param role
	 */
	private void sortRoles(List<Role> role) {
		Collections.sort(role, new Comparator<Role>() {
			public int compare(Role arg0, Role arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
	}

}
