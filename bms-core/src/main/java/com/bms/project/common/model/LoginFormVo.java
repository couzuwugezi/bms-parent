package com.bms.project.common.model;

import lombok.Data;

/**
 * @author liqiang
 * @date 2019/10/26 23:27
 */
@Data
public class LoginFormVo {
    private String username;
    private String password;
    private String captchaCode;
}
