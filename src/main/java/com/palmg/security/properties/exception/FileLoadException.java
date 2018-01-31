package com.palmg.security.properties.exception;

public class FileLoadException extends Exception {
	private static final long serialVersionUID = 666511336101803567L;

	public FileLoadException(String fileName) {
		super(fileName);
	}
	public FileLoadException(String fileName, Exception e) {
		super(fileName, e);
	}
}
