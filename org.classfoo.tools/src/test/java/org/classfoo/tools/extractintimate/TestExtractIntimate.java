package org.classfoo.tools.extractintimate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.classfoo.tools.extractintimate.impl.ExtractIntimatePropertiesImpl;
import org.classfoo.tools.extractintimate.impl.RoleImpl;
import org.classfoo.tools.jdbc.ConnectionManager;
import org.classfoo.tools.spring.SpringContext;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

public class TestExtractIntimate {

	@Test
	public void testExtractIntimate() throws IOException, SQLException {
		//初始化Spring
		ApplicationContext context = SpringContext.initSpringContext();
		ExtractIntimate intimate = context.getBean(ExtractIntimate.class);
		//配置基本的设置
		ExtractIntimatePropertiesImpl properties = new ExtractIntimatePropertiesImpl();
		properties.setThirdPersonFemale('她');
		properties.setThirdPersonMale('他');
		properties.setSetenceEnds(new char[] { '。', '？', '；' });
		ArrayList<Role> roles = new ArrayList<Role>(20);
		//roles.add(new RoleImpl("我", null, Role.TYPE_OBJECT));
		roles.add(new RoleImpl("直子", null, Role.TYPE_FEMALE));
		roles.add(new RoleImpl("渡边", null, Role.TYPE_MALE));
		roles.add(new RoleImpl("木月", null, Role.TYPE_MALE));
		roles.add(new RoleImpl("突击队", null, Role.TYPE_MALE));
		roles.add(new RoleImpl("玲子", null, Role.TYPE_FEMALE));
		roles.add(new RoleImpl("绿子", Arrays.asList(new String[] { "小林绿", "阿绿" }), Role.TYPE_FEMALE));
		roles.add(new RoleImpl("永泽", null, Role.TYPE_MALE));
		roles.add(new RoleImpl("初美", null, Role.TYPE_FEMALE));
		roles.add(new RoleImpl("书", null, Role.TYPE_OBJECT));
		roles.add(new RoleImpl("酒", Arrays.asList(new String[] { "威士忌", "啤酒", "黑啤", "鸡尾酒" }), Role.TYPE_OBJECT));
		roles.add(new RoleImpl("吉他", Arrays.asList(new String[] { "吉它" }), Role.TYPE_OBJECT));
		roles.add(new RoleImpl("唱片", null, Role.TYPE_OBJECT));
		roles.add(new RoleImpl("朋友", Arrays.asList(new String[] { "好友" }), Role.TYPE_OBJECT));
		roles.add(new RoleImpl("爸爸", Arrays.asList(new String[] { "父亲" }), Role.TYPE_MALE));
		roles.add(new RoleImpl("妈妈", Arrays.asList(new String[] { "母亲" }), Role.TYPE_FEMALE));
		roles.add(new RoleImpl("姐姐", Arrays.asList(new String[] { "姐" }), Role.TYPE_FEMALE));
		roles.add(new RoleImpl("妹妹", Arrays.asList(new String[] { "妹" }), Role.TYPE_FEMALE));
		roles.add(new RoleImpl("哥哥", Arrays.asList(new String[] { "哥" }), Role.TYPE_MALE));
		roles.add(new RoleImpl("弟弟", Arrays.asList(new String[] { "弟" }), Role.TYPE_MALE));

		properties.setRoles(roles);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				TestExtractIntimate.class.getResourceAsStream("挪威的森林.txt"), "GB2312"));
		try {
			ExtractIntimateResult result = intimate.parse(reader, properties);
			printForFun(roles, result);
			outputToDb(context, result);
		}
		finally {
			reader.close();
		}
	}

	/**
	 * 将数据输出到数据库表中
	 * @param context
	 * @param result
	 * @throws SQLException 
	 */
	private void outputToDb(ApplicationContext context, ExtractIntimateResult result) throws SQLException {
		ConnectionManager connmgr = context.getBean(ConnectionManager.class);
		outputToDbOneDim(result, connmgr);
		outputToDbTwoDim(result, connmgr);
		outputToDbThreeDim(result, connmgr);
	}

	private void outputToDbOneDim(ExtractIntimateResult result, ConnectionManager connmgr) throws SQLException {
		Connection conn = connmgr.getConnection();
		try {
			conn.setAutoCommit(false);
			List<Role> roles = result.getRoles();
			for (int i = 0; i < roles.size(); i++) {
				Role role = roles.get(i);
				int count = result.getIntimate(false, role);
				PreparedStatement state1 = conn.prepareStatement("replace into NOVEL_ONEDIM (ROLE_,COUNT_,EXACTLY_) values(?,?,?)");
				state1.setString(1, role.getName());
				state1.setInt(2, count);
				state1.setInt(3, 1);
				state1.executeUpdate();
				count = result.getIntimate(true, role);
				state1.setString(1, role.getName());
				state1.setInt(2, count);
				state1.setInt(3, 0);
				state1.executeUpdate();
			}
			conn.commit();
		}
		finally {
			conn.close();
		}
	}

	private void outputToDbTwoDim(ExtractIntimateResult result, ConnectionManager connmgr) throws SQLException {
		Connection conn = connmgr.getConnection();
		try {
			conn.setAutoCommit(false);
			List<Role> roles = result.getRoles();
			for (int i = 0; i < roles.size(); i++) {
				for (int j = 0; j < roles.size(); j++) {
					if(i == j){
						continue;
					}
					Role role1 = roles.get(i);
					Role role2 = roles.get(j);
					int count = result.getIntimate(false, role1, role2);
					PreparedStatement state1 = conn.prepareStatement("replace into NOVEL_TWODIM (ROLE1_,ROLE2_,COUNT_,EXACTLY_) values(?,?,?,?)");
					state1.setString(1, role1.getName());
					state1.setString(2, role2.getName());
					state1.setInt(3, count);
					state1.setInt(4, 1);
					state1.executeUpdate();
					count = result.getIntimate(true, role1, role2);
					state1.setString(1, role1.getName());
					state1.setString(2, role2.getName());
					state1.setInt(3, count);
					state1.setInt(4, 0);
					state1.executeUpdate();
				}
			}
			conn.commit();
		}
		finally {
			conn.close();
		}
	}

	private void outputToDbThreeDim(ExtractIntimateResult result, ConnectionManager connmgr) throws SQLException {
		Connection conn = connmgr.getConnection();
		try {
			conn.setAutoCommit(false);
			List<Role> roles = result.getRoles();
			for (int i = 0; i < roles.size(); i++) {
				for (int j = 0; j < roles.size(); j++) {
					for (int k = 0; k < roles.size(); k++) {
						if (i == j || j == k || k == i || (i == j && j == k)) {
							continue;
						}
						Role role1 = roles.get(i);
						Role role2 = roles.get(j);
						Role role3 = roles.get(k);
						int count = result.getIntimate(false, role1, role2, role3);
						PreparedStatement state1 = conn.prepareStatement("replace into NOVEL_THREEDIM (ROLE1_,ROLE2_,ROLE3_,COUNT_,EXACTLY_) values(?,?,?,?,?)");
						state1.setString(1, role1.getName());
						state1.setString(2, role2.getName());
						state1.setString(3, role3.getName());
						state1.setInt(4, count);
						state1.setInt(5, 1);
						state1.executeUpdate();
						count = result.getIntimate(true, role1, role2, role3);
						state1.setString(1, role1.getName());
						state1.setString(2, role2.getName());
						state1.setString(3, role3.getName());
						state1.setInt(4, count);
						state1.setInt(5, 0);
						state1.executeUpdate();
					}
				}
			}
			conn.commit();
		}
		finally {
			conn.close();
		}
	}

	/**
	 * 输出到控制台，just for fun
	 * @param roles
	 * @param result
	 */
	private void printForFun(ArrayList<Role> roles, ExtractIntimateResult result) {
		//输出一维关系
		System.out.println("一维关系分析（复合）：");
		for (int i = 0; i < roles.size(); i++) {
			Role role = roles.get(i);
			int count = result.getIntimate(false, role);
			System.out.println(role.getName() + "出现的频次为：" + count);
		}
		System.out.println("一维关系分析（独立）：");
		for (int i = 0; i < roles.size(); i++) {
			Role role = roles.get(i);
			int count = result.getIntimate(true, role);
			System.out.println(role.getName() + "独立出现的频次为：" + count);
		}
		//输出二维关系
		System.out.println("二维关系分析：");
		String tab = "        ";
		for (int i = -1; i < roles.size(); i++) {
			for (int j = -1; j < roles.size(); j++) {
				if (i == -1 && j == -1) {
					System.out.print(tab);
					continue;
				}
				if (i == -1 && j != -1) {
					System.out.print(roles.get(j).getName() + tab);
					continue;
				}
				if (i != -1 && j == -1) {
					System.out.print(roles.get(i).getName() + tab);
					continue;
				}
				Role role1 = roles.get(i);
				Role role2 = roles.get(j);
				if (role1.equals(role2)) {
					int count = result.getIntimate(false, role1);
					System.out.print(count);
					System.out.print(tab);
				}
				else {
					int count = result.getIntimate(false, role1, role2);
					System.out.print(count);
					System.out.print(tab);
				}
			}
			System.out.println();
		}
	}
}
