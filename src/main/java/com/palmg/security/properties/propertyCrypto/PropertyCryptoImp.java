package com.palmg.security.properties.propertyCrypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.palmg.security.properties.Cryptogram;
import com.palmg.security.properties.CryptogramFactory;
import com.palmg.security.properties.Log;
import com.palmg.security.properties.PropertyCrypto;
import com.palmg.security.properties.Scheme;
import com.palmg.security.properties.config.Config;
import com.palmg.security.properties.config.ConfigException;
import com.palmg.security.properties.config.ConfigFactory;
import com.palmg.security.properties.entity.KeyDocument;
import com.palmg.security.properties.exception.FileLoadException;
import com.palmg.security.properties.exception.FileWriteException;

/**
 * <h3>生成与解析加密文件的相关方法。</h3>
 * 
 * <h4>generateSecretFile</h4>
 * <p>
 * 方法使用已有的密钥生成加密段。明文结构如下<br>
 * byte[0]:flag标记长度。 byte[1]~byte[1+byte[1]]:flag内容，由@FILE_FLAG定义。
 * byte[1+byte[1]+1]~byte[length]:原始properties结构的内容。
 * </p>
 * 
 * @author chenkui
 *
 */
public class PropertyCryptoImp implements PropertyCrypto {
	static Logger LOG = LoggerFactory.getLogger(PropertyCryptoImp.class);

	static final byte[] FILE_FLAG = "palmg".getBytes(Charset.forName("UTF-8"));
	static final int LEN = FILE_FLAG.length;

	@Override
	public KeyDocument generateKeyFile() throws FileWriteException {
		Config config = null;
		config = ConfigFactory.ins.getConfig();
		String path;
		try {
			path = config.getWritePath() + config.getKeyFileName();
		} catch (ConfigException e) {
			throw new FileWriteException("Generate write path error", e);
		}
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(path))) {
			Scheme scheme = config.getScheme();
			Cryptogram crypto = CryptogramFactory.getInstance().getCryptogram(scheme);
			Key key = crypto.generateKey(config.getSeed());
			KeyDocument keyDoc = new KeyDocument(key.getEncoded(), scheme, config.getProfile());
			out.writeObject(keyDoc);
			Log.ins.info("Key file has generate:" + path);
			return keyDoc;
		} catch (ConfigException | NoSuchAlgorithmException | IOException e) {
			throw new FileWriteException("Generate secret key certificate error", e);
		}
	}

	@Override
	public KeyDocument readKeyFile() throws FileLoadException {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(getExistsKeyFile()))) {
			KeyDocument keyDoc = (KeyDocument) in.readObject();
			return keyDoc;
		} catch (IOException | ClassNotFoundException e) {
			throw new FileLoadException("Load secret key certificate error", e);
		}
	}

	@Override
	public void generateSecretFile(Properties Properties) throws FileWriteException {
		try (ByteArrayOutputStream byteOps = new ByteArrayOutputStream();
				ObjectOutputStream objectOps = new ObjectOutputStream(byteOps);) {
			objectOps.writeObject(Properties);
			byte[] text = byteOps.toByteArray();
			KeyDocument keyDoc = readKeyFile();
			Cryptogram cryptogram = CryptogramFactory.getInstance().getCryptogram(keyDoc.getScheme());
			byte[] plaintext = encrypt(text);
			byte[] ciphertext = cryptogram.encrypt(keyDoc.getKey(), plaintext);
			byte[] base64url = Base64.getUrlEncoder().encode(ciphertext);
			write(keyDoc.getProfile(), base64url);
		} catch (Exception e) {
			throw new FileWriteException("Generate secret file error", e);
		}
	}

	@Override
	public Properties decryptFile() throws FileLoadException {
		Properties properties = null;
		byte[] text = null;
		try {
			KeyDocument keyDoc = readKeyFile();
			byte[] ciphertext = Base64.getUrlDecoder().decode(read(keyDoc.getProfile()));
			Cryptogram cryptogram = CryptogramFactory.getInstance().getCryptogram(keyDoc.getScheme());
			byte[] plaintext = cryptogram.decrypt(keyDoc.getKey(), ciphertext);
			text = decrypt(plaintext);
		} catch (Exception e) {
			throw new FileLoadException("decrypt error!", e);
		}
		try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(text))) {
			properties = (Properties) is.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new FileLoadException("Create Properties instance error!", e);
		}
		return properties;
	}

	private String getExistsKeyFile() throws FileLoadException {
		String filePath = null;
		Config config = ConfigFactory.ins.getConfig();
		try {
			final String[] paths = config.getLoadPath();
			final String fileName = config.getKeyFileName();
			for (String path : paths) {
				String temp = path + fileName;
				File file = new File(temp);
				if (file.exists()) {
					filePath = temp;
					break;
				}
			}
		} catch (ConfigException e) {
			throw new FileLoadException("Load Secret key certificate Error!", e);
		}
		if (null == filePath) {
			throw new FileLoadException("Secret key certificate not exists!");
		}
		return filePath;
	}

	/**
	 * an encrypt for out text byte[0]:flag length。 byte[1]~byte[1+byte[1]]:flag
	 * information, is define by FILE_FLAG。 byte[1+byte[1]+1]~byte[length]: the
	 * properties information。
	 * 
	 * @param texts
	 * @return byte[][]:[0] is the random for xor. [1] is the computed result.
	 */
	private byte[] encrypt(byte[] texts) {
		int textLen = texts.length;
		byte[] plainText = new byte[1 + LEN + textLen];
		plainText[0] = (byte) LEN;
		int flagIndex = 0;
		while (LEN > flagIndex) {
			plainText[++flagIndex] = FILE_FLAG[flagIndex - 1];
		}
		++flagIndex;
		for (int index = 0; textLen > index; index++) {
			plainText[index + flagIndex] = texts[index];
		}
		return plainText;
	}

	private byte[] decrypt(byte[] ciphertext) throws FileLoadException {
		final int flagLen = ciphertext[0];
		byte[] flag = new byte[flagLen];
		int flagIndex = 0;
		while (flagLen > flagIndex) {
			flag[flagIndex] = ciphertext[++flagIndex];
		}
		if (!flagCompare(flag))
			throw new FileLoadException("The FILE_FLAG is not match!");
		++flagIndex;
		int textLen = ciphertext.length - flagLen - 1;
		byte[] text = new byte[textLen];
		for (int index = 0; textLen > index; index++) {
			text[index] = ciphertext[flagIndex + index];
		}
		return text;
	}

	private boolean flagCompare(byte[] flag) {
		boolean result = LEN == flag.length;
		if (result) {
			for (int index = 0; LEN > index; index++) {
				result = FILE_FLAG[index] == flag[index];
				if (!result)
					break;
			}
		}
		return result;
	}

	private void write(String profile, byte[] text) throws FileWriteException, ConfigException {
		Config config = ConfigFactory.ins.getConfig();
		File file = new File(config.getWritePath() + config.getCertificateFileName());
		try (FileOutputStream fos = new FileOutputStream(file)) {
			if (!file.exists()) {
				file.createNewFile();
			}
			fos.write(text, 0, text.length);
			LOG.info("Ciphertext File has created to: " + file.getPath());
		} catch (IOException e) {
			throw new FileWriteException(e);
		}

	}

	private byte[] read(String profile) throws FileLoadException, ConfigException {
		Config config = ConfigFactory.ins.getConfig();
		String[] paths = config.getLoadPath();
		String fileName = config.getCertificateFileName();
		File file = null;
		for (String path : paths) {
			file = new File(path + fileName);
			if (file.exists())
				break;
		}
		if (file.exists()) {
			byte[] result = new byte[(int) file.length()];
			try (FileInputStream is = new FileInputStream(file)) {
				is.read(result);
			} catch (IOException e) {
				throw new FileLoadException("read file error:" + file.getPath(), e);
			}
			return result;
		} else {
			throw new FileLoadException("File is not exists:" + file.getPath());
		}
	}
}
