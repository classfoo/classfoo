package org.classfoo.aliclound.util;

public class Pair<U extends Object, V extends Object> {

	private U u;

	private V v;

	public Pair(U u, V v) {
		this.u = u;
		this.v = v;
	}

	public U getFirst() {
		return this.u;
	}

	public void setFirst(U u) {
		this.u = u;
	}

	public V getSecond() {
		return this.v;
	}

	public void setSecond(V v) {
		this.v = v;
	}
}
