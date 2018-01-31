package com.palmg.security.properties.scheme;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.palmg.security.properties.Cryptogram;

public class AESCryptogram implements Cryptogram{
	@Override
	public Key generateKey(byte[] seed) throws NoSuchAlgorithmException {
		if(null == seed) {
			seed = Helper.ins.generateSeed(16);
		}
		KeyGenerator keygen = KeyGenerator.getInstance("AES");
		SecureRandom random = new SecureRandom();
		random.setSeed(seed);
		keygen.init(random);
		return keygen.generateKey();
	}

	@Override
	public byte[] encrypt(byte[] key, byte[] text) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		return cipher.doFinal(text);
	}

	@Override
	public byte[] decrypt(byte[] key, byte[] secret) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		return cipher.doFinal(secret);
	}
}
