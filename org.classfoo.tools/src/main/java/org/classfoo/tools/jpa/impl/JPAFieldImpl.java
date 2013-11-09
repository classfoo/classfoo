package org.classfoo.tools.jpa.impl;

import java.io.Serializable;

import org.classfoo.tools.jpa.JPAField;

/**
 * 加入了@Entity注解的类中属性如果加入了@Column时，将会被解析成一个JPAField实例，里面封装了
 * 数据库表字段，类属性类型等信息
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-7-9
 */
public class JPAFieldImpl implements JPAField , Serializable{

	private static final long serialVersionUID = -2347827861975510489L;

	private String name;

	private Class<?> fieldType;

	private String columnName;

	private int columnLength;

	private char columnType;

	private boolean columnUnique;

	private boolean columnNullable;

	private int columnPrecision;

	private int columnScale;

	private String columnDefinition;

	private boolean columnUpdatable;

	private boolean columnInsertable;

	private boolean primary;

	private boolean lob;

	private boolean version;
	
	private boolean lazyload;

	/**
	 * 初始化JPAField实例，传入属性名和字段名
	 * @param fieldName
	 * @param columnName
	 */
	public JPAFieldImpl(String fieldName, String columnName) {
		this.name = fieldName;
		//字段名恒定转换为大写，以便不同系统统一
		this.columnName = columnName.toUpperCase();
	}

	public String getName() {
		return this.name;
	}

	public Class<?> getFieldType() {
		return this.fieldType;
	}

	public String getColumnName() {
		return this.columnName;
	}

	public int getColumnLength() {
		return this.columnLength;
	}

	public char getColumnType() {
		return this.columnType;
	}

	public int getColumnPrecision() {
		return this.columnPrecision;
	}

	public int getColumnScale() {
		return this.columnScale;
	}

	public String getColumnDefinition() {
		return this.columnDefinition;
	}

	public boolean isColumnInsertable() {
		return columnInsertable;
	}

	public boolean isColumnUpdatable() {
		return columnUpdatable;
	}

	public boolean isColumnUnique() {
		return this.columnUnique;
	}

	public boolean isColumnNullable() {
		return columnNullable;
	}

	public boolean isLob() {
		return lob;
	}

	public boolean isPrimary() {
		return primary;
	}

	public boolean isVersion() {
		return version;
	}
	
	public boolean isLazyLoad() {
		return this.lazyload;
	}

	protected void setFieldType(Class<?> fieldType) {
		this.fieldType = fieldType;
	}

	protected void setColumnType(char columnType) {
		this.columnType = columnType;
	}

	protected void setColumnLength(int length) {
		this.columnLength = length;
	}

	protected void setColumnPrecision(int precision) {
		this.columnPrecision = precision;
	}

	protected void setColumnScale(int scale) {
		this.columnScale = scale;
	}

	protected void setColumnUnique(boolean unique) {
		this.columnUnique = unique;
	}

	protected void setColumnUpdatable(boolean updatable) {
		this.columnUpdatable = updatable;
	}

	protected void setColumnNullable(boolean nullable) {
		this.columnNullable = nullable;
	}

	protected void setColumnInsertable(boolean insertable) {
		this.columnInsertable = insertable;
	}

	protected void setColumnDefinition(String define) {
		this.columnDefinition = define;
	}

	protected void setPrimary(boolean primary) {
		this.primary = primary;
	}

	protected void setVersion(boolean version) {
		if(version && this.isPrimary()){
			throw new RuntimeException("不允许设置主键字段" + this.getName() + "为Version字段！");
		}
		this.version = version;
	}

	protected void setLob(boolean lob) {
		if(lob && this.isPrimary()){
			throw new RuntimeException("不允许设置主键字段" + this.getName() + "为Lob字段！");
		}
		this.lob = lob;
	}

	protected void setLazyLoad(boolean lazyload){
		if(lazyload&&this.isPrimary()){
			throw new RuntimeException("不允许设置主键字段" + this.getName() + "为延迟加载字段！");
		}
		this.lazyload = lazyload;
	}
}
