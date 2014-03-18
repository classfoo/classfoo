package org.classfoo.aliclound.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.classfoo.aliclound.Calculater;
import org.classfoo.aliclound.Mapper;
import org.classfoo.aliclound.Reducer;
import org.classfoo.aliclound.util.Pair;

public class CalculaterImpl<Line extends Object, Key extends Object, Value extends Object> implements Calculater<Line, Key, Value> {

	private Reducer<Line, Key, Value> reducer;

	private Mapper<Line, Key, Value> mapper;

	private String inencoding = "utf-8";

	private String outencoding = "utf-8";

	private Class<? extends Line> lineClass;

	private boolean ignorefirstline = false;

	public CalculaterImpl(Mapper<Line, Key, Value> mapper, Reducer<Line, Key, Value> reducer, Class<? extends Line> line) {
		this.mapper = mapper;
		this.reducer = reducer;
		this.lineClass = line;
	}

	public void setInputStreamEncoding(String encoding) {
		this.inencoding = encoding;
	}

	public void setOuputStreamEncoding(String encoding) {
		this.outencoding = encoding;
	}

	public void setIgnoreFirstLine(boolean ignore) {
		this.ignorefirstline = ignore;
	}

	public void calc(InputStream is, OutputStream os) throws IOException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		System.out.println("开始执行Mapper处理...");
		Map<Key, List<Value>> mapresults = this.doMap(is);
		System.out.println("开始执行Reducer处理...");
		this.doReduce(mapresults, os);
	}

	/**
	 * 执行Map处理
	 * @param is
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private Map<Key, List<Value>> doMap(InputStream is) throws UnsupportedEncodingException, IOException,
			NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		int count = 0;
		Map<Key, List<Value>> mapresults = new HashMap<Key, List<Value>>(1024);
		BufferedReader br = new BufferedReader(new InputStreamReader(is, inencoding));
		try {
			if (ignorefirstline) {
				br.readLine();
			}
			String line = br.readLine();
			while (line != null) {
				Constructor<? extends Line> con = lineClass.getConstructor(String.class);
				Line instance = con.newInstance(line);
				Pair<Key, Value> pair = this.tryDoMap(instance);
				if (pair == null) {
					line = br.readLine();
					continue;
				}
				count++;
				List<Value> list = mapresults.get(pair.getFirst());
				if (list == null) {
					list = new ArrayList<Value>(64);
					mapresults.put(pair.getFirst(), list);
				}
				list.add(pair.getSecond());
				line = br.readLine();
			}
			System.out.println("Mapper处理了" + count + "行数据...");
		}
		finally {
			br.close();
		}
		return mapresults;
	}

	private Pair<Key, Value> tryDoMap(Line instance) {
		try {
			return this.mapper.map(instance);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 执行Reduce处理
	 * @param mapresults
	 * @param os
	 * @throws IOException 
	 */
	private void doReduce(Map<Key, List<Value>> mapresults, OutputStream os) throws IOException {
		int count = 0;
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, this.outencoding));
		try {
			for (Key key : mapresults.keySet()) {
				List<Value> list = mapresults.get(key);
				Pair<Key, List<Value>> pair = new Pair<Key, List<Value>>(key, list);
				Line[] result = this.tryDoReduce(pair);
				if (result == null) {
					continue;
				}
				for (Line line : result) {
					writer.println(line.toString());
					count++;
				}
			}
			System.out.println("Reducer输出了" + count + "行数据...");
		}
		finally {
			writer.close();
		}
	}

	private Line[] tryDoReduce(Pair<Key, List<Value>> pair) {
		try {
			return this.reducer.reduce(pair);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
