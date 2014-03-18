package org.classfoo.aliclound.impl.aggviewcount;

import org.classfoo.aliclound.Mapper;
import org.classfoo.aliclound.util.Pair;

/**
 * 针对查看次数的聚集Mapper
 * <p>Copyright: Copyright (c) 2014<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2014-3-16
 */
public class AggViewCountMapper implements Mapper<String, String, Integer> {

	public Pair<String, Integer> map(String line) {
		if (line == null || line.length() == 0) {
			return null;
		}
		int index = line.indexOf(",");
		if (index == -1) {
			return null;
		}
		index = line.indexOf(",", index + 1);
		if (index == -1) {
			return null;
		}
		int nextindex = line.indexOf(",", index + 1);
		if (nextindex == -1) {
			return null;
		}
		String key = line.substring(0, index);
		String value = line.substring(index + 1, nextindex);
		if (value.charAt(0) != '0') {
			return null;
		}
		return new Pair<String, Integer>(key, 0);
	}

}
