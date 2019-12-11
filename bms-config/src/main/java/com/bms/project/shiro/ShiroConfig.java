package com.bms.project.shiro;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.session.mgt.eis.SessionIdGenerator;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Properties;

/**
 * @author liqiang
 * @date 2019/10/26 22:04
 */
@Configuration
public class ShiroConfig {

    @Bean
    public Producer producer() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "yes");
        properties.setProperty("kaptcha.border.color", "105,179,90");
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        properties.setProperty("kaptcha.textproducer.font.size", "25");
        properties.setProperty("kaptcha.textproducer.char.space", "10");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.DefaultNoise");
        properties.setProperty("kaptcha.image.width", "115");
        properties.setProperty("kaptcha.image.height", "48");
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }

    /**
     * 负责shiroBean的生命周期
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @DependsOn({"lifecycleBeanPostProcessor", "credentialsMatcher"})
    public Realm realm(ShiroProperty shiroProperty) {
        CustomerRealm customerRealm = new CustomerRealm();
        customerRealm.setCredentialsMatcher(credentialsMatcher(shiroProperty));
        return customerRealm;
    }

    @Bean
    @DependsOn({"realm", "sessionManager"})
    public SecurityManager securityManager(ShiroProperty shiroProperty) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm(shiroProperty));
        securityManager.setSessionManager(sessionManager());
        return securityManager;
    }

    @Bean
    @DependsOn("securityManager")
    public ShiroFilterFactoryBean shiroFilterChainDefinition(ShiroProperty shiroProperty) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        shiroFilterFactoryBean.getFilters().put("authc", new CustomAccessControlFilter());
        shiroFilterFactoryBean.getFilters().put("html", new CustomNoAjaxFilter());

        shiroFilterFactoryBean.setSecurityManager(securityManager(shiroProperty));

        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/login**", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/captcha*", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/check", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/health", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/error/**", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/logout", "anon");

        //静态资源
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/favicon.ico", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/css/**", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/assets/**", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/js/**", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/fonts/**", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/img/**", "anon");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/**/*.html", "anon");

        shiroFilterFactoryBean.setLoginUrl("/login.html");
        shiroFilterFactoryBean.setSuccessUrl("/index.html");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/", "html");
        shiroFilterFactoryBean.getFilterChainDefinitionMap().put("/**", "authc");

        return shiroFilterFactoryBean;
    }

    @Bean
    @DependsOn("securityManager")
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(ShiroProperty shiroProperty) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager(shiroProperty));
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * 限制登录次数
     *
     * @param shiroProperty
     *
     * @return 匹配器
     */
    @Bean
    public CredentialsMatcher credentialsMatcher(ShiroProperty shiroProperty) {
        CustomCredentialsMatcher customCredentialsMatcher = new CustomCredentialsMatcher();
        customCredentialsMatcher.setHashAlgorithmName(shiroProperty.getHashAlgorithmName());
        customCredentialsMatcher.setHashIterations(shiroProperty.getHashIterations());
        return customCredentialsMatcher;
    }

    @Bean
    public SimpleCookie simpleCookie() {
        SimpleCookie simpleCookie = new SimpleCookie("bms_shiro_session");
        simpleCookie.setHttpOnly(true);
        simpleCookie.setPath("/");
        // maxAge=-1 表示浏览器关闭时失效此Cookie
        simpleCookie.setMaxAge(-1);
        return simpleCookie;
    }

    /**
     * 配置会话ID生成器
     */
    @Bean
    public SessionIdGenerator sessionIdGenerator() {
        return new JavaUuidSessionIdGenerator();
    }

    @Bean
    @DependsOn("simpleCookie")
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionIdCookie(simpleCookie());
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        // 是否开启删除无效的session对象，默认为true
        sessionManager.setDeleteInvalidSessions(true);
        // 是否开启定时调度器进行检测过期session，默认为true
        sessionManager.setSessionValidationSchedulerEnabled(true);
//        sessionManager.setSessionDAO(new RedisSessionDAO());
        return sessionManager;
    }
}
