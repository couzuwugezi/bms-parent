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
public class AccountQueryVo extends PaginationModel {
    /**
     * 姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String mobilePhone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 登录名
     */
    private String loginName;

    /**
     * 状态（enabled：启用、disable：禁用）
     */
    private String status;
}
