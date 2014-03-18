package org.classfoo.aliclound;

/**
 * 计算服务接口
 * <p>Copyright: Copyright (c) 2014<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2014-3-16
 */
public interface CalculaterService {

	public <Line extends Object, Key extends Object, Value extends Object> Calculater<Line, Key, Value> createCaculater(
			Mapper<Line, Key, Value> mapper, Reducer<Line, Key, Value> reducer, Class<? extends Line> line,
			Class<? extends Key> key, Class<? extends Value> value) throws InstantiationException,
			IllegalAccessException;
}
