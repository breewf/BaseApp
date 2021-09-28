package com.hy.basic.utils;

import com.blankj.utilcode.util.NetworkUtils;

/**
 * @author hy
 * @date 2021/7/12
 * desc: 网络判断工具类
 * @see com.blankj.utilcode.util.NetworkUtils
 **/
public class AppNetworkUtils {

    /**
     * @return Wi-Fi 当前是否连接
     */
    public static boolean isWifiConnected() {
        try {
            return NetworkUtils.isWifiConnected();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return 网络 当前是否连接
     */
    public static boolean isConnected() {
        try {
            return NetworkUtils.isConnected();
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * @return 4g 当前是否连接
     */
    public static boolean is4G() {
        try {
            return NetworkUtils.is4G();
        } catch (Exception e) {
            return false;
        }
    }

}
