package org.classfoo.tools.jdbc;

/**
 * 定义字段类型
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author dengw
 * @createdate 2012-3-1
 */
public interface ColumnTypes {

	public static final char INTEGER = 'I';

	public static final char NUMERIC = 'N';

	public static final char VARCHAR = 'C';

	public static final char CLOB = 'M';

	public static final char BLOB = 'X';

	public static final char DATE = 'D';

	public static final char TIME = 'T';

	public static final char TIMESTAMP = 'P';

	public static final char NULL = 'V';

	public static final char OTHER = 'U';
}
