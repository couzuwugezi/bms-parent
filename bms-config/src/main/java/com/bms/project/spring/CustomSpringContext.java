package com.bms.project.spring;

import org.springframework.context.ApplicationContext;

/**
 * @author liqiang
 * @date 2019/10/26 14:37
 */
public class CustomSpringContext {

    private static ApplicationContext applicationContext;

    public CustomSpringContext() {
    }

    public static void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
