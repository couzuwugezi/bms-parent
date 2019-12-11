package com.bms.project.spring;

import com.bms.project.model.ResponseCode;

/**
 * @author liqiang
 * @date 2019/10/27 18:36
 */
public class BmsException extends RuntimeException {

    private String errCode;
    private String errMsg;

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public BmsException(ResponseCode responseCode) {
        this.errCode = responseCode.getCode();
        this.errMsg = responseCode.getDesc();
    }


}
