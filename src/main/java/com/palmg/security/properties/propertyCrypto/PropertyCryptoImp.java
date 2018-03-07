package com.palmg.security.properties.propertyCrypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

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
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				FileOutputStream fos = new FileOutputStream(path); 
				) {
			Scheme scheme = config.getScheme();
			Cryptogram crypto = CryptogramFactory.getInstance().getCryptogram(scheme);
			Key key = crypto.generateKey(config.getSeed());
			KeyDocument doc = new KeyDocument(key.getEncoded(), scheme, config.getProfile());
			oos.writeObject(doc);
			byte[] bytes = bos.toByteArray();
			byte[] base64Byte = Base64.getEncoder().encode(bytes);
			fos.write(base64Byte);
			Log.ins.info("Key file has generate:" + path + ". Current profie: " + config.getProfile());
			return doc;
		} catch (ConfigException | NoSuchAlgorithmException | IOException e) {
			throw new FileWriteException("Generate secret key certificate error", e);
		}
	}
	
	@Override
	public KeyDocument readKeyFile() throws FileLoadException {
		try (InputStream in = getExistsKeyFile()) {
			byte[] buffer = new byte[1024];
			byte[] base64Byte = new byte[0];
			int len = 0;
			while( (len = in.read(buffer)) != -1) {
				byte[] temp = base64Byte;
				base64Byte = new byte[temp.length + len];
				System.arraycopy(temp, 0, base64Byte, 0, temp.length);
				System.arraycopy(buffer, 0, base64Byte, temp.length, len);
			}
			byte[] oriByte = Base64.getDecoder().decode(base64Byte);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(oriByte));
			KeyDocument keyDoc = (KeyDocument) ois.readObject();
			return keyDoc;
		} catch (IOException | ClassNotFoundException e) {
			throw new FileLoadException("Load secret key certificate error", e);
		}
	}

	@Override
	public void generateSecretFile() throws FileWriteException {
		try {
			Config config = ConfigFactory.ins.getConfig();
			String[] paths = config.getLoadPath();
			final String fileName = config.getPropertiesFileName();
			boolean isLoadProperties = false;
			ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
			List<String> checkList = new LinkedList<>();
			for (String path : paths) {
				String temp = path + fileName;
				checkList.add(temp);
				Resource resource = resourceLoader.getResource(temp);
				if (resource.exists()) {
					Properties properties = new Properties();
					properties.load(resource.getInputStream());
					isLoadProperties = true;
					generateSecretFile(properties);
					break;
				}
			}
			if(false == isLoadProperties) {
				throw new FileWriteException("Properties file not exists in such path:" + checkList);
			}
		} catch (ConfigException | IOException e) {
			throw new FileWriteException("",e);
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
			byte[] base64url = Base64.getEncoder().encode(ciphertext);
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
			byte[] ciphertext = Base64.getDecoder().decode(read(keyDoc.getProfile()));
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

	private InputStream getExistsKeyFile() throws FileLoadException {
		InputStream inputStream = null;
		Config config = ConfigFactory.ins.getConfig();
		try {
			final String[] paths = config.getLoadPath();
			final String fileName = config.getKeyFileName();
			ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
			for (String path : paths) {
				String temp = path + fileName;
				Resource resource = resourceLoader.getResource(temp);
				if (resource.exists()) {
					inputStream = resource.getInputStream();
					break;
				}
			}
		} catch (ConfigException | IOException e) {
			throw new FileLoadException("Load Secret key certificate Error!", e);
		}
		if (null == inputStream) {
			throw new FileLoadException("Secret key certificate not exists!");
		}
		return inputStream;
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
		File file = new File(config.getWritePath() + config.getCertificateFileName().replace(config.getProfileFlag(), profile));
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
		String fileName = config.getCertificateFileName().replace(config.getProfileFlag(), profile);
		ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
		Resource resource = null;
		for (String path : paths) {
			resource = resourceLoader.getResource(path + fileName);
			if (resource.exists()) {
				LOG.info("Ciphertext File has read from: " + resource.getDescription());
				break;
			}
		}
		if (resource.exists()) {
			final int bufferSize = 100;
			int readLen = 0;
			byte[] buffer = new byte[bufferSize];
			ByteReader reader = null;
			try (InputStream is = resource.getInputStream()) {
				reader = new ByteReader();
				while (-1 != (readLen = is.read(buffer, 0, bufferSize))) {
					reader.add(buffer, readLen);
				}
			} catch (IOException e) {
				throw new FileLoadException("read file error:", e);
			}
			return reader.get();
		} else {
			throw new FileLoadException("File is not exists!");
		}
	}
}

class ByteReader {
	private List<byte[]> buffers;
	private int length;

	public ByteReader() {
		buffers = new LinkedList<byte[]>();
		length = 0;
	}

	public void add(byte[] data, int offset) {
		length += offset;
		byte[] mem = new byte[offset];
		System.arraycopy(data, 0, mem, 0, offset);
		buffers.add(mem);
	}

	public byte[] get() {
		byte[] result = new byte[length];
		int offset = 0;
		for (byte[] bytes : buffers) {
			System.arraycopy(bytes, 0, result, offset, bytes.length);
			offset += bytes.length;
		}
		return result;
	}
}
