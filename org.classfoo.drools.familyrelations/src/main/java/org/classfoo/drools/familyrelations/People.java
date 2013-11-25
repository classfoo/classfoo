package org.classfoo.drools.familyrelations;

/**
 * 人物，包括名称，性别，年龄
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-1
 */
public class People {

	public boolean ismale;

	public String name;

	public int age;

	public People(String name, boolean ismale, int age) {
		this.name = name;
		this.ismale = ismale;
	}

	public boolean getIsmale() {
		return ismale;
	}

	public void setIsmale(boolean ismale) {
		this.ismale = ismale;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}
}
