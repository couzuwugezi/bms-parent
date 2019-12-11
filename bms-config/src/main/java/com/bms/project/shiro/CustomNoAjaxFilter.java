package com.bms.project.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author liqiang
 * @date 2019/11/10 22:35
 * shiro非ajax请求拦截 针对/*.html
 */
@Slf4j
public class CustomNoAjaxFilter extends AccessControlFilter {

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
        HttpServletResponse httpServletResponse = (HttpServletResponse)servletResponse;
//        httpServletResponse.sendError(1002,"登录失效");
        saveRequestAndRedirectToLogin(servletRequest, servletResponse);
        return false;
    }
}
