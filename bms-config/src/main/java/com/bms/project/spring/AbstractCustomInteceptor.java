package com.bms.project.spring;

import com.bms.project.generator.entity.BmsAccountMp;
import com.bms.project.model.ResponseCode;
import com.bms.project.shiro.ShiroProperty;
import com.bms.project.spring.annotation.RefuseRepeat;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * @author liqiang
 * @date 2019/11/3 22:31
 */
@Component
@Slf4j
public abstract class AbstractCustomInteceptor implements HandlerInterceptor {

    @Autowired
    private ShiroProperty shiroProperty;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) {
        // 校验是否需要修改密码
        int passwordEffectiveTime = shiroProperty.getPasswordEffectiveTime();
        LocalDateTime modifyPasswordTime = ((BmsAccountMp) SecurityUtils.getSubject().getPrincipal()).getModifyPasswordTime();
        long between = ChronoUnit.MONTHS.between(modifyPasswordTime.toLocalDate(), LocalDateTime.now().toLocalDate());
        if (httpServletRequest.getRequestURI().endsWith("/permission/account/uppdatePwd1") && passwordEffectiveTime < between) {
            throw new BmsException(ResponseCode.NEED_UPDATE_PWD);
        }

        // 校验是否重复提交
        if (o instanceof HandlerMethod) {
            HandlerMethod h = (HandlerMethod) o;
            RefuseRepeat annotation = h.getMethod().getAnnotation(RefuseRepeat.class);
            if (annotation != null) {
                try {
                    if (this.checkIfRepeatSubmit(httpServletRequest)) {
                        throw new BmsException(ResponseCode.SUBMIT_REPEAT);
                    }
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    /**
     * 校验当前请求是否是重复提交
     *
     * @param httpServletRequest
     *
     * @return
     *
     * @throws IOException
     */
    protected abstract boolean checkIfRepeatSubmit(HttpServletRequest httpServletRequest) throws IOException;
}