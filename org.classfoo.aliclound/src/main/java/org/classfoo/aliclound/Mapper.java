package org.classfoo.aliclound;

import org.classfoo.aliclound.util.Pair;

/**
 * Mapper接口，读取数据，处理数据为键值对，输出数据
 * <p>Copyright: Copyright (c) 2014<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2014-3-16
 */
public interface Mapper<Line, Key, Value> {

	/**
	 * 执行Mapper，将Line分解为键值对
	 * @param line
	 * @return
	 */
	public Pair<Key, Value> map(Line line);

}
