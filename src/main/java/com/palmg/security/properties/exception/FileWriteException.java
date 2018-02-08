package com.palmg.security.properties.exception;

public class FileWriteException extends Exception {
	private static final long serialVersionUID = 666511336101803567L;
	public FileWriteException(String fileName) {
		super(fileName);
	}
	public FileWriteException(Exception e) {
		super(e);
	}
	public FileWriteException(String fileName, Exception e) {
		super(fileName, e);
	}
}
