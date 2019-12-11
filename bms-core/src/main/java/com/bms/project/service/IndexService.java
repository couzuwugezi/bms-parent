package com.bms.project.service;

import com.bms.project.common.model.LoginFormVo;
import com.bms.project.model.ResponseData;

/**
 * @author liqiang
 * @date 2019/10/26 23:19
 */
public interface IndexService {
    ResponseData login(LoginFormVo loginFormVo);

    ResponseData loadMenus();
}
