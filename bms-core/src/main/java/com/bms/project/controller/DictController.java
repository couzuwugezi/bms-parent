package com.bms.project.controller;

import com.bms.project.generator.entity.BmsDictMp;
import com.bms.project.generator.entity.BmsRoleAuthRelationMp;
import com.bms.project.generator.service.IBmsDictMpService;
import com.bms.project.generator.service.IBmsRoleAuthRelationMpService;
import com.bms.project.model.ResponseData;
import com.bms.project.spring.annotation.RefuseRepeat;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author liqiang
 * @date 2019/11/6 22:34
 */
@RestController
@RequestMapping("permission/dict")
public class DictController {

    @Autowired
    private IBmsDictMpService iBmsDictMpService;

    @Autowired
    private IBmsRoleAuthRelationMpService iBmsRoleAuthRelationMpService;

    @PostMapping("/loadDicts/{parentId}")
    @RequiresPermissions({"read"})
    public ResponseData loadDicts(@PathVariable("parentId") Integer parentId) {
        return ResponseData.success(iBmsDictMpService.list(new QueryWrapper<BmsDictMp>().eq("parent_id", parentId)));
    }

    /**
     * 根据角色加载关联的权限
     *
     * @param roleId
     *
     * @return
     */
    @PostMapping("/loadAuth/{roleId}")
    @RequiresPermissions({"read"})
    public ResponseData loadAuth(@PathVariable("roleId") String roleId) {
        return ResponseData.success(
                Optional.ofNullable(
                        iBmsRoleAuthRelationMpService.list(new QueryWrapper<BmsRoleAuthRelationMp>().eq("role_id", roleId))
                ).orElseGet(ArrayList::new)
                        .stream().map(BmsRoleAuthRelationMp::getDictValue).collect(Collectors.toSet())
        );
    }

    /**
     * 新增或修改字典表
     *
     * @param parentId
     * @param bmsDictMp
     *
     * @return
     */
    @PostMapping("/addDicts/{parentId}")
    @Transactional(rollbackFor = Exception.class)
    @RequiresPermissions({"add", "update"})
    @RefuseRepeat
    public ResponseData addDicts(@PathVariable("parentId") Integer parentId, @RequestBody BmsDictMp bmsDictMp) {
        bmsDictMp.setParentId(parentId);
        Integer id = bmsDictMp.getId();
        try {
            if (id != null) {
                iBmsDictMpService.updateById(bmsDictMp);
            } else {
                iBmsDictMpService.save(bmsDictMp);
            }
        } catch (Exception e) {
            if (e.getCause() instanceof MySQLIntegrityConstraintViolationException) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResponseData.error("当前[权限值]已存在");
            }
            throw e;
        }

        return ResponseData.success();
    }
}
