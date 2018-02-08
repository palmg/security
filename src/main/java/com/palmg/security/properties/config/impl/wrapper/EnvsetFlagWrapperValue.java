package com.palmg.security.properties.config.impl.wrapper;

import com.palmg.security.properties.config.ConfigException;

public class EnvsetFlagWrapperValue<E> extends EnvsetWrapperValue<E> implements WrapperValue<E> {
	public EnvsetFlagWrapperValue(){
		super();
	}
	public EnvsetFlagWrapperValue(String name){
		super(name);
	}
	@SuppressWarnings("unchecked")
	@Override
	public E getValue(String property) throws ConfigException {
		String value = super.getValue(property).toString();
		if (value.contains(",")) {
			return (E) valueFlagReplace(value.split(","));
		} else {
			return (E) valueFlagReplace(value)[0];
		}
	}

	private String[] valueFlagReplace(String... values) {
		for (int index = 0; values.length > index; index++) {
			switch (values[index]) {
			case "${user.home}":
				values[index] = "file:" + System.getProperty("user.home") + "/";
				break;
			case "${user.dir}":
				values[index] = "file:" + System.getProperty("user.dir") + "/";
				break;
			}
		}
		return values;
	}
}
