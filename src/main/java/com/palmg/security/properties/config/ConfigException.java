package com.palmg.security.properties.config;

public class ConfigException extends Exception {
	private static final long serialVersionUID = 1L;

	public ConfigException(String msg) {
		super(msg);
	}

	public ConfigException(String msg, Exception e) {
		super(msg, e);
	}
}
