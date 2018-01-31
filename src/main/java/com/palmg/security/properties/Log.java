package com.palmg.security.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Log {
	ins;
	static Logger LOG = LoggerFactory.getLogger("Common Log");
	public void info(String msg){
		LOG.info(msg);
	}
	public void error(String msg) {
		LOG.error(msg);
	}
}
