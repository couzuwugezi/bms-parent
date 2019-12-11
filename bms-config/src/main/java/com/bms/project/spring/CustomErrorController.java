package com.bms.project.spring;

import com.bms.project.model.ResponseData;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author liqiang
 */
@Controller
public class CustomErrorController implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @ResponseBody
    @RequestMapping(value = ERROR_PATH)
    public ResponseData handleError(HttpServletRequest request, HttpServletResponse response) {
        // TODO 全局异常的记录
        String rtnMsg = "";
        if (response.getStatus() == 500) {
            rtnMsg = "内部异常,接口请求失败";
        } else if (response.getStatus() == 404) {
            rtnMsg = "接口请求失败,访问的资源不存在";
        } else {
            rtnMsg = "触发了未知异常,请联系管理员!";
        }
        return new ResponseData(String.valueOf(response.getStatus()), rtnMsg);
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

}
