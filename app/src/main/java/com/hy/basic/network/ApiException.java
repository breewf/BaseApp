package com.hy.basic.network;

import com.hy.baseapp.common.Toasts;

/**
 * @author hy
 * @date 2021/7/12
 * desc: 服务端错误码
 **/
public class ApiException extends RuntimeException {

    private static final int ERROR_USER_NOT_EXIST = 100;
    private static final int ERROR_WRONG_PASSWORD = 101;

    public int code;
    public String message;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;

        showToast(message);
    }

    private void showToast(String message) {
        Toasts.showShort(message);
    }

    /**
     * 映射服务器返回的自定义错误码，
     *
     * @param code 服务器返回的自定义错误码
     */
    private static String toApiErrorMessage(int code) {
        String message;
        switch (code) {
            case ERROR_USER_NOT_EXIST:
                message = "用户不存在";
                break;
            case ERROR_WRONG_PASSWORD:
                message = "密码错误";
                break;
            default:
                message = "未知错误";
        }
        return message;
    }
}
