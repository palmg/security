package com.palmg.security.properties.scheme;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.Charset;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import com.palmg.security.properties.Cryptogram;
import com.palmg.security.properties.config.ConfigFactory;
import com.palmg.security.properties.scheme.AESCryptogram;

class AESCryptogramTest {
	@Test
	void generateKey() {
		Cryptogram cryptogram = new AESCryptogram();
		Key key = null;
		try {
			key = cryptogram.generateKey(null);
		} catch (NoSuchAlgorithmException e) {
			fail("Throw NoSuchAlgorithmException :" + e);
		}
		assertEquals("AES", key.getAlgorithm());
		assertEquals("RAW", key.getFormat());
		assertEquals(16, key.getEncoded().length);
	}

	@Test
	void encrypt() {
		Cryptogram cryptogram = new AESCryptogram();
		try {
			Key key = cryptogram.generateKey(ConfigFactory.ins.getConfig().getSeed());
			final String text = "www.mahoooo.com";
			byte[] secret = cryptogram.encrypt(key.getEncoded(), text.getBytes(Charset.forName("utf-8")));
			byte[] stext = cryptogram.decrypt(key.getEncoded(), secret);
			assertEquals(text, new String(stext, Charset.forName("utf-8")));
		} catch (Exception e) {
			fail("Throw Exception :" + e);
		} 
	}
}
