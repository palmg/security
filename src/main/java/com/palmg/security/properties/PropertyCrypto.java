package com.palmg.security.properties;

import java.util.Properties;

import com.palmg.security.properties.entity.KeyDocument;
import com.palmg.security.properties.exception.FileLoadException;
import com.palmg.security.properties.exception.FileWriteException;
import com.palmg.security.properties.propertyCrypto.PropertyCryptoImp;;

public interface PropertyCrypto {
	static public PropertyCrypto getNew() {
		return new PropertyCryptoImp();
	}

	KeyDocument generateKeyFile() throws FileWriteException ;

	KeyDocument readKeyFile() throws FileLoadException;
	
	void generateSecretFile(Properties Properties) throws FileWriteException;

	Properties decryptFile() throws FileLoadException ;
}
