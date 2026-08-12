package com.kami.cloud.lease.common.config;

import com.kami.cloud.lease.common.convert.StringToBaseEnumConverterFactory;
import com.kami.cloud.lease.common.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 通用 Web MVC 配置：注册 AuthInterceptor 与枚举转换器。
 * 网关（WebFlux）不依赖本 common 模块，不会受影响。
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private StringToBaseEnumConverterFactory stringToBaseEnumConverterFactory;
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(this.stringToBaseEnumConverterFactory);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.authInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login/**");
        registry.addInterceptor(this.authInterceptor)
                .addPathPatterns("/app/**")
                .excludePathPatterns("/app/login/**");
    }
}