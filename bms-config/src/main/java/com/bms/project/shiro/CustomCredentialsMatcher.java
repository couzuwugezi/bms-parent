package com.bms.project.shiro;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liqiang
 * @date 2019/10/27 20:07
 */
@Component
public class CustomCredentialsMatcher extends HashedCredentialsMatcher {

    @Autowired
    private ShiroProperty shiroProperty;

    public static Map<String, AtomicInteger> CONTROL_MAP = new ConcurrentHashMap<>(16);
    private static Map<String, Long> OUT_TIME = new ConcurrentHashMap<>(16);

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        String loginName = (String) token.getPrincipal();
        int times = shiroProperty.getTimes();
        int time = shiroProperty.getTime();
        if (CONTROL_MAP.containsKey(loginName)) {
            AtomicInteger atomicInteger = CONTROL_MAP.get(loginName);
            // 如果登录次数超过了系统规定的次数
            if (atomicInteger.incrementAndGet() >= times) {
                // 配置的锁定时间(毫秒)
                Long lockTime = time * 60000L;
                // 已经锁定的时间
                Long spendTime = System.currentTimeMillis() - OUT_TIME.get(loginName);
                if (spendTime >= lockTime) {
                    // 如果锁定时间到期了,就把当前登录的限制移除
                    CONTROL_MAP.put(loginName, new AtomicInteger(1));
                } else {
                    // 如果登录次数超过了系统规定的次数,并且锁定时间没有到期
                    throw new ExcessiveAttemptsException("您已经登录失败了" + times + "次,请" + getGapTime(lockTime - spendTime) + "后再试");
                }
            } else {
                // 没超过限制,就每次都将当前毫秒数存进去
                OUT_TIME.put(loginName, System.currentTimeMillis());
            }
        } else {
            // 如果没有当前账号的信息,则记录次数为0
            CONTROL_MAP.put(loginName, new AtomicInteger(1));
        }
        boolean match = super.doCredentialsMatch(token, info);
        if (match) {
            // 验证通过,就把当前账户的登录次数清空
            OUT_TIME.remove(loginName);
            CONTROL_MAP.remove(loginName);
        }
        return match;
    }

    private String getGapTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String format = formatter.format(time);
        String[] split = format.split(":");
        return split[0] + "分" + split[1] + "秒";
    }

    public static void main(String[] args) {
        String s = new SimpleHash("MD5", "bms123", "a10bd1a2-066f-4652-ac19-89eb05f16bf5", 1000).toHex();
        System.out.println(s);
    }
}
