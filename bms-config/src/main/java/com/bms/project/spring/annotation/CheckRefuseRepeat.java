package com.bms.project.spring.annotation;

import com.alibaba.fastjson.JSON;
import com.bms.project.shiro.ShiroProperty;
import com.bms.project.spring.AbstractCustomInteceptor;
import com.bms.project.util.CM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liqiang
 * @date 2019/11/7 13:28
 */
@Component
public class CheckRefuseRepeat extends AbstractCustomInteceptor {

    @Autowired
    private ShiroProperty shiroProperty;

    private static final String REPEAT_KEY = "REPEAT_SUBMIT";
    private static final String SUBMIT_TIME = "SUBMIT_TIME";
    private static final String SUBMIT_BODY = "SUBMIT_BODY";

    /**
     * 本实现只针对 application/json 方式提交的数据进行核对
     *
     * @param httpServletRequest
     *
     * @return true代表重复提交
     */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean checkIfRepeatSubmit(HttpServletRequest httpServletRequest) throws IOException {
        String requestUrl = httpServletRequest.getRequestURL().toString();
        Map<String, Object> map = new HashMap<>(3);

        String contentType = httpServletRequest.getContentType();
        // 获取当前请求的时间戳和请求参数
        String str;
        StringBuilder wholeStr = new StringBuilder();
        if ("application/json;charset=UTF-8".equals(contentType)) {
            BufferedReader br = httpServletRequest.getReader();
            while ((str = br.readLine()) != null) {
                wholeStr.append(str);
            }
        } else {
            Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
            wholeStr = new StringBuilder(JSON.toJSONString(parameterMap));
        }

        map.put(SUBMIT_BODY, wholeStr.toString());
        long thisTime = System.currentTimeMillis();
        map.put(SUBMIT_TIME, thisTime);

        HttpSession session = httpServletRequest.getSession();
        Object attribute = session.getAttribute(REPEAT_KEY);
        // 如果不为空,说明已经是重复的请求了
        if (attribute != null) {
            // 拿到上一次保存的访问时间和访问数据
            Map<String, Object> attributeMap = (Map<String, Object>) attribute;
            Map<String, Object> lastData = (Map<String, Object>) attributeMap.get(requestUrl);
            if (lastData != null) {
                String body = CM.toString(lastData.get(SUBMIT_BODY));
                long time = CM.toLong(lastData.get(SUBMIT_TIME));
                if (wholeStr.toString().equals(body) && (thisTime - time) <= shiroProperty.getRepeatTime() * 1000) {
                    return true;
                }
            }
        }
        Map<String, Object> sessionMap = new HashMap<>(1);
        sessionMap.put(requestUrl, map);
        session.setAttribute(REPEAT_KEY, sessionMap);
        return false;
    }
}
