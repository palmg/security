package com.palmg.security.properties.config;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Test;
import org.junit.runners.MethodSorters;

import com.palmg.security.properties.Scheme;
import com.palmg.security.properties.config.impl.LazyLoadConfig;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LazyLoadConfigTest {
	
	@Test
	public void loadConfigTest1() {
	    System.setProperty("palmg-security-writePath", "myPath");
		try {
			LazyLoadConfig config = new LazyLoadConfig();
			assertTrue(Scheme.AES == config.getScheme());
			assertTrue("PalmgEnvCert.key".equals(config.getKeyFileName()));
			assertTrue("myPath".equals(config.getWritePath()));
			assertTrue(4 == config.getLoadPath().length);
			assertTrue("default".equals(config.getProfile()));
			assertTrue("${profile}".equals(config.getProfileFlag()));
			assertTrue("properties-default.data".equals(config.getCertificateFileName()));
			assertTrue(16 == config.getSeed().length);
		}catch(Exception e) {
			fail("loadConfigTest Exception :", e);
		}
	}
	
	@Test
	public void loadConfigTest2() {
		//set the system property
		try {
		    System.setProperty("palmg-security-profile", "myProfile");
		    System.setProperty("palmg-security-seed", "abbckdjadaadsadad");
			LazyLoadConfig config = new LazyLoadConfig();
			assertTrue(Scheme.AES == config.getScheme());
			assertTrue("PalmgEnvCert.key".equals(config.getKeyFileName()));
			assertTrue(4 == config.getLoadPath().length);
			assertTrue("myProfile".equals(config.getProfile()));
			assertTrue("${profile}".equals(config.getProfileFlag()));
			assertTrue("properties-myProfile.data".equals(config.getCertificateFileName()));
			assertTrue(16 == config.getSeed().length);
		}catch(Exception e) {
			fail("loadConfigTestSetSystem Exception :", e);
		}
	}
}
