package com.palmg.security.properties.config.impl;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

import com.palmg.security.properties.Scheme;
import com.palmg.security.properties.config.Config;
import com.palmg.security.properties.config.ConfigException;
import com.palmg.security.properties.config.impl.wrapper.EnvsetFlagWrapperValue;
import com.palmg.security.properties.config.impl.wrapper.EnvsetWrapperValue;
import com.palmg.security.properties.config.impl.wrapper.LoadWrapper;
import com.palmg.security.properties.config.impl.wrapper.PropertiesWrapper;
import com.palmg.security.properties.config.impl.wrapper.WrapperValue;
import com.palmg.security.properties.scheme.Helper;

public class LazyLoadConfig implements Config {
	private PropertiesWrapper propertieWrapper;
	private LoadWrapper<Scheme> scheme;
	private LoadWrapper<String> keyFileName;
	private LoadWrapper<String[]> loadPath;
	private LoadWrapper<String> writePath;
	private LoadWrapper<String> profile;
	private LoadWrapper<String> profileFlag;
	private LoadWrapper<String> propertiesFileName;
	private LoadWrapper<String> certificateFileName;
	private LoadWrapper<byte[]> seed;

	public LazyLoadConfig() {
		propertieWrapper = new PropertiesWrapper();
	}

	@Override
	public Scheme getScheme() throws ConfigException {// 加密方案
		return valueOf(scheme, "scheme", new WrapperValue<Scheme>() {
			@Override
			public Scheme getValue(String property) {
				return Scheme.valueOf(property);
			}
		});
	}

	@Override
	public String getKeyFileName() throws ConfigException {// 生成的key文件名称
		return valueOf(keyFileName, "keyFileName");
	}

	@Override
	public String[] getLoadPath() throws ConfigException {
		return valueOf(loadPath, "loadPath", new EnvsetFlagWrapperValue<String[]>("palmg-security-loadPath"));
	}

	@Override
	public String getWritePath() throws ConfigException {
		return valueOf(writePath, "writePath", new EnvsetFlagWrapperValue<String>("palmg-security-writePath"))
				.replace("file:", "").replace("classpath:", "");
	}

	@Override
	public String getProfile() throws ConfigException {
		return valueOf(profile, "profile");
	}

	@Override
	public String getProfileFlag() throws ConfigException {
		return valueOf(profileFlag, "profileFlag");
	}

	@Override
	public String getPropertiesFileName() throws ConfigException {
		return valueOf(propertiesFileName, "propertiesFileName");
	}

	@Override
	public String getCertificateFileName() throws ConfigException {
		return valueOf(certificateFileName, "certificateFileName");
	}

	@Override
	public byte[] getSeed() throws ConfigException {
		return valueOf(seed, "seed", new WrapperValue<byte[]>() {

			@Override
			/**
			 * 获取配置中的种子数据，长度必须为16。 如果种子太长，则截取；太短则补足；不存在则生成
			 */
			public byte[] getValue(String property) throws ConfigException {
				property = new EnvsetWrapperValue<String>("palmg-security-seed").getValue(property);

				if (null == property || property.isEmpty())
					return Helper.ins.generateSeed(16);

				if (16 < property.length()) {
					property = property.substring(0, 16);
				} else if (16 > property.length()) {
					property = cutProperty(property);
				}
				return property.getBytes(Charset.forName("UTF-8"));
			}

			private String cutProperty(String property) {
				property += property;
				if (16 < property.length()) {
					return property = property.substring(0, 16);
				} else if (16 > property.length()) {
					return cutProperty(property);
				} else {
					return property;
				}
			}
		});
	}

	/**
	 * 加载数据，环境变量数据默认为 palmg-security-${name}
	 * 
	 * @param loadWrapper
	 * @param name
	 * @return
	 * @throws ConfigException
	 */
	private <T> T valueOf(LoadWrapper<T> loadWrapper, String name) throws ConfigException {
		return valueOf(loadWrapper, name, new EnvsetWrapperValue<T>("palmg-security-" + name));
	}

	@SuppressWarnings("unchecked")
	private <T> T valueOf(LoadWrapper<T> loadWrapper, String name, WrapperValue<T> wrapperValue)
			throws ConfigException {
		if (null == loadWrapper) {
			synchronized (name) {
				try {
					Field field = this.getClass().getDeclaredField(name);
					field.setAccessible(true);
					field.set(this, new LoadWrapper<T>(propertieWrapper, name, wrapperValue));
					loadWrapper = (LoadWrapper<T>) field.get(this);
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
						| IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return loadWrapper.of();
	}
}
