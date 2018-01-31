package com.palmg.security.properties.config.impl.wrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.palmg.security.properties.config.Config;
import com.palmg.security.properties.config.ConfigException;

/**
 * 对Properties进行延迟加载包装。
 * of方法用于返回 name指定的属性值，直到调用of方法时才会去加载Properties并加载对应的值
 * @author chenkui
 *
 * @param <E>
 */
public class LoadWrapper<E> {
	private PropertiesWrapper propertiesWrapper;
	private E e;
	private String name;
	private WrapperValue<E> wrapperValue;

	public LoadWrapper(PropertiesWrapper propertiesWrapper, String name, WrapperValue<E> wrapperValue) {
		this.propertiesWrapper = propertiesWrapper;
		this.name = name;
		this.wrapperValue = wrapperValue;
	}

	public E of() throws ConfigException {
		if (null == e) {
			loadProperties();
			synchronized (this) {
				e = wrapperValue.getValue(propertiesWrapper.getValue(name));
			}
		}
		return e;
	}

	private void loadProperties() throws ConfigException {
		if (null == propertiesWrapper.get()) {
			synchronized (propertiesWrapper) {
				if (null == propertiesWrapper.get()) {
					ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
					try {
						List<Resource> resources = new ArrayList<Resource>();
						resources.addAll(Arrays.asList(resourceLoader.getResources(Config.FILE_PATH_LOOP[1])));
						resources.addAll(Arrays.asList(resourceLoader.getResources(Config.FILE_PATH_LOOP[0])));
						if (0 < resources.size()) {
							List<Properties> list = new ArrayList<Properties>();
							for (Resource x : resources) {
								if (x.exists()) {
									Properties p = new Properties();
									p.load(x.getInputStream());
									list.add(p);
								}
							}
							propertiesWrapper.set(list.toArray(new Properties[list.size()]));
						}
					} catch (IOException e) {
						throw new ConfigException("Read properties erro", e);
					}
					if (null == propertiesWrapper.get() || 0 == propertiesWrapper.get().length) {
						throw new ConfigException("No config properties exists");
					}
				}
			}
		}
	}
}