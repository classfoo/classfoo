package org.classfoo.aliclound;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.classfoo.aliclound.impl.CalculaterServiceImpl;
import org.classfoo.aliclound.impl.aggcollaborativefiltering.AggCollaborativeFilteringMapper;
import org.classfoo.aliclound.impl.aggcollaborativefiltering.AggCollaborativeFilteringReducer;
import org.classfoo.aliclound.impl.aggcolumndistinct.AggColumnDistinctMapper;
import org.classfoo.aliclound.impl.aggcolumndistinct.AggColumnDistinctReducer;
import org.classfoo.aliclound.impl.aggviewcount.AggViewCountMapper;
import org.classfoo.aliclound.impl.aggviewcount.AggViewCountReducer;

public class Main {

	public static final void main(String[] args) throws InstantiationException, IllegalAccessException, IOException,
			SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
		//calcAggViewCount();
		//calcAggDistinctUser();
		//calcAggDistinctProduct();
		calcAggCollaborativeFiltering();
	}

	/**
	 * 计算获取用户列表
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	private static void calcAggDistinctUser() throws InstantiationException, IllegalAccessException, SecurityException,
			IllegalArgumentException, IOException, NoSuchMethodException, InvocationTargetException {
		CalculaterServiceImpl service = new CalculaterServiceImpl();
		AggColumnDistinctMapper mapper = new AggColumnDistinctMapper(0);
		AggColumnDistinctReducer reducer = new AggColumnDistinctReducer();
		Calculater<String, String, String> calc = service.createCaculater(mapper, reducer, String.class, String.class,
				String.class);
		InputStream in = Main.class.getResourceAsStream("t_alibaba_data.csv");
		try {
			FileOutputStream out = new FileOutputStream("aggdistinctuser.csv");
			try {
				calc.setInputStreamEncoding("GBK");
				calc.setOuputStreamEncoding("UTF-8");
				calc.setIgnoreFirstLine(true);
				calc.calc(in, out);
			}
			finally {
				out.close();
			}
		}
		finally {
			in.close();
		}
	}

	/**
	 * 计算获取用户列表
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	private static void calcAggDistinctProduct() throws InstantiationException, IllegalAccessException,
			SecurityException, IllegalArgumentException, IOException, NoSuchMethodException, InvocationTargetException {
		CalculaterServiceImpl service = new CalculaterServiceImpl();
		AggColumnDistinctMapper mapper = new AggColumnDistinctMapper(1);
		AggColumnDistinctReducer reducer = new AggColumnDistinctReducer();
		Calculater<String, String, String> calc = service.createCaculater(mapper, reducer, String.class, String.class,
				String.class);
		InputStream in = Main.class.getResourceAsStream("t_alibaba_data.csv");
		try {
			FileOutputStream out = new FileOutputStream("aggdistinctproduct.csv");
			try {
				calc.setInputStreamEncoding("GBK");
				calc.setOuputStreamEncoding("UTF-8");
				calc.setIgnoreFirstLine(true);
				calc.calc(in, out);
			}
			finally {
				out.close();
			}
		}
		finally {
			in.close();
		}

	}

	/**
	 * 计算用户对产品的查看次数数据
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	private static void calcAggViewCount() throws InstantiationException, IllegalAccessException,
			FileNotFoundException, IOException, NoSuchMethodException, InvocationTargetException {
		CalculaterServiceImpl service = new CalculaterServiceImpl();
		AggViewCountMapper mapper = new AggViewCountMapper();
		AggViewCountReducer reducer = new AggViewCountReducer();
		Calculater<String, String, Integer> calc = service.createCaculater(mapper, reducer, String.class, String.class,
				Integer.class);
		InputStream in = Main.class.getResourceAsStream("t_alibaba_data.csv");
		try {
			FileOutputStream out = new FileOutputStream("aggviewcount.csv");
			try {
				calc.setInputStreamEncoding("GBK");
				calc.setOuputStreamEncoding("UTF-8");
				calc.setIgnoreFirstLine(true);
				calc.calc(in, out);
			}
			finally {
				out.close();
			}
		}
		finally {
			in.close();
		}
	}

	private static void calcAggCollaborativeFiltering() throws InstantiationException, IllegalAccessException,
			FileNotFoundException, IOException, NoSuchMethodException, InvocationTargetException {
		CalculaterServiceImpl service = new CalculaterServiceImpl();
		AggCollaborativeFilteringMapper mapper = new AggCollaborativeFilteringMapper();
		AggCollaborativeFilteringReducer reducer = new AggCollaborativeFilteringReducer();
		Calculater<String, String, String[]> calc = service.createCaculater(mapper, reducer, String.class,
				String.class, String[].class);
		InputStream in = Main.class.getResourceAsStream("aggviewcount.csv");
		try {
			FileOutputStream out = new FileOutputStream("aggcollaborativefiltering.csv");
			try {
				calc.setInputStreamEncoding("UTF-8");
				calc.setOuputStreamEncoding("UTF-8");
				calc.setIgnoreFirstLine(true);
				calc.calc(in, out);
			}
			finally {
				out.close();
			}
		}
		finally {
			in.close();
		}
	}
}
