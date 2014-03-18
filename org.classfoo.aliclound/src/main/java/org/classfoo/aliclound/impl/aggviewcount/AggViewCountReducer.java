package org.classfoo.aliclound.impl.aggviewcount;

import java.util.List;

import org.classfoo.aliclound.Reducer;
import org.classfoo.aliclound.util.Pair;

/**
 * 针对查看次数的聚集Reducer
 * <p>Copyright: Copyright (c) 2014<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2014-3-16
 */
public class AggViewCountReducer implements Reducer<String, String, Integer> {

	public String[] reduce(Pair<String, List<Integer>> mapvalue) {
		return new String[] { mapvalue.getFirst() + ',' + mapvalue.getSecond().size() };
	}

}
