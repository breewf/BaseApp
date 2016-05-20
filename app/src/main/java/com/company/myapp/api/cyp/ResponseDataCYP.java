package com.company.myapp.api.cyp;

/**
 * 车易拍ResponseData
 */
public class ResponseDataCYP {

    public int getResCode() {
        return resCode;
    }

    public void setResCode(int resCode) {
        this.resCode = resCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int resCode;
    public String msg;

    @Override
    public String toString() {
        return "ResponseData{" +
                "resCode=" + resCode +
                ", msg='" + msg + '\'' +
                '}';
    }
}
