package org.classfoo.tools.jpa.impl;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.classfoo.tools.jdbc.ColumnTypes;
import org.classfoo.tools.jpa.JPAField;
import org.classfoo.tools.jpa.JPATable;

/**
 * JPA注解类解析工具类
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-6-27
 */
public class JPAAnnotationUtil {

	//blob，clob字段的初始大小，主要用于db2
	public static final int BLOBSIZE = 1024 * 128;

	/**
	 * 解析JPA注解类列表，构造JPATable对象列表
	 * @param annotatedClasses
	 * @return
	 */
	public static ArrayList<JPATable> parseAnnotatedClasses(Class<?>... annotatedClasses) {
		int size = annotatedClasses.length;
		ArrayList<JPATable> tables = new ArrayList<JPATable>();
		for (int i = 0; i < size; i++) {
			Class<?> annotatedClass = annotatedClasses[i];
			JPATable jpaTable = parseAnnotatedClass(annotatedClass);
			if (jpaTable == null) {
				continue;
			}
			tables.add(jpaTable);
		}
		return tables;
	}

	/**
	 * 解析JPA注解类，获取JPATable实例
	 * @param annotatedClass
	 * @return
	 */
	private static JPATable parseAnnotatedClass(Class<?> annotatedClass) {
		boolean hasEntity = annotatedClass.isAnnotationPresent(Entity.class);
		if (!hasEntity) {
			return null;
		}
		String entityName = JPAUtil.getEntityName(annotatedClass);
		String tableName = JPAUtil.getTableName(annotatedClass);
		JPATableImpl jpaTable = new JPATableImpl(annotatedClass, entityName, tableName);
		boolean addPrimaryKey = true;//主键将是自身，或者第一个设置了主键的父类
		parseAnnotatedFields(jpaTable, annotatedClass, addPrimaryKey);
		return jpaTable;
	}

	/**
	 * 解析注解类中的属性列表，将之添加到JPATable中，将递归处理父类的属性
	 * @param jpaTable
	 * @param annotatedClass
	 * @param addPrimaryKey 控制是否要获取主键设置
	 */
	private static void parseAnnotatedFields(JPATableImpl jpaTable, Class<?> annotatedClass, boolean addPrimaryKey) {
		//判断注解，只有具有Entity和MappedSuperClass的类才分析其属性的column注解
		boolean hasEntity = annotatedClass.isAnnotationPresent(Entity.class);
		boolean hasMappedSuperclass = annotatedClass.isAnnotationPresent(MappedSuperclass.class);
		boolean containsPrimary = false;
		if (hasEntity || hasMappedSuperclass) {
			Field[] fields = annotatedClass.getDeclaredFields();
			if (fields != null && fields.length != 0) {
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					JPAField jpaField = parseAnnotatedField(field, addPrimaryKey);
					if (jpaField == null) {
						continue;
					}
					if (jpaField.isPrimary()) {
						containsPrimary = true;
					}
					jpaTable.addField(jpaField);
				}
			}
		}
		//递归处理父类的属性
		Class<?> superClass = annotatedClass.getSuperclass();
		if (superClass != null) {//第一次获取到了主键，父类中的@Id就当作普通字段
			parseAnnotatedFields(jpaTable, superClass, !containsPrimary);
		}
	}

	/**
	 * 解析注解类的属性，对其中的Column，Version，Id注解进行处理，提取成JPAField属性
	 * @param field
	 * @param addPrimaryKey 
	 * @return
	 */
	private static JPAField parseAnnotatedField(Field field, boolean addPrimaryKey) {
		boolean hasColumn = field.isAnnotationPresent(Column.class);
		if (!hasColumn) {
			return null;
		}
		String fieldName = field.getName();
		Column column = field.getAnnotation(Column.class);
		String columnName = column.name();
		if (columnName == null || columnName.length() == 0) {
			columnName = fieldName;
		}
		JPAFieldImpl jpaField = new JPAFieldImpl(fieldName, columnName);
		Class<?> fieldType = field.getType();
		jpaField.setFieldType(fieldType);
		int length = column.length();
		jpaField.setColumnLength(length);
		int precision = column.precision();
		jpaField.setColumnPrecision(precision);
		int scale = column.scale();
		jpaField.setColumnScale(scale);
		boolean unique = column.unique();
		jpaField.setColumnUnique(unique);
		boolean updatable = column.updatable();
		jpaField.setColumnUpdatable(updatable);
		boolean nullable = column.nullable();
		jpaField.setColumnNullable(nullable);
		boolean insertable = column.insertable();
		jpaField.setColumnInsertable(insertable);
		String define = column.columnDefinition();
		jpaField.setColumnDefinition(define);
		boolean isPrimary = field.isAnnotationPresent(Id.class);
		if (isPrimary) {//当addPrimay为true时，增加为主键字段，否则增加为unique字段
			if (addPrimaryKey) {
				jpaField.setPrimary(true);//如果addPrimaryKey为false，那么就不当作主键处理
			}
			else {
				jpaField.setColumnUnique(true);
			}
		}
		boolean isVersion = field.isAnnotationPresent(Version.class);
		jpaField.setVersion(isVersion);
		boolean isLob = field.isAnnotationPresent(Lob.class);
		jpaField.setLob(isLob);
		Basic basic = field.getAnnotation(Basic.class);
		if (basic != null) {
			FetchType fetch = basic.fetch();
			if (fetch == FetchType.LAZY) {
				jpaField.setLazyLoad(true);
			}
		}
		else {
			if (jpaField.isLob()) {
				jpaField.setLazyLoad(true);
			}
		}
		return parseColumnType(jpaField, field, isLob);
	}

	/**
	 * 为JPAField设置字段类型，并且可能需要根据字段类型去修改一些其它属性
	 * @param jpaField
	 * @param field
	 * @param isLob
	 * @return
	 */
	private static JPAField parseColumnType(JPAFieldImpl jpaField, Field field, boolean isLob) {
		Class<?> fieldType = field.getType();
		if (String.class.equals(fieldType)) {
			if (isLob) {
				jpaField.setColumnType(ColumnTypes.CLOB);
				//db2的blob字段必须设置大小
				jpaField.setColumnLength(BLOBSIZE);
				return jpaField;
			}
			else {
				jpaField.setColumnType(ColumnTypes.VARCHAR);
				return jpaField;
			}
		}
		if (byte[].class.equals(fieldType)) {
			jpaField.setColumnType(ColumnTypes.BLOB);
			//db2的blob字段必须设置大小
			jpaField.setColumnLength(BLOBSIZE);
			return jpaField;
		}
		if (Blob.class.equals(fieldType)) {
			jpaField.setColumnType(ColumnTypes.BLOB);
			//db2的blob字段必须设置大小
			jpaField.setColumnLength(BLOBSIZE);
			return jpaField;
		}
		if (Clob.class.equals(fieldType)) {
			jpaField.setColumnType(ColumnTypes.CLOB);
			//db2的blob字段必须设置大小
			jpaField.setColumnLength(BLOBSIZE);
			return jpaField;
		}
		if (isLob) {
			throw new RuntimeException("不允许类型为" + fieldType + "的属性加入@Lob注解！");
		}
		if (Integer.class.equals(fieldType) || int.class.equals(fieldType)) {
			jpaField.setColumnType(ColumnTypes.INTEGER);
			return jpaField;
		}
		if (Long.class.equals(fieldType) || long.class.equals(fieldType)) {
			jpaField.setColumnType(ColumnTypes.NUMERIC);
			jpaField.setColumnLength(20);
			jpaField.setColumnPrecision(0);
			jpaField.setColumnScale(0);
			return jpaField;
		}
		if (Byte.class.equals(fieldType) || byte.class.equals(fieldType)) {
			jpaField.setColumnType(ColumnTypes.NUMERIC);
			jpaField.setColumnLength(4);
			jpaField.setColumnPrecision(0);
			jpaField.setColumnScale(0);
			return jpaField;
		}
		if (Boolean.class.equals(fieldType) || boolean.class.equals(fieldType)) {
			jpaField.setColumnType(ColumnTypes.NUMERIC);
			jpaField.setColumnLength(1);
			jpaField.setColumnPrecision(0);
			jpaField.setColumnScale(0);
			return jpaField;
		}
		if (Double.class.equals(fieldType) || double.class.equals(fieldType)) {
			jpaField.setColumnType(ColumnTypes.NUMERIC);
			jpaField.setColumnLength(12);
			jpaField.setColumnPrecision(52);
			jpaField.setColumnScale(52);
			return jpaField;
		}
		if (Float.class.equals(fieldType) || float.class.equals(fieldType)) {
			jpaField.setColumnType(ColumnTypes.NUMERIC);
			jpaField.setColumnLength(8);
			jpaField.setColumnPrecision(24);
			jpaField.setColumnScale(24);
			return jpaField;
		}
		if (Timestamp.class.equals(fieldType)) {
			jpaField.setColumnType(ColumnTypes.TIMESTAMP);
			return jpaField;
		}
		jpaField.setColumnType(ColumnTypes.VARCHAR);
		return jpaField;
	}

}
