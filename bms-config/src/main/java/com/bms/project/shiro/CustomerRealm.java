package com.bms.project.shiro;

import com.bms.project.generator.entity.BmsAccountMp;
import com.bms.project.generator.entity.BmsAccountRoleRelationMp;
import com.bms.project.generator.entity.BmsRoleAuthRelationMp;
import com.bms.project.generator.entity.BmsRoleMp;
import com.bms.project.generator.mapper.BmsAccountMpMapper;
import com.bms.project.generator.mapper.BmsAccountRoleRelationMpMapper;
import com.bms.project.generator.mapper.BmsRoleAuthRelationMpMapper;
import com.bms.project.generator.mapper.BmsRoleMpMapper;
import com.bms.project.model.Constant;
import com.bms.project.model.ResponseCode;
import com.bms.project.spring.BmsException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author liqiang
 * @date 2019/10/26 20:56
 */
@Component
public class CustomerRealm extends AuthorizingRealm {

    @Autowired
    private ShiroProperty shiroProperty;

    @Autowired
    private BmsAccountMpMapper bmsAccountMpMapper;

    @Autowired
    private BmsAccountRoleRelationMpMapper bmsAccountRoleRelationMpMapper;

    @Autowired
    private BmsRoleMpMapper bmsRoleMpMapper;

    @Autowired
    private BmsRoleAuthRelationMpMapper bmsRoleAuthRelationMpMapper;

    /**
     * 角色权限和对应权限添加
     *
     * @param principalCollection
     *
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        BmsAccountMp account = (BmsAccountMp) principalCollection.getPrimaryPrincipal();
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        String accountId = account.getId();

        List<BmsAccountRoleRelationMp> linkRoles = bmsAccountRoleRelationMpMapper.selectList(new QueryWrapper<BmsAccountRoleRelationMp>().eq("account_id", accountId));
        Set<String> roles = new HashSet<>();
        Set<String> roleIds = new HashSet<>();
        for (BmsAccountRoleRelationMp linkRole : linkRoles) {
            String roleId = linkRole.getRoleId();
            BmsRoleMp bmsRoleMp = bmsRoleMpMapper.selectById(roleId);
            Optional.ofNullable(bmsRoleMp).filter(role -> "enabled".equals(role.getStatus())).ifPresent(role -> roles.add(role.getName()));
            Optional.ofNullable(bmsRoleMp).filter(role -> "enabled".equals(role.getStatus())).ifPresent(role -> roleIds.add(role.getId()));
        }
        if (CollectionUtils.isNotEmpty(roles)) {
            authorizationInfo.addRoles(roles);
        }

        if (CollectionUtils.isNotEmpty(roles)) {
            List<BmsRoleAuthRelationMp> roleAuthRelationMps = bmsRoleAuthRelationMpMapper.selectList(new QueryWrapper<BmsRoleAuthRelationMp>().in("role_id", roleIds));
            if (CollectionUtils.isNotEmpty(roleAuthRelationMps)) {
                Set<String> collect = roleAuthRelationMps.stream().map(BmsRoleAuthRelationMp::getDictValue).collect(Collectors.toSet());
                authorizationInfo.addStringPermissions(collect);
            }
        }

        return authorizationInfo;
    }

    /**
     * 验证登录
     *
     * @param authenticationToken
     *
     * @return
     *
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
        CaptchaToken token = (CaptchaToken) authenticationToken;

        if (shiroProperty.isIfNeedCaptcha()) {
            // 获取用户在登录页面输入的验证码
            String loginCaptcha = token.getCaptchaCode();

            //如果验证码为空,直接返回错误
            if (StringUtils.isBlank(loginCaptcha)) {
                throw new BmsException(ResponseCode.ERROR_CAPTCHA);
            }

            // 验证码不正确也直接返回错误
            String sessionCaptcha = (String) SecurityUtils.getSubject().getSession().getAttribute(Constant.KAPTCHA_SESSION_KEY);
            if (!loginCaptcha.equalsIgnoreCase(sessionCaptcha)) {
                throw new BmsException(ResponseCode.ERROR_CAPTCHA);
            }
        }

        String username = token.getUsername();

        BmsAccountMp account = bmsAccountMpMapper.selectOne(new QueryWrapper<BmsAccountMp>().eq("login_name", username));

        String status = Optional.ofNullable(account).orElseThrow(() -> new UnknownAccountException("账号不存在，请联系管理员")).getStatus();
        if ("disable".equals(status)) {
            throw new LockedAccountException("账号已经被锁定,请联系管理员!");
        }

        return new SimpleAuthenticationInfo(account, account.getPassword(), ByteSource.Util.bytes(account.getSalt()), getName());
    }
}
