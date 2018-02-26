package com.palmg.security.properties.propertyCrypto;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.palmg.security.properties.PropertyCrypto;
import com.palmg.security.properties.exception.FileLoadException;
import com.palmg.security.properties.exception.FileWriteException;

class PropertyCryptoImpTest {

	@Test
	void testGenerateFile() {
		PropertyCrypto propertyCrypto = new PropertyCryptoImp();
		try {
			propertyCrypto.generateKeyFile();
			Properties p1 = new Properties();
			p1.put("testKey1", "testValue1");
			p1.put("testKey2", "testValue2");
			p1.put("testKey3", "testValue3");
			p1.put("testKey4", "testValue4");
			p1.put("testKey5", "testValue5");
			propertyCrypto.generateSecretFile(p1);
			final Properties p2 = propertyCrypto.decryptFile();
			p1.entrySet().forEach(entry -> {
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				assertTrue(p2.getProperty(key).equals(value));
			});
		} catch (FileWriteException | FileLoadException e) {
			fail("Throw Exception :" + e);
		}
	}
	
	@Test
	void testGenerateFileFromProperties() {
		PropertyCrypto propertyCrypto = new PropertyCryptoImp();
		try {
			propertyCrypto.generateKeyFile();
			propertyCrypto.generateSecretFile();
			Properties p1 = new Properties();
			p1.load(PropertyCryptoImpTest.class.getResourceAsStream("/securityInfomation.properties"));
			final Properties p2 = propertyCrypto.decryptFile();
			p1.entrySet().forEach(entry -> {
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				assertTrue(p2.getProperty(key).equals(value));
			});
		} catch (FileWriteException | FileLoadException | IOException e) {
			fail("Throw Exception :" + e);
		}
	}
}
