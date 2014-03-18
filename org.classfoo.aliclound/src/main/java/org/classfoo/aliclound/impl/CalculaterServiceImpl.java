package org.classfoo.aliclound.impl;

import org.classfoo.aliclound.Calculater;
import org.classfoo.aliclound.CalculaterService;
import org.classfoo.aliclound.Mapper;
import org.classfoo.aliclound.Reducer;

/**
 * @see CalculaterService
 * <p>Copyright: Copyright (c) 2014<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2014-3-16
 */
public class CalculaterServiceImpl implements CalculaterService {

	public <Line extends Object, Key extends Object, Value extends Object> Calculater<Line, Key, Value> createCaculater(
			Mapper<Line, Key, Value> mapper, Reducer<Line, Key, Value> reducer, Class<? extends Line> line,
			Class<? extends Key> key, Class<? extends Value> value) throws InstantiationException,
			IllegalAccessException {
		return new CalculaterImpl<Line, Key, Value>(mapper, reducer, line);
	}

}
