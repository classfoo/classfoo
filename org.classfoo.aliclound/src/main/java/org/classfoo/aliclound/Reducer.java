package org.classfoo.aliclound;

import java.util.List;

import org.classfoo.aliclound.util.Pair;

/**
 * Reducer接口
 * <p>Copyright: Copyright (c) 2014<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2014-3-16
 */
public interface Reducer<Line, Key, Value> {

	/**
	 * 执行Reducer，将List<Value>进行聚集
	 * @param mapvalue
	 * @return
	 */
	public Line[] reduce(Pair<Key, List<Value>> mapvalue);

}
