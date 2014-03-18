package org.classfoo.aliclound.impl.aggcolumndistinct;

import org.classfoo.aliclound.Mapper;
import org.classfoo.aliclound.util.Pair;

/**
 * 聚集一列中distinct值的Mapper
 * <p>Copyright: Copyright (c) 2014<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2014-3-16
 */
public class AggColumnDistinctMapper implements Mapper<String, String, String> {

	private int columnindex;

	public AggColumnDistinctMapper(int columnindex) {
		this.columnindex = columnindex;
	}

	public Pair<String, String> map(String line) {
		if (line == null) {
			return null;
		}
		String[] splits = line.split(",");
		if (splits.length < columnindex + 1) {
			return null;
		}
		return new Pair<String, String>(splits[columnindex], null);
	}

}
