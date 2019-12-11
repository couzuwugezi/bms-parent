package com.bms.project.controller;

import com.bms.project.common.AccountUtil;
import com.bms.project.common.model.RoleQueryVo;
import com.bms.project.generator.entity.BmsDictMp;
import com.bms.project.generator.entity.BmsRoleAuthRelationMp;
import com.bms.project.generator.entity.BmsRoleMp;
import com.bms.project.generator.entity.BmsRoleResourceRelationMp;
import com.bms.project.generator.service.IBmsDictMpService;
import com.bms.project.generator.service.IBmsRoleAuthRelationMpService;
import com.bms.project.generator.service.IBmsRoleMpService;
import com.bms.project.generator.service.IBmsRoleResourceRelationMpService;
import com.bms.project.model.ResponseData;
import com.bms.project.spring.annotation.RefuseRepeat;
import com.bms.project.util.TinyUUIDGenerator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liqiang
 * @date 2019/11/3 21:22
 */
@RestController
@RequestMapping("permission/role")
public class RoleController {

    @Autowired
    private IBmsRoleMpService iBmsRoleMpService;

    @Autowired
    private IBmsRoleResourceRelationMpService iBmsRoleResourceRelationMpService;

    @Autowired
    private IBmsRoleAuthRelationMpService iBmsRoleAuthRelationMpService;

    @Autowired
    private IBmsDictMpService iBmsDictMpService;

    /**
     * 查询数据
     *
     * @param roleQueryVo
     *
     * @return
     */
    @PostMapping("loadRoles")
    @RequiresPermissions({"read"})
    public ResponseData loadAccounts(@RequestBody RoleQueryVo roleQueryVo) {
        String name = roleQueryVo.getName();
        String status = roleQueryVo.getStatus();

        QueryWrapper<BmsRoleMp> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name.trim());
        }
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq("status", status);
        }
        return ResponseData.success(iBmsRoleMpService.list(queryWrapper));

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
        String id = map.get("id");
        String status = map.get("status");
        BmsRoleMp mp = iBmsRoleMpService.getById(id);
        mp.setStatus(status);
        return iBmsRoleMpService.updateById(mp)
                ? ResponseData.success() : ResponseData.error();
    }

    /**
     * 新增编辑角色信息
     *
     * @param bmsRoleMp
     *
     * @return
     */
    @PostMapping("add")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"add"})
    @RefuseRepeat
    public ResponseData saveOrUpdateRole(@RequestBody BmsRoleMp bmsRoleMp) {
        String id = bmsRoleMp.getId();
        try {
            if (StringUtils.isNotBlank(id)) {
                return iBmsRoleMpService.updateById(bmsRoleMp) ? ResponseData.success() : ResponseData.error();
            }
            bmsRoleMp.setCreateId(AccountUtil.getCurrentAccountId());
            bmsRoleMp.setCreateTime(LocalDateTime.now());
            bmsRoleMp.setId(TinyUUIDGenerator.generate());
            iBmsRoleMpService.save(bmsRoleMp);
        } catch (Exception e) {
            if (e.getCause() instanceof MySQLIntegrityConstraintViolationException) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseData.error("当前[角色名称]已存在");
            }
            throw e;
        }
        return ResponseData.success();
    }

    /**
     * 绑定菜单
     *
     * @param map id 角色id
     *            keys 选择的菜单
     *
     * @return
     */
    @PostMapping("bindRes")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"update"})
    @RefuseRepeat
    public ResponseData bindRes(@RequestBody Map<String, String> map) {
        String id = map.get("id");
        String keys = map.get("keys");
        iBmsRoleResourceRelationMpService.remove(new QueryWrapper<BmsRoleResourceRelationMp>().eq("role_id", id));
        if (StringUtils.isNotBlank(keys)) {
            String[] resourceIds = keys.split(",");
            BmsRoleResourceRelationMp relationMp = new BmsRoleResourceRelationMp();
            for (String resourceId : resourceIds) {
                relationMp.setId(TinyUUIDGenerator.generate());
                relationMp.setRoleId(id);
                relationMp.setResourceId(resourceId);
                iBmsRoleResourceRelationMpService.save(relationMp);
            }
        }
        return ResponseData.success();
    }

    /**
     * 根据角色加载对应绑定菜单
     *
     * @param map
     *
     * @return
     */
    @PostMapping("loadResByRoleId")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"read"})
    public ResponseData loadResByRoleId(@RequestBody Map<String, String> map) {
        String roleId = map.get("roleId");
        List<BmsRoleResourceRelationMp> rolesLinkRes = iBmsRoleResourceRelationMpService.list(new QueryWrapper<BmsRoleResourceRelationMp>().eq("role_id", roleId));
        if (CollectionUtils.isNotEmpty(rolesLinkRes)) {
            Set<String> collect = rolesLinkRes.stream().map(BmsRoleResourceRelationMp::getResourceId).collect(Collectors.toSet());
            return ResponseData.success(collect);
        }
        return ResponseData.success();
    }

    /**
     * 给角色授权
     *
     * @param roleId
     * @param map    dictIds 授权id数组
     *
     * @return
     */
    @PostMapping("/auth/{roleId}")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"update"})
    @RefuseRepeat
    public ResponseData auth(@PathVariable("roleId") String roleId, @RequestBody Map<String, String> map) {
        String auths = map.get("auths");
        iBmsRoleAuthRelationMpService.remove(new QueryWrapper<BmsRoleAuthRelationMp>().eq("role_id", roleId));

        Map<String, List<BmsDictMp>> collectById = Optional.ofNullable(iBmsDictMpService.list()).orElseGet(ArrayList::new).stream().collect(Collectors.groupingBy(BmsDictMp::getValue));
        if (StringUtils.isNotBlank(auths)) {
            String[] split = auths.split(",");
            BmsRoleAuthRelationMp bmsRoleAuthRelationMp = new BmsRoleAuthRelationMp();
            bmsRoleAuthRelationMp.setRoleId(roleId);

            for (String dictValue : split) {
                bmsRoleAuthRelationMp.setId(TinyUUIDGenerator.generate());
                bmsRoleAuthRelationMp.setDictValue(dictValue);
                bmsRoleAuthRelationMp.setDictId(Optional.ofNullable(Optional.ofNullable(collectById.get(dictValue)).orElseGet(ArrayList::new).get(0)).orElseGet(BmsDictMp::new).getId());
                iBmsRoleAuthRelationMpService.save(bmsRoleAuthRelationMp);
            }
        }

        return ResponseData.success();
    }

}
