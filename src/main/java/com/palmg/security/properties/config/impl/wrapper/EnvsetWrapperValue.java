package com.palmg.security.properties.config.impl.wrapper;

import com.palmg.security.properties.config.ConfigException;

/**
 * 通用属性截取配置类 会从初始化的环境变量中获取变量。 优先级分别为：System.env、System.property、properties文件中的配置
 * 如果不指定propertiesName，则不会加载环境变量的数据，仅使用配置文件数据。
 * 
 * @author chenkui
 * @param <E>
 */
public class EnvsetWrapperValue<E> implements WrapperValue<E> {
	private String propertiesName;

	public EnvsetWrapperValue() {
	}

	public EnvsetWrapperValue(String propertiesName) {
		this.propertiesName = propertiesName;
	}

	public void setPropertiesName(String propertiesName) {
		this.propertiesName = propertiesName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E getValue(String property) throws ConfigException {
		if (null != propertiesName) {
			final String pro = System.getProperty(propertiesName);
			final String env = System.getenv(propertiesName);
			property = null == pro ? property : pro;
			property = null == env ? property : env;
		}
		return (E) property;
	}
}
