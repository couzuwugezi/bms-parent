package com.bms.project.model;


import com.baomidou.mybatisplus.extension.api.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页返回
 * @param <T>
 */
public class ResponsePage<T> {

    private String rtnFlag;

    private String rtnMsg;

    private Long total;

    private List<R> rows;

    public ResponsePage() {
        super();
    }

    public ResponsePage(Long total, List<R> rows) {
        super();
        this.total = total;
        this.rows = rows;
    }

    public ResponsePage(String rtnFlag, String rtnMsg) {
        this.rtnFlag = rtnFlag;
        this.rtnMsg = rtnMsg;
    }

    public ResponsePage(String rtnFlag, String rtnMsg, Long total, List<R> rows) {
        this.rtnFlag = rtnFlag;
        this.rtnMsg = rtnMsg;
        this.total = total;
        this.rows = rows;
    }

    public static ResponsePage success(Long total, List rows) {
        return new ResponsePage("9999", "接口调用成功", total, rows);
    }

    public static ResponsePage emptyResponsePage() {
        return ResponsePage.success(0L, new ArrayList(0));
    }

    public static ResponsePage error(String rtnMsg) {
        return new ResponsePage("0000", rtnMsg, 0L, new ArrayList(0));
    }

    public String getRtnFlag() {
        return rtnFlag;
    }

    public void setRtnFlag(String rtnFlag) {
        this.rtnFlag = rtnFlag;
    }

    public String getRtnMsg() {
        return rtnMsg;
    }

    public void setRtnMsg(String rtnMsg) {
        this.rtnMsg = rtnMsg;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<R> getRows() {
        return rows;
    }

    public void setRows(List<R> rows) {
        this.rows = rows;
    }

}
