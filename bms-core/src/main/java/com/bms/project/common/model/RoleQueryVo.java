package com.bms.project.common.model;

import com.bms.project.common.PaginationModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author liqiang
 * @date 2019/11/3 21:33
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoleQueryVo extends PaginationModel {
    /**
     * 姓名
     */
    private String name;

    /**
     * 角色描述
     */
    private String status;

}
