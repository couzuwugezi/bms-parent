package com.bms.project.common;

import lombok.Data;

/**
 * @author liqiang
 * @date 2019/11/3 21:34
 */
@Data
public class PaginationModel {
    private Integer currentPage;
    private Integer pageSize;
}
