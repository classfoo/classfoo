package org.classfoo.tools.microblog.impl.sina;

import java.util.Map;

import org.classfoo.tools.bot.Bot;
import org.classfoo.tools.bot.BotFactory;
import org.springframework.stereotype.Component;

@Component
public class SinaWeiboBotFactory implements BotFactory {

	public static final String NAME = "com.sina.weibo";

	public String getName() {
		return NAME;
	}

	public Bot createBot(String name, Map<String, String> options) {
		// TODO Auto-generated method stub
		return null;
	}

}
