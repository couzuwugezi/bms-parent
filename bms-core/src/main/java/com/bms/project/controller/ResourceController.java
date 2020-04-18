package com.bms.project.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bms.project.common.AccountUtil;
import com.bms.project.generator.entity.BmsAccountMp;
import com.bms.project.generator.entity.BmsResourceMp;
import com.bms.project.generator.entity.BmsRoleResourceRelationMp;
import com.bms.project.generator.service.IBmsResourceMpService;
import com.bms.project.generator.service.IBmsRoleResourceRelationMpService;
import com.bms.project.model.ResponseData;
import com.bms.project.spring.annotation.RefuseRepeat;
import com.bms.project.util.CM;
import com.bms.project.util.TinyUUIDGenerator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 管理系统-资源表 前端控制器
 * </p>
 * @author liqiang
 */
@RestController
@RequestMapping("/permission/resource")
public class ResourceController {

    @Autowired
    private IBmsResourceMpService service;

    @Autowired
    private IBmsRoleResourceRelationMpService iBmsRoleResourceRelationMpService;

    /**
     * 加载所有菜单
     *
     * @param jsonObject
     *
     * @return
     */
    @PostMapping("/loadRes")
    @RequiresPermissions({"read"})
    public ResponseData loadRes(@RequestBody JSONObject jsonObject) {
        String name = jsonObject.getString("name");
        String type = jsonObject.getString("type");
        String status = jsonObject.getString("status");
        QueryWrapper<BmsResourceMp> resourceQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            resourceQueryWrapper.like("name", name);
        }
        if (StringUtils.isNotBlank(type)) {
            if ("menu".equals(type)) {
                resourceQueryWrapper.eq("parent_id", 0);
            } else {
                resourceQueryWrapper.ne("parent_id", 0);
            }
            resourceQueryWrapper.eq("type", type);
        } else {
            resourceQueryWrapper.eq("parent_id", 0);
        }
        if (StringUtils.isNotBlank(status)) {
            resourceQueryWrapper.eq("status", status);
        }
        resourceQueryWrapper.orderByAsc("sort");
        List<BmsResourceMp> res = service.list(resourceQueryWrapper);
        JSONArray objects = JSON.parseArray(JSON.toJSONString(res));
        JSONArray array = new JSONArray();

        List<BmsResourceMp> children = service.list(new QueryWrapper<BmsResourceMp>().ne("parent_id", "0"));
        Map<String, List<BmsResourceMp>> collect = children.stream().collect(Collectors.groupingBy(BmsResourceMp::getParentId));
        for (Object object : objects) {
            array.add(AccountUtil.digui((JSONObject) object, collect));
        }
        return ResponseData.success(array);
    }

    /**
     * 只加载所有的启用菜单
     *
     * @return
     */
    @PostMapping("/loadEnabledResource")
    @RequiresPermissions({"read"})
    public ResponseData loadEnabledResource() {
        List<BmsResourceMp> res = service.list(new QueryWrapper<BmsResourceMp>().eq("status", "enabled").eq("parent_id", 0));
        JSONArray objects = JSON.parseArray(JSON.toJSONString(res));
        JSONArray array = new JSONArray();

        List<BmsResourceMp> children = service.list(new QueryWrapper<BmsResourceMp>().ne("parent_id", "0").eq("status", "enabled"));
        Map<String, List<BmsResourceMp>> collect = children.stream().collect(Collectors.groupingBy(BmsResourceMp::getParentId));
        for (Object object : objects) {
            array.add(AccountUtil.digui((JSONObject) object, collect));
        }
        return ResponseData.success(array);
    }

    @RequestMapping("/add")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"add"})
    @RefuseRepeat
    public ResponseData add(@RequestBody BmsResourceMp entity) {
        if (StringUtils.isBlank(entity.getName())) {
            return ResponseData.error("资源名称不能为空");
        }
        if (StringUtils.isBlank(entity.getType())) {
            return ResponseData.error("资源类型不能为空");
        }
        if (StringUtils.isBlank(entity.getStatus())) {
            //默认启用
            entity.setStatus("enabled");
        }
        BmsAccountMp currentAccount = AccountUtil.getCurrentAccount();
        LocalDateTime now = LocalDateTime.now();
        entity.setId(TinyUUIDGenerator.generate());
        entity.setCreateTime(now);
        entity.setModifyTime(now);
        entity.setCreateId(currentAccount.getId());
        entity.setModifyId(currentAccount.getId());
        try {
            service.save(entity);
        } catch (Exception e) {
            if (e.getCause() instanceof MySQLIntegrityConstraintViolationException) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseData.error("当前资源名称已存在");
            }
            throw e;
        }
        //获取该资源的父资源在角色资源关系表中选中的数据
        QueryWrapper<BmsRoleResourceRelationMp> queryWrapper = Wrappers.query();
        queryWrapper.eq("resource_id", entity.getParentId());
        List<BmsRoleResourceRelationMp> roleResourceRelationList = iBmsRoleResourceRelationMpService.list(queryWrapper);
        //获取选中资源的角色id，与插入的新资源建立关系
        for (BmsRoleResourceRelationMp po : roleResourceRelationList) {
            BmsRoleResourceRelationMp newPo = new BmsRoleResourceRelationMp();
            newPo.setId(TinyUUIDGenerator.generate());
            newPo.setRoleId(po.getRoleId());
            newPo.setResourceId(entity.getId());
            iBmsRoleResourceRelationMpService.save(newPo);
        }
        return ResponseData.success();
    }

    /**
     * 修改资源
     *
     * @return
     */
    @RequestMapping("/editRes")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"update"})
    @RefuseRepeat
    public ResponseData editRes(@RequestBody BmsResourceMp entity) {
        String id = entity.getId();
        String name = entity.getName();
        if (id == null) {
            return ResponseData.error("主键ID不能为空");
        }
        if (StringUtils.isBlank(name)) {
            return ResponseData.error("资源名称不能为空");
        }
        if (StringUtils.isBlank(CM.toString(entity.getType()))) {
            return ResponseData.error("资源类型不能为空");
        }
        BmsResourceMp resource = service.getById(id);
        if (resource != null) {
            if (!resource.getName().equals(name)) {
                int checkName = service.count(new QueryWrapper<BmsResourceMp>().eq("name", name).ne("id", id));
                if (checkName != 0) {
                    return ResponseData.error("资源名称重复");
                }
            }
            resource.setModifyId(AccountUtil.getCurrentAccountId());
            resource.setModifyTime(LocalDateTime.now());
            resource.setStatus(entity.getStatus());
            resource.setSort(entity.getSort());
            resource.setName(name);
            resource.setUrl(entity.getUrl());
            resource.setType(entity.getType());
            service.updateById(resource);
        }
        return ResponseData.success();
    }

    @PostMapping("/changeResStatus/{id}/{status}")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"update"})
    @RefuseRepeat
    public ResponseData changeResStatus(@PathVariable("id") String id, @PathVariable("status") String status) {

        // 先更新当前选中的菜单
        BmsResourceMp resourceMp = service.getById(id);
        resourceMp.setStatus(status);
        resourceMp.setModifyId(AccountUtil.getCurrentAccountId());
        resourceMp.setModifyTime(LocalDateTime.now());
        service.updateById(resourceMp);
        // 再递归更新所有的子菜单
        List<BmsResourceMp> children = service.list(new QueryWrapper<BmsResourceMp>().eq("parent_id", id));
        diguiResource(children, status);

        return ResponseData.success();
    }

    private void diguiResource(List<BmsResourceMp> children, String status) {
        for (BmsResourceMp child : children) {
            child.setStatus(status);
            child.setModifyId(AccountUtil.getCurrentAccountId());
            child.setModifyTime(LocalDateTime.now());
            service.updateById(child);
            List<BmsResourceMp> children2 = service.list(new QueryWrapper<BmsResourceMp>().eq("parent_id", child.getId()));
            diguiResource(children2, status);
        }
    }

}
