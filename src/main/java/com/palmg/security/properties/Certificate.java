package com.palmg.security.properties;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.palmg.security.properties.exception.FileWriteException;

public class Certificate {
	static Logger LOG = LoggerFactory.getLogger(Certificate.class);
	public static void main(String[] args) throws FileWriteException, IOException {
		//PropertyCrypto.getNew().generateSecretFile(new Properties());
		System.out.println(System.getProperty("user.home"));
		ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
		Resource[] resource = resourceLoader.getResources("file:/home/chenkui/envcert.key");
		resource = resourceLoader.getResources("classpath:/**envcert.key");
	}
}
