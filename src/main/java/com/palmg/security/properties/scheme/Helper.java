package com.palmg.security.properties.scheme;

import java.security.SecureRandom;

public enum Helper {
	ins;
	/**
	 * 随机种子，随机性比外部随机性差
	 */
	final private char[] ROOT_SEED = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
	/**
	 * 随机种子长度
	 */
	final private int ROOT_SEED_LEN = ROOT_SEED.length;

	/**
	 * 生成随机种子
	 * @param len
	 * @return
	 */
	public byte[] generateSeed(final int len) {
		SecureRandom random = new SecureRandom();
		byte[] seed = new byte[len];
		int curLen = 0;
		int index = -1;
		while (len > curLen) {
			index = Math.abs(random.nextInt(ROOT_SEED_LEN));
			seed[curLen++] = (byte) ROOT_SEED[index];
		}
		return seed;
	}
}
