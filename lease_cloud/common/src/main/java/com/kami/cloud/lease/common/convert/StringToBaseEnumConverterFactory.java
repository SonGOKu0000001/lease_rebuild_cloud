package com.kami.cloud.lease.common.convert;

import com.kami.cloud.lease.common.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

/**
 * 将所有继承 BaseEnum 的枚举与 String 之间做转换（用于 0/1 入参）
 */
@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {

    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return new Converter<String, T>() {
            @Override
            public T convert(String source) {
                for (T value : targetType.getEnumConstants()) {
                    if (value.getCode().equals(Integer.valueOf(source))) {
                        return value;
                    }
                }
                throw new IllegalArgumentException("code非法");
            }
        };
    }
}