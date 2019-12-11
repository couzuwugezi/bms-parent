package com.bms.project.controller;

import com.bms.project.common.AccountUtil;
import com.bms.project.common.model.AccountQueryVo;
import com.bms.project.generator.entity.BmsAccountMp;
import com.bms.project.generator.entity.BmsAccountRoleRelationMp;
import com.bms.project.generator.service.IBmsAccountMpService;
import com.bms.project.generator.service.IBmsAccountRoleRelationMpService;
import com.bms.project.model.ResponseData;
import com.bms.project.model.ResponsePage;
import com.bms.project.shiro.ShiroProperty;
import com.bms.project.spring.annotation.RefuseRepeat;
import com.bms.project.util.TinyUUIDGenerator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author liqiang
 * @date 2019/11/3 21:22
 */
@RestController
@RequestMapping("permission/account")
public class AccountController {

    @Autowired
    private ShiroProperty shiroProperty;

    @Autowired
    private IBmsAccountMpService iBmsAccountMpService;

    @Autowired
    private IBmsAccountRoleRelationMpService iBmsAccountRoleRelationMpService;

    /**
     * 查询数据
     *
     * @param accountQueryVo
     *
     * @return
     */
    @PostMapping("loadAccounts")
    @RequiresPermissions({"read"})
    public ResponsePage loadAccounts(@RequestBody AccountQueryVo accountQueryVo) {
        String name = accountQueryVo.getName();
        String loginName = accountQueryVo.getLoginName();
        String mobilePhone = accountQueryVo.getMobilePhone();
        String email = accountQueryVo.getEmail();
        String status = accountQueryVo.getStatus();

        QueryWrapper<BmsAccountMp> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.eq("name", name.trim());
        }
        if (StringUtils.isNotBlank(loginName)) {
            queryWrapper.eq("login_name", loginName.trim());
        }
        if (StringUtils.isNotBlank(mobilePhone)) {
            queryWrapper.like("mobile_phone", mobilePhone.trim());
        }
        if (StringUtils.isNotBlank(email)) {
            queryWrapper.like("email", email.trim());
        }
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq("status", status.trim());
        }

        Page<BmsAccountMp> bmsAccountMpPage = new Page<>(accountQueryVo.getCurrentPage(), accountQueryVo.getPageSize());
        IPage<BmsAccountMp> page = iBmsAccountMpService.page(bmsAccountMpPage, queryWrapper);

        return ResponsePage.success(page.getTotal(), page.getRecords());

    }

    /**
     * 修改密码
     *
     * @param pwdMap
     *
     * @return
     */
    @PostMapping("updatePwd")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"update"})
    @RefuseRepeat
    public ResponseData updatePwd(@RequestBody Map<String, String> pwdMap) {
        String password = pwdMap.get("password");
        String verifyPwd = verifyPwd(password);
        if (verifyPwd != null) {
            return ResponseData.error(verifyPwd);
        }
        String loginName = pwdMap.get("loginName");
        BmsAccountMp accountMp = iBmsAccountMpService.getOne(new QueryWrapper<BmsAccountMp>().eq("login_name", loginName));
        String encrypt = new SimpleHash(shiroProperty.getHashAlgorithmName(), password, accountMp.getSalt(), shiroProperty.getHashIterations()).toHex();
        accountMp.setModifyPasswordTime(LocalDateTime.now()).setPassword(encrypt).setModifyId(AccountUtil.getCurrentAccountId()).setModifyTime(LocalDateTime.now());

        return iBmsAccountMpService.updateById(accountMp)
                ? ResponseData.success() : ResponseData.error();
    }

    @PostMapping("updatePwd1")
    @Transactional(rollbackFor = Exception.class)
    @RequiresAuthentication
    @RefuseRepeat
    public ResponseData updatePwd1(@RequestBody Map<String, String> pwdMap) {
        String oldPassword = pwdMap.get("oldPassword");

        String accountId = AccountUtil.getCurrentAccountId();
        BmsAccountMp accountMp = iBmsAccountMpService.getById(accountId);
        String oldEncrypt = new SimpleHash(shiroProperty.getHashAlgorithmName(), oldPassword, accountMp.getSalt(), shiroProperty.getHashIterations()).toHex();
        if (!oldEncrypt.equals(accountMp.getPassword())) {
            return ResponseData.error("原始密码错误!");
        }
        String password = pwdMap.get("password");
        pwdMap.clear();
        pwdMap.put("password", password);
        pwdMap.put("loginName", accountMp.getLoginName());

        return updatePwd(pwdMap);
    }

    /**
     * 重置密码
     *
     * @param pwdMap id
     *
     * @return
     */
    @PostMapping("resetPwd")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"update"})
    @RefuseRepeat
    public ResponseData resetPwd(@RequestBody Map<String, String> pwdMap) {
        String id = pwdMap.get("id");
        String salt = UUID.randomUUID().toString();
        String encrypt = new SimpleHash(shiroProperty.getHashAlgorithmName(), shiroProperty.getDefaultPassword(), salt, shiroProperty.getHashIterations()).toHex();
        return iBmsAccountMpService.updateById(new BmsAccountMp().setModifyPasswordTime(LocalDateTime.now())
                .setAdminSetPassword(encrypt).setPassword(encrypt).setId(id).setSalt(salt))
                ? ResponseData.success() : ResponseData.error();
    }

    /**
     * 修改账号信息
     *
     * @param bmsAccountMp
     *
     * @return
     */
    @PostMapping("editAccount")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"update"})
    @RefuseRepeat
    public ResponseData editAccount(@RequestBody BmsAccountMp bmsAccountMp) {
        try {
            return iBmsAccountMpService.updateById(bmsAccountMp)
                    ? ResponseData.success() : ResponseData.error();
        } catch (Exception e) {
            if (e.getCause() instanceof MySQLIntegrityConstraintViolationException) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseData.error("当前[账号]已存在");
            }
            throw e;
        }
    }

    /**
     * 修改生效时效
     *
     * @param map ids 批量数据id
     *            status enabled生效 disable失效
     *
     * @return
     */
    @PostMapping("updateStatus")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"update"})
    @RefuseRepeat
    public ResponseData updateStatus(@RequestBody Map<String, String> map) {
        String[] ids = map.get("ids").split(",");
        List<BmsAccountMp> bmsAccountMps = iBmsAccountMpService.list(new QueryWrapper<BmsAccountMp>().in("id", Arrays.asList(ids)));
        bmsAccountMps.forEach(mp -> mp.setStatus(map.get("status")));
        return iBmsAccountMpService.updateBatchById(bmsAccountMps)
                ? ResponseData.success() : ResponseData.error();
    }

    /**
     * 新增账号信息
     *
     * @param bmsAccountMp
     *
     * @return
     */
    @PostMapping("addAccount")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"add"})
    @RefuseRepeat
    public ResponseData saveAccount(@RequestBody BmsAccountMp bmsAccountMp) {
        String id = TinyUUIDGenerator.generate();
        String salt = UUID.randomUUID().toString();
        String defaultPassword = shiroProperty.getDefaultPassword();
        String encrypt = new SimpleHash(shiroProperty.getHashAlgorithmName(), defaultPassword, salt, shiroProperty.getHashIterations()).toHex();

        bmsAccountMp.setPassword(encrypt);
        bmsAccountMp.setAdminSetPassword(encrypt);
        bmsAccountMp.setCreateId(AccountUtil.getCurrentAccountId());
        bmsAccountMp.setSalt(salt);
        bmsAccountMp.setId(id);

        try {
            return iBmsAccountMpService.save(bmsAccountMp) ? ResponseData.success() : ResponseData.error();
        } catch (Exception e) {
            if (e.getCause() instanceof MySQLIntegrityConstraintViolationException) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseData.error("当前[账号]已存在");
            }
            throw e;
        }
    }

    @PostMapping("loadLinkRolesByAccId")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"update"})
    @RefuseRepeat
    public ResponseData loadLinkRolesByAccId(@RequestBody Map<String, String> map) {
        String accountId = map.get("accountId");
        List<BmsAccountRoleRelationMp> links = iBmsAccountRoleRelationMpService
                .list(new QueryWrapper<BmsAccountRoleRelationMp>()
                        .eq("account_id", accountId));
        return ResponseData.success(links);
    }

    @PostMapping("bindRoles")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"update"})
    @RefuseRepeat
    public ResponseData bindRoles(@RequestBody Map<String, String> map) {
        String accountId = map.get("accountId");
        String roleIds = map.get("roleIds");
        iBmsAccountRoleRelationMpService
                .remove(new QueryWrapper<BmsAccountRoleRelationMp>()
                        .eq("account_id", accountId));

        if (StringUtils.isNotBlank(roleIds)) {
            String[] roleIdArr = roleIds.split(",");
            BmsAccountRoleRelationMp bmsAccountRoleRelationMp = new BmsAccountRoleRelationMp();
            bmsAccountRoleRelationMp.setAccountId(accountId);
            for (String id : roleIdArr) {
                bmsAccountRoleRelationMp.setId(TinyUUIDGenerator.generate());
                bmsAccountRoleRelationMp.setRoleId(id);
                iBmsAccountRoleRelationMpService.save(bmsAccountRoleRelationMp);
            }
        }
        return ResponseData.success();
    }


    private String verifyPwd(String password) {
        int maxLength = shiroProperty.getMaxLength();
        int minLength = shiroProperty.getMinLength();
        boolean mustContainDigit = shiroProperty.isMustContainDigit();
        boolean mustContainLowerCase = shiroProperty.isMustContainLowerCase();
        boolean mustContainUpperCase = shiroProperty.isMustContainUpperCase();
        if (password.length() > maxLength) {
            return "密码长度不得大于" + maxLength + "位";
        }
        if (password.length() < minLength) {
            return "密码长度不得小于" + minLength + "位";
        }
        if (mustContainDigit) {
            String num = ".*[0-9].*";
            if (!password.matches(num)) {
                return "必须包含有数字";
            }
        }
        if (mustContainLowerCase) {
            String lowerCase = ".*[a-z].*";
            if (!password.matches(lowerCase)) {
                return "必须包含有小写字母";
            }
        }
        if (mustContainUpperCase) {
            String upperCase = ".*[A-Z].*";
            if (!password.matches(upperCase)) {
                return "必须包含有大写字母";
            }
        }
        return null;
    }
}
