package org.classfoo.drools.familyrelations;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * 测试类
 * <p>Copyright: Copyright (c) 2013<p>
 * <p>succez<p>
 * @author ClassFoo
 * @createdate 2013-11-1
 */
public class DroolsTest {

	public static final void main(String[] args) {
		try {
			KnowledgeBase kbase = readKnowledgeBase();
			StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
			KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession, "test");

			People jby = new People("贾宝玉", true, 16);
			ksession.insert(jby);
			People jz = new People("贾政", true, 50);
			ksession.insert(jz);
			People js = new People("贾赦", true, 55);
			ksession.insert(js);
			People jm = new People("贾母", false, 80);
			ksession.insert(jm);
			Relation fz = new Relation("父亲", jz, jby);
			Relation xd = new Relation("哥哥", js, jz);
			Relation mq1 = new Relation("母亲", jm, js);
			Relation mq2 = new Relation("母亲", jm, jz);
			ksession.insert(fz);
			ksession.insert(xd);
			ksession.insert(mq1);
			ksession.insert(mq2);

			People lrh = new People("林如海", true, 50);
			ksession.insert(lrh);
			People ldy = new People("林黛玉", false, 16);
			ksession.insert(ldy);
			People jm2 = new People("贾敏", false, 40);
			ksession.insert(jm2);
			Relation fq = new Relation("丈夫", lrh, jm2);
			Relation xm = new Relation("妹妹", jm2, jz);
			Relation mq3 = new Relation("母亲", jm2, ldy);
			Relation mq4 = new Relation("母亲", jm, jm2);
			ksession.insert(fq);
			ksession.insert(xm);
			ksession.insert(mq3);
			ksession.insert(mq4);

			ksession.fireAllRules();
			logger.close();
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static KnowledgeBase readKnowledgeBase() throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("familyrelations.drl"), ResourceType.DRL);
		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		return kbase;
	}
}
