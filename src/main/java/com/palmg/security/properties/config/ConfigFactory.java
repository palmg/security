package com.palmg.security.properties.config;

import com.palmg.security.properties.config.impl.LazyLoadConfig;

public enum ConfigFactory {
	ins;
	private Config config;
	public Config getConfig() {
		if(null == config) {
			synchronized (ConfigFactory.class) {
				if(null == config) {
					config = new LazyLoadConfig();
				}
			}
		}
		return config;
	}
}
