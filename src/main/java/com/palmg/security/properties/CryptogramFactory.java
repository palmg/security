package com.palmg.security.properties;

import com.palmg.security.properties.scheme.AESCryptogram;

public class CryptogramFactory {
	static volatile private CryptogramFactory factory;
	private CryptogramFactory() {}
	static public CryptogramFactory getInstance() {
		if(null == factory) {
			synchronized (CryptogramFactory.class) {
				if(null == factory) {
					factory = new CryptogramFactory();
				}
			}
		}
		return factory;
	}
	
	//获取加密方案
	public Cryptogram getCryptogram(Scheme scheme) {
		Cryptogram cryptogram = null;
		switch (scheme) {
		case AES:
		default:
			cryptogram = new AESCryptogram();
			break;
		}
		return cryptogram;
	}
}
