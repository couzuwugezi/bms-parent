package com.bms.project.model;

/**
 * 统一返回
 *
 * @author liqiang
 * @param <T>
 */
public class ResponseData<T> {
    /**
     * 统一返回码
     */
    private String rtnFlag;

    /**
     * 统一返回消息
     */
    private String rtnMsg;

    /**
     * 结果对象
     */
    private T rtnData;

    /**
     * 错误信息
     */
    private String errInfo;

    private boolean success;

    public ResponseData() {
    }

    public ResponseData(String rtnFlag, String rtnMsg) {
        this.rtnFlag = rtnFlag;
        this.rtnMsg = rtnMsg;
        this.success = ResponseCode.MS_SUCCESS.getCode().equals(rtnFlag);
    }

    public ResponseData(String rtnFlag, String rtnMsg, T rtnData, String errInfo) {
        this.rtnFlag = rtnFlag;
        this.rtnMsg = rtnMsg;
        this.rtnData = rtnData;
        this.errInfo = errInfo;
        this.success = ResponseCode.MS_SUCCESS.getCode().equals(rtnFlag);
    }

    public static ResponseData success() {
        return new ResponseData<>(ResponseCode.MS_SUCCESS.getCode(), "成功", null, "");
    }

    public static ResponseData success(Object t) {
        return new ResponseData<>(ResponseCode.MS_SUCCESS.getCode(), "成功", t, "");
    }

    public static ResponseData error() {
        return new ResponseData(ResponseCode.MS_SYSTEM_ERROR.getCode(), ResponseCode.MS_SYSTEM_ERROR.getDesc());
    }

    public static ResponseData error(String rtnMsg) {
        return new ResponseData(ResponseCode.MS_SYSTEM_ERROR.getCode(), rtnMsg);
    }

    public static ResponseData error(ResponseCode responseCode) {
        return new ResponseData(responseCode.getCode(), responseCode.getDesc());
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

    public T getRtnData() {
        return rtnData;
    }

    public void setRtnData(T rtnData) {
        this.rtnData = rtnData;
    }

    public String getErrInfo() {
        return errInfo;
    }

    public void setErrInfo(String errInfo) {
        this.errInfo = errInfo;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "rtnFlag='" + rtnFlag + '\'' +
                ", rtnMsg='" + rtnMsg + '\'' +
                ", rtnData=" + rtnData +
                ", errInfo='" + errInfo + '\'' +
                ", success=" + success +
                '}';
    }
}
