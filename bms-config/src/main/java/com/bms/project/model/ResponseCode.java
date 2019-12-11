package com.bms.project.model;

/**
 * @author liqiang
 * @date 2019-04-08 09:32
 * 返回码定义
 */
public enum ResponseCode {
    /*平台标准错误码 开始*/
    MS_SUCCESS("9999", "成功"),
    MS_SYSTEM_ERROR("0000", "系统异常"),
    LOGIN_ERROR("LOGIN_ERROR", "登录异常"),
    NEED_UPDATE_PWD("NEED_UPDATE_PWD", "密码过期,需要修改密码"),
    OPERATE_DB_ERROR("OPERATE_DB_ERROR", "操作数据库失败"),
    SUBMIT_REPEAT("SUBMIT_REPEAT", "请勿重复操作"),
    ERROR_CAPTCHA("ERROR_CAPTCHA", "验证码有误"),
    SESSION_CANNOT_BE_NULL("SESSION_CANNOT_BE_NULL", "session不能为null"),
    LOGIN_LIMIT("LOGIN_LIMIT", "登录次数限制");

    /**
     * 代码
     */
    private String code;
    /**
     * 描述
     */
    private String desc;

    ResponseCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ResponseCode getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ResponseCode param : values()) {
            if (param.getCode().equals(code)) {
                return param;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
