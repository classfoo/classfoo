package org.classfoo.aliclound.impl.aggcollaborativefiltering;

import org.classfoo.aliclound.Mapper;
import org.classfoo.aliclound.util.Pair;

/**
 * 协同分析的Mapper处理，将数据”用户,产品,访问次数“转换为："用户,(产品,次数)"，交付给Reducer
 * 
 * <p>Copyright: Copyright (c) 2014<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2014-3-16
 */
public class AggCollaborativeFilteringMapper implements Mapper<String, String, String[]> {

	public Pair<String, String[]> map(String line) {
		if (line == null) {
			return null;
		}
		String[] splits = line.split(",");
		return new Pair<String, String[]>(splits[0], new String[] { splits[1], splits[2] });
	}
}
