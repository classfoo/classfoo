package org.classfoo.aliclound;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

public interface Calculater<Line, Key, Value> {

	/**
	 * 设置输入流的编码
	 * @param encoding
	 */
	public void setInputStreamEncoding(String encoding);

	/**
	 * 设置输出流的编码
	 * @param encoding
	 */
	public void setOuputStreamEncoding(String encoding);

	/**
	 * 设置是否忽略第一行，默认不忽略
	 * @param ignore
	 */
	public void setIgnoreFirstLine(boolean ignore);

	/**
	 * 读取文件，执行计算，得到输出
	 * @param is
	 * @param os
	 * @throws IOException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 */
	public void calc(InputStream is, OutputStream os) throws IOException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException;

}
