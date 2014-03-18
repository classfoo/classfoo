package org.classfoo.aliclound.impl.aggcolumndistinct;

import java.util.List;

import org.classfoo.aliclound.Reducer;
import org.classfoo.aliclound.util.Pair;

/**
 * 聚集一列中distinct值的Reducer
 * <p>Copyright: Copyright (c) 2014<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2014-3-16
 */
public class AggColumnDistinctReducer implements Reducer<String, String, String> {

	public String[] reduce(Pair<String, List<String>> mapvalue) {
		return new String[] { mapvalue.getFirst() };
	}

}
