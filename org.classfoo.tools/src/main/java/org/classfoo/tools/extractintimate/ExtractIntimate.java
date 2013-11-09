package org.classfoo.tools.extractintimate;

import java.io.Reader;

/**
 * 抽取一段文字中给定人物之间的关联程度，通过人物出现在同一个句子的频率来判断关联程度大小
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-3
 */
public interface ExtractIntimate {

	public static final String THIRD_PERSON_MALE = "他";

	public static final String THIRD_PERSON_FEMALE = "她";

	/**
	 * 抽取人物关联程度
	 * @param reader
	 * @param properties
	 * @return
	 */
	public ExtractIntimateResult parse(Reader reader, ExtractIntimateProperties properties);
}
