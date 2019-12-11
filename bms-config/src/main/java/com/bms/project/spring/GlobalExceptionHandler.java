package com.bms.project.spring;

import org.apache.shiro.authz.AuthorizationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liqiang
 * @date 2019/10/27 18:44
 * 自定义全局处理异常
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(BmsException.class)
    public Map<String, Object> handleCustomException(BmsException bmsException) {
        Map<String, Object> errorResultMap = new HashMap<>(2);
        errorResultMap.put("rtnFlag", bmsException.getErrCode());
        errorResultMap.put("rtnMsg", bmsException.getErrMsg());
        return errorResultMap;
    }

    @ResponseBody
    @ExceptionHandler(AuthorizationException.class)
    public Map<String, Object> handleShiroException(AuthorizationException exception) {
        Map<String, Object> errorResultMap = new HashMap<>(2);
        errorResultMap.put("rtnFlag", "shiro");
        errorResultMap.put("rtnMsg", exception.getMessage());
        return errorResultMap;
    }
}