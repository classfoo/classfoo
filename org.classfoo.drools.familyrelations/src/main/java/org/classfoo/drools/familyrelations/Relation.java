package org.classfoo.drools.familyrelations;

/**
 * 关系，A是B的Relation，例如贾宝玉是林黛玉的表哥
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-1
 */
public class Relation {

	public People a;

	public People b;

	public String relation;

	public Relation(String relation, People a, People b) {
		this.relation = relation;
		this.a = a;
		this.b = b;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public People getA() {
		return a;
	}

	public void setA(People a) {
		this.a = a;
	}

	public People getB() {
		return b;
	}

	public void setB(People b) {
		this.b = b;
	}
}
