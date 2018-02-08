package com.palmg.security.properties.config.impl.wrapper;

import java.util.Properties;

/**
 * 多重Properties配置的处理类，在获取属性的时候，会将列表中多个属性值合并到一处
 * @author chenkui
 */
public class PropertiesWrapper {
	private Properties[] list;

	Properties[] get() {
		return list;
	}

	void set(Properties[] list) {
		this.list = list;
	}

	String getValue(String key) {
		String value = list[0].getProperty(key);
		for (Properties p : list) {
			value = p.getProperty(key, value);
		}
		return value;
	}
}
