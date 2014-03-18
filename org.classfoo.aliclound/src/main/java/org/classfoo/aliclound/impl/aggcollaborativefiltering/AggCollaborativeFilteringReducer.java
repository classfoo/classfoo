package org.classfoo.aliclound.impl.aggcollaborativefiltering;

import java.util.ArrayList;
import java.util.List;

import org.classfoo.aliclound.Reducer;
import org.classfoo.aliclound.util.Pair;

/**
 * 协同算法的Reducer处理，根据获取的
 * <p>Copyright: Copyright (c) 2014<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2014-3-16
 */
public class AggCollaborativeFilteringReducer implements Reducer<String, String, String[]> {

	private List<Pair<String, List<String[]>>> previous = new ArrayList<Pair<String, List<String[]>>>(1024);

	public String[] reduce(Pair<String, List<String[]>> mapvalue) {
		ArrayList<String> result = new ArrayList<String>(10);
		for (Pair<String, List<String[]>> previou : previous) {
			this.reduce(mapvalue, previou, result);
		}
		previous.add(mapvalue);
		if (result.isEmpty()) {
			return null;
		}
		return result.toArray(new String[] {});
	}

	/**
	 * 对用户进行两两计算
	 * @param mapvalue
	 * @param previou
	 * @param result
	 */
	private void reduce(Pair<String, List<String[]>> mapvalue, Pair<String, List<String[]>> previou,
			ArrayList<String> result) {
		String user1 = mapvalue.getFirst();
		String user2 = previou.getFirst();
		double value = this.getMinkowski(mapvalue.getSecond(), previou.getSecond(), 1);
		if(value == -1){
			return;
		}
		result.add(user1 + ',' + user2 + ',' + value);
	}

	/**
	 * 获取两个用户之间权重评分
	 * @param list1
	 * @param list2
	 * @param r
	 * @return
	 */
	public double getMinkowski(List<String[]> list1, List<String[]> list2, int r) {
		int distance = 0;
		boolean commonRating = false;
		for (String[] item : list1) {
			String product = item[0];
			String visit1 = item[1];
			String visit2 = this.getVisit(product, list2);
			if (visit2 != null) {
				distance += Math.pow(Math.abs(Integer.valueOf(visit1) - Integer.valueOf(visit2)), r);
				commonRating = true;
			}
		}
		if (commonRating) {
			return Math.pow(distance, 1 / r);
		}
		else {
			return -1;
		}
	}

	/**
	 * 从list获取第一个元素匹配product的item，返回其第二个元素
	 * @param product
	 * @param list
	 * @return
	 */
	private String getVisit(String product, List<String[]> list) {
		for (String[] item : list) {
			if (product.equals(item[0])) {
				return item[1];
			}
		}
		return null;
	}
}
