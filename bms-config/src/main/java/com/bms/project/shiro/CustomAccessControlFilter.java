package com.bms.project.shiro;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author liqiang
 * @date 2019/10/26 22:35
 * 参考了 https://blog.51cto.com/wyait/2107423
 * shiro登录拦截 针对/**
 */
@Slf4j
public class CustomAccessControlFilter extends AccessControlFilter {

    /**
     * 表示是否允许访问，如果允许访问返回true，否则false；
     *
     * @param request
     * @param response
     * @param mappedValue
     *
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 是不是想去登录地址
        if (isLoginRequest(request, response)) {
            return true;
        } else {
            // 判断当前是否已经登陆
            Subject subject = getSubject(request, response);
            // 没有登录授权 且没有记住我
            return subject.isAuthenticated() || subject.isRemembered();
        }

    }

    /**
     * 表示当访问拒绝时执行
     *
     * @param servletRequest
     * @param servletResponse
     *
     * @return
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.setHeader("content-type", "application/json;charset=UTF-8");
        JSONObject res = new JSONObject();
        res.put("rtnFlag", "1001");
        res.put("errMsg", "未登录");
        httpServletResponse.getWriter().write(JSON.toJSONString(res));
        return false;
    }
}
