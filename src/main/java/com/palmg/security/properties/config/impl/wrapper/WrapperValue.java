package com.palmg.security.properties.config.impl.wrapper;

import com.palmg.security.properties.config.ConfigException;

/**
 * 数据延迟加载的包装借口。
 * 实现getValue方法之后，直到数据被使用时才被加载
 * @author chenkui
 *
 * @param <E>
 */
public interface WrapperValue<E> {
	E getValue(String property) throws ConfigException;
}
