package com.bms.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bms.project.common.AccountUtil;
import com.bms.project.common.model.LoginFormVo;
import com.bms.project.generator.entity.BmsAccountRoleRelationMp;
import com.bms.project.generator.entity.BmsResourceMp;
import com.bms.project.generator.entity.BmsRoleMp;
import com.bms.project.generator.entity.BmsRoleResourceRelationMp;
import com.bms.project.generator.mapper.BmsAccountRoleRelationMpMapper;
import com.bms.project.generator.mapper.BmsResourceMpMapper;
import com.bms.project.generator.mapper.BmsRoleMpMapper;
import com.bms.project.generator.mapper.BmsRoleResourceRelationMpMapper;
import com.bms.project.model.ResponseData;
import com.bms.project.service.IndexService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author liqiang
 * @date 2019/10/26 23:20
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class IndexServiceImpl implements IndexService {


    @Autowired
    private BmsAccountRoleRelationMpMapper bmsAccountRoleRelationMpMapper;

    @Autowired
    private BmsRoleMpMapper bmsRoleMpMapper;

    @Autowired
    private BmsResourceMpMapper bmsResourceMpMapper;

    @Autowired
    private BmsRoleResourceRelationMpMapper bmsRoleResourceRelationMpMapper;

    @Override
    public ResponseData login(LoginFormVo loginFormVo) {
        return AccountUtil.login(loginFormVo);
    }

    @Override
    public ResponseData loadMenus() {
        // 当前登录人id
        String currentAccountId = AccountUtil.getCurrentAccountId();
        List<BmsAccountRoleRelationMp> linkRoles = bmsAccountRoleRelationMpMapper.selectList(new QueryWrapper<BmsAccountRoleRelationMp>().eq("account_id", currentAccountId));
        // 获取当前人员所有的角色
        Set<String> roles = new HashSet<>();
        for (BmsAccountRoleRelationMp linkRole : linkRoles) {
            String roleId = linkRole.getRoleId();
            BmsRoleMp bmsRoleMp = bmsRoleMpMapper.selectById(roleId);
            Optional.ofNullable(bmsRoleMp).filter(role -> "enabled".equals(role.getStatus())).ifPresent(role -> roles.add(role.getId()));
        }
        if (CollectionUtils.isNotEmpty(roles)) {
            JSONArray array = new JSONArray();
            // 根据角色获取当前所有的授权菜单id
            List<BmsRoleResourceRelationMp> bmsRoleResourceRelationMps =
                    bmsRoleResourceRelationMpMapper.selectList(new QueryWrapper<BmsRoleResourceRelationMp>().in("role_id", roles));
            Set<String> resourceIds = bmsRoleResourceRelationMps.stream().map(BmsRoleResourceRelationMp::getResourceId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(resourceIds)) {
                // 获取所有绑定的菜单
                List<BmsResourceMp> resources = bmsResourceMpMapper.selectList(new QueryWrapper<BmsResourceMp>().in("id", resourceIds).eq("status", "enabled"));
                Set<String> parentNodeIds = new HashSet<>();
                for (BmsResourceMp resource : resources) {
                    parentNodeIds.add(getParentResource(resource));
                }
                Set<BmsResourceMp> parentNodes = new HashSet<>(bmsResourceMpMapper.selectBatchIds(parentNodeIds));
                JSONArray objects = JSON.parseArray(JSON.toJSONString(parentNodes));
                Map<String, List<BmsResourceMp>> collect = resources.stream().collect(Collectors.groupingBy(BmsResourceMp::getParentId));
                for (Object object : objects) {
                    JSONObject jsonObject = (JSONObject) object;
                    if ("0".equals(jsonObject.getString("parentId"))) {
                        array.add(AccountUtil.digui(jsonObject, collect));
                    }
                }
            }
            return ResponseData.success(array);
        }
        return ResponseData.success();
    }

    /**
     * 递归获取到最顶层菜单id集合,因为绑定菜单的时候不把顶层的算进去
     *
     * @param resource
     *
     * @return
     */
    public String getParentResource(BmsResourceMp resource) {
        if (!"0".equals(resource.getParentId())) {
            String id = resource.getParentId();
            BmsResourceMp bmsResourceMp = bmsResourceMpMapper.selectById(id);
            return getParentResource(bmsResourceMp);
        } else {
            return resource.getId();
        }
    }
}
