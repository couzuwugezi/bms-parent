package com.bms.project.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * @author liqiang
 * @date 2019/11/8 15:41
 * <p>
 * 重写shiro的校验表单
 */
public class CaptchaToken extends UsernamePasswordToken {
    /**
     * 验证码
     */
    private String captchaCode;

    public CaptchaToken(UsernamePasswordToken token, String captchaCode) {
        super(token.getUsername(), token.getPassword(), "");
        this.captchaCode = captchaCode;
    }

    String getCaptchaCode() {
        return captchaCode;
    }

}
