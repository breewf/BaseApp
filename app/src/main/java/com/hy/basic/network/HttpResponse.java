package com.hy.basic.network;

import com.google.gson.annotations.SerializedName;
import com.hy.basic.BaseModel;

/**
 * @author hy
 * @date 2021/7/12
 * desc: 网络请求响应
 **/
public class HttpResponse<T> extends BaseModel {

    public static final int FAIL = 0;
    public static final int SUCCESS = 1;

    @SerializedName("code")
    public int code;

    @SerializedName("msg")
    public String msg;

    @SerializedName("success")
    public boolean success;

    @SerializedName("data")
    public T data;

    @SerializedName("statusCode")
    public String statusCode;
    @SerializedName("desc")
    public String desc;
    @SerializedName("result")
    public T result;
}
