package org.classfoo.tools.jpa.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.classfoo.tools.jdbc.TableColumnMetaData;
import org.classfoo.tools.jdbc.TableMetaData;
import org.classfoo.tools.jpa.JPAField;
import org.classfoo.tools.jpa.JPATable;

/**
 * 每一个加入了@Entity注解的类在经过JPASessionFactory注册后，都将得到一个JPATable实例。这个实
 * 例中封装了注解类的各种详细信息，例如entity名，表名，字段列表，主键字段，延迟加载字段，乐观锁
 * 字段等等
 * <p>Copyright: Copyright (c) 2012<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2012-7-9
 */
public class JPATableImpl implements JPATable, Serializable {

	private static final long serialVersionUID = -5684229425358466093L;

	private String entityName;

	private String tableName;

	private Map<String, JPAField> fields;

	private Map<String, String> columnnames;

	private List<String> primaryFieldNames;

	private List<String> lobFieldNames;

	private List<String> lazyLoadFieldNames;

	private String versionFieldName;

	private Class<?> type;

	public JPATableImpl(Class<?> type, String entityName, String tableName) {
		this.type = type;
		this.entityName = entityName;
		//数据库表名统一使用大写定义
		this.tableName = tableName == null ? null : tableName.toUpperCase();
	}

	public Class<?> getJPAType() {
		return this.type;
	}

	public String getEntityName() {
		return this.entityName;
	}

	public String getTableName() {
		return this.tableName;
	}

	public List<String> getFieldNames() {
		if (this.fields == null || this.fields.isEmpty()) {
			return Collections.emptyList();
		}
		int size = this.fields.size();
		ArrayList<String> list = new ArrayList<String>(size);
		list.addAll(this.fields.keySet());
		return list;
	}

	public List<String> getPrimaryFieldNames() {
		return this.primaryFieldNames;
	}

	public List<String> getLobFieldNames() {
		return this.lobFieldNames;
	}

	public List<String> getLazyLoadFieldNames() {
		return this.lazyLoadFieldNames;
	}

	public String getVersionFieldName() {
		return this.versionFieldName;
	}

	public JPAField getField(String name) {
		if (this.fields == null || this.fields.isEmpty()) {
			return null;
		}
		return this.fields.get(name);
	}

	public JPAField getFieldByColumnName(String name) {
		if (this.columnnames == null || this.columnnames.isEmpty()) {
			return null;
		}
		//XXX 需要将columnName转换为大写字母，因为内部map存储columnName时统一使用大写
		String fieldname = this.columnnames.get(name.toUpperCase());
		return this.getField(fieldname);
	}

	/**
	 * 向JPATable内部添加JPAField，添加过程中判断feild是否为组件，是否是version字段等等
	 * @param jpaField
	 */
	public void addField(JPAField jpaField) {
		//添加字段map
		String fieldName = jpaField.getName();
		if (this.fields == null) {
			this.fields = new HashMap<String, JPAField>();
			this.fields.put(fieldName, jpaField);
		}
		else {
			if (this.fields.containsKey(fieldName)) {
				return;
				//throw new RuntimeException("不能向"+this+"重复添加属性:" + fieldName);
			}
			this.fields.put(fieldName, jpaField);
		}
		String columnName = jpaField.getColumnName();
		if (columnName == null) {
			throw new RuntimeException("必须有字段名！");
		}
		if (this.columnnames == null) {
			this.columnnames = new HashMap<String, String>();
			this.columnnames.put(columnName, fieldName);
		}
		else {
			if (this.columnnames.containsKey(columnName)) {
				throw new RuntimeException("不能向" + this + "重复添加字段:" + columnName);
			}
			this.columnnames.put(columnName, fieldName);
		}
		//添加主键字段名称列表
		if (jpaField.isPrimary()) {
			if (this.primaryFieldNames == null) {
				this.primaryFieldNames = new ArrayList<String>();
			}
			this.primaryFieldNames.add(fieldName);
		}
		//添加lob字段名称列表
		if (jpaField.isLob()) {
			if (this.lobFieldNames == null) {
				this.lobFieldNames = new ArrayList<String>();
			}
			this.lobFieldNames.add(fieldName);
		}
		if (jpaField.isLazyLoad()) {
			if (this.lazyLoadFieldNames == null) {
				this.lazyLoadFieldNames = new ArrayList<String>();
			}
			this.lazyLoadFieldNames.add(fieldName);
		}
		//添加version字段列表
		if (jpaField.isVersion()) {
			if (this.versionFieldName != null) {
				throw new RuntimeException("JPA注解异常：不能重复设置Version注解！");
			}
			this.versionFieldName = fieldName;
		}
	}

	public String toString() {
		return this.getEntityName();
	}

	/**
	 * 将未在注解类中定义，但在数据库表中有的字段进行扫描，将之补充入元模型中
	 * @param tableMeta
	 */
	public void addExtraFields(TableMetaData tableMeta) {
		int count = tableMeta.getColumnCount();
		for (int i = 0; i < count; i++) {
			TableColumnMetaData column = tableMeta.getColumn(i);
			String name = column.getColumnName();
			String upperCase = name.toUpperCase();
			if (columnnames.containsKey(upperCase)) {
				//name转换为大写作为字段名
				continue;
			}
			if(this.fields.containsKey(upperCase)){
				//name不区分大小写作为field名称
				continue;
			}
			JPAFieldImpl field = new JPAFieldImpl(upperCase, upperCase);
			field.setColumnType(column.getType());
			field.setColumnLength(column.getLength());
			field.setColumnNullable(column.isNullable());
			field.setColumnUnique(column.isUnique());
			field.setLazyLoad(true);
			field.setFieldType(JPADataBaseUtil.getFieldTypeByColumnType(column.getType()));
			field.setColumnPrecision(column.getScale());
			this.addField(field);
		}
	}
}
