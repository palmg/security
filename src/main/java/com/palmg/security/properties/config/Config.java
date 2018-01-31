package com.palmg.security.properties.config;

import com.palmg.security.properties.Scheme;

public interface Config {
	final static String[] FILE_PATH_LOOP = {
			"classpath:palmg-security-properties.properties",
			"classpath*:palmg/config/**/palmg-security-properties.properties"
	};
	
	Scheme getScheme() throws ConfigException ;
	String getKeyFileName() throws ConfigException ;
	String[] getLoadPath() throws ConfigException ;
	String getWritePath() throws ConfigException ;
	String getProfile() throws ConfigException ;
	String getProfileFlag() throws ConfigException ;
	String getCertificateFileName() throws ConfigException ;
	byte[] getSeed() throws ConfigException;
}
