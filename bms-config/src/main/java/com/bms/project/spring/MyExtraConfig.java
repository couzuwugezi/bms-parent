package com.bms.project.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author liqiang
 * @date 2019/10/27 21:21
 */
@Component
public class MyExtraConfig extends WebMvcConfigurerAdapter {

    @Autowired
    @Qualifier("checkRefuseRepeat")
    private AbstractCustomInteceptor abstractCustomInteceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] strings = {
                "/health",
                "/error/**",
                "/login**",
                "/captcha*",
                "/check",
                "/loadMainDiv",
                "/logout"
        };
        registry.addInterceptor(abstractCustomInteceptor).addPathPatterns("/**").excludePathPatterns(strings);
    }
}


