package com.bms.project.shiro;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author lixiaohua
 * @since 2019-03-18
 */
@Component
@Data
@ConfigurationProperties(prefix = "shiro")
public class ShiroProperty {

    /**
     * 是否启用验证码
     */
    private boolean ifNeedCaptcha = true;

    /**
     * 重复提交间隔时间
     */
    private Long repeatTime = 5L;

    /**
     * 默认密码
     */
    private String defaultPassword = "bms123";

    /**
     * 登录超时时间
     */
    private Long outTime = 30L;

    /**
     * 散列算法名称
     */
    private String hashAlgorithmName;

    /**
     * 散列迭代次数
     */
    private int hashIterations;

    /**
     * 锁定时间（单位：分钟）
     */
    private int time = 5;

    /**
     * 密码重试次数
     */
    private int times = 5;

    /**
     * 密码有效时间（单位：月）
     */
    private int passwordEffectiveTime = 1;

    /**
     * 密码最小长度
     */
    private int minLength = 6;

    /**
     * 密码最大长度
     */
    private int maxLength = 15;

    /**
     * 必须包含大写字母
     */
    private boolean mustContainUpperCase;

    /**
     * 必须包含小写字母
     */
    private boolean mustContainLowerCase;

    /**
     * 必须包含数字
     */
    private boolean mustContainDigit;


}
