package org.classfoo.tools.extractintimate.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.classfoo.tools.extractintimate.ExtractIntimate;
import org.classfoo.tools.extractintimate.ExtractIntimateProperties;
import org.classfoo.tools.extractintimate.ExtractIntimateResult;
import org.classfoo.tools.extractintimate.Role;
import org.springframework.stereotype.Component;

/**
 * @see ExtractIntimate
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-3
 */
@Component
public class ExtractIntimateImpl implements ExtractIntimate {

	public ExtractIntimateResult parse(Reader reader, ExtractIntimateProperties properties) {
		ExtractIntimateResultImpl result = new ExtractIntimateResultImpl(properties);
		try {
			return parse(reader, properties, result);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 读取流，按照properties的设置分析小说，得到结果
	 * @param reader
	 * @param properties
	 * @param result
	 * @return
	 * @throws IOException
	 */
	private ExtractIntimateResult parse(Reader reader, ExtractIntimateProperties properties,
			ExtractIntimateResultImpl result) throws IOException {
		List<Role> roles = properties.getRoles();
		char thirdmale = properties.getThirdPersonFemale();
		char thirdfemale = properties.getThirdPersonFemale();
		char[] ends = properties.getSetenceEnds();
		char[] c = new char[1];
		ExtractIntimateStatus status = new ExtractIntimateStatus(roles);
		while (reader.read(c) != -1) {
			this.parseCharactor(c[0], roles, ends, thirdmale, thirdfemale, result, status);
		}
		return result;
	}

	/**
	 * 对小说中单个文字的状态机分析
	 * @param c
	 * @param roles
	 * @param ends
	 * @param thirdmale
	 * @param thirdfemale
	 * @param result
	 * @param status
	 */
	private void parseCharactor(char c, List<Role> roles, char ends[], char thirdmale, char thirdfemale,
			ExtractIntimateResultImpl result, ExtractIntimateStatus status) {
		if (c == thirdfemale) {
			status.meetThirdMale(c);
		}
		else if (c == thirdfemale) {
			status.meetThirdFemale(c);
		}
		else {
			if (isEnd(c, ends)) {
				status.endSentence(result);
			}
			else {
				status.addCharactor(c);
			}
		}
	}

	/**
	 * 判断字符是不是一个句子结束字符
	 * @param c
	 * @param ends
	 * @return
	 */
	private boolean isEnd(char c, char[] ends) {
		for (int i = 0; i < ends.length; i++) {
			char end = ends[i];
			if (c == end) {
				return true;
			}
		}
		return false;
	}

	class ExtractIntimateStatus {

		private List<Role> roles = null;

		private List<Role> matches = null;

		private Role latestFemale = null;

		private Role latestMale = null;

		private int[][] marks = null;

		public ExtractIntimateStatus(List<Role> roles) {
			this.roles = roles;
			int rolesize = this.roles.size();
			this.marks = new int[rolesize][];
			this.matches = new ArrayList<Role>(5);
		}

		/**
		 * 遍历到一个非特殊字符
		 * @param c
		 */
		public void addCharactor(char c) {
			Role role = this.meetRole(c);
			if (role == null) {
				return;
			}
			if (role.getType() == Role.TYPE_MALE) {
				this.latestMale = role;
			}
			else if (role.getType() == Role.TYPE_FEMALE) {
				this.latestFemale = role;
			}
			if (!this.matches.contains(role)) {
				this.matches.add(role);
			}
		}

		/**
		 * 判断到达当前字符后，是否匹配到了某个角色，将把匹配到的角色返回
		 * @param c
		 * @return
		 */
		private Role meetRole(char c) {
			int size = this.marks.length;
			for (int i = 0; i < size; i++) {
				Role role = this.roles.get(i);
				String name = role.getName();
				List<String> aliasNames = role.getAliasNames();
				int count = aliasNames.size() + 1;
				initMarks(i, count);
				if (updateMarks(name, i, 0, c)) {
					return role;
				}
				for (int j = 0; j < aliasNames.size(); j++) {
					String aliasName = aliasNames.get(j);
					if (updateMarks(aliasName, i, j + 1, c)) {
						return role;
					}
				}
			}
			return null;
		}

		/**
		 * 判断是否要初始化marks的第I位，如果需要，将其填充一个长度为count的，值为0的数组
		 * @param i
		 * @param count
		 */
		private void initMarks(int i, int count) {
			if (this.marks[i] != null) {
				return;
			}
			this.marks[i] = new int[count];
			for (int j = 0; j < count; j++) {
				this.marks[i][j] = 0;
			}
		}

		/**
		 * 更新marks数组，并返回是否匹配一个角色
		 * @param name
		 * @param i
		 * @param j
		 * @param c
		 * @return
		 */
		private boolean updateMarks(String name, int i, int j, char c) {
			int namelen = name.length();
			if (namelen > this.marks[i][j] && name.charAt(this.marks[i][j]) == c) {
				this.marks[i][j]++;
				if (namelen == this.marks[i][j]) {
					return true;
				}
				return false;
			}
			else {
				this.marks[i][j] = 0;
				return false;
			}
		}

		/**
		 * 将当前句子匹配到角色写入到result中
		 * @param result
		 */
		public void endSentence(ExtractIntimateResultImpl result) {
			if (this.matches == null || this.matches.isEmpty()) {
				for (int i = 0; i < this.marks.length; i++) {
					this.marks[i] = null;
				}
				return;
			}
			result.writeIntimates(this.matches);
			for (int i = this.matches.size() - 1; i >= 0; i--) {
				this.matches.remove(i);
			}
			for (int i = 0; i < this.marks.length; i++) {
				this.marks[i] = null;
			}
		}

		/**
		 * 判断是否出现了‘他’类型的字符，如果遇到，那么将最近一个male纳入匹配列表
		 * @param c
		 */
		public void meetThirdMale(char c) {
			if (this.latestMale == null || matches.contains(this.latestMale)) {
				return;
			}
			matches.add(this.latestMale);
		}

		/**
		 * 判断是否出现了‘她’类型的字符，如果遇到，那么将最近一个female纳入匹配列表
		 * @param c
		 */
		public void meetThirdFemale(char c) {
			if (this.latestFemale == null || matches.contains(this.latestFemale)) {
				return;
			}
			matches.add(this.latestFemale);
		}

	}
}
