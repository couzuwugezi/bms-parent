package com.bms.project.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bms.project.common.model.LoginFormVo;
import com.bms.project.generator.entity.BmsAccountMp;
import com.bms.project.generator.entity.BmsResourceMp;
import com.bms.project.model.ResponseData;
import com.bms.project.shiro.CaptchaToken;
import com.bms.project.shiro.CustomCredentialsMatcher;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liqiang
 * @date 2019/10/26 23:29
 */
@Component
public class AccountUtil {

    public static final String UNLOGIN = "1001";

    public static final String UNMODIFIED_PASSWORD = "1002";

    public static final String UNAUTHORIZED = "1003";

    /**
     * 登陆次数
     */
    private static Integer times;

    @Value("${shiro.times:5}")
    public void setTimes(Integer times) {
        AccountUtil.times = times;
    }

    private static Integer outTime;

    @Value("${shiro.outTime:30}")
    public void setOutTime(Integer outTime) {
        AccountUtil.outTime = outTime;
    }

    public static BmsAccountMp getCurrentAccount() {
        Object object = SecurityUtils.getSubject().getPrincipal();
        return (BmsAccountMp) object;
    }

    public static String getCurrentAccountId() {
        return getCurrentAccount().getId();
    }

    public static JSONObject digui(JSONObject obj, Map<String, List<BmsResourceMp>> collect) {
        List<BmsResourceMp> child = collect.get(obj.getString("id"));
        if (CollectionUtils.isNotEmpty(child)) {
            JSONArray objects = JSON.parseArray(JSON.toJSONString(child));
            obj.put("children", objects);
            for (Object resource : objects) {
                JSONObject obj1 = (JSONObject) resource;
                digui(obj1, collect);
            }
        }
        return obj;
    }

    public static ResponseData login(LoginFormVo loginFormVo) {
        UsernamePasswordToken token = new UsernamePasswordToken(loginFormVo.getUsername(), loginFormVo.getPassword());
        Subject subject = SecurityUtils.getSubject();
        try {
            CaptchaToken captchaToken = new CaptchaToken(token, loginFormVo.getCaptchaCode());
            subject.login(captchaToken);
            BmsAccountMp account = getCurrentAccount();
            if (account.getPassword().equals(account.getAdminSetPassword())) {
                return new ResponseData(AccountUtil.UNMODIFIED_PASSWORD, "请修改掉管理员给您设置的密码");
            }
            subject.getSession().setTimeout(outTime * 60 * 1000);
            return ResponseData.success();
        } catch (ExcessiveAttemptsException | LockedAccountException | UnknownAccountException e) {
            return ResponseData.error(e.getMessage());
        } catch (IncorrectCredentialsException e) {
            Map<String, AtomicInteger> controlMap = CustomCredentialsMatcher.CONTROL_MAP;
            AtomicInteger atomicInteger = controlMap.get(loginFormVo.getUsername());
            return ResponseData.error("密码错误,还剩" + (times - (atomicInteger.get())) + "次机会");
        }

    }


}
