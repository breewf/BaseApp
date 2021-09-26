package com.hy.baseapp.utils;

import android.text.TextUtils;

/**
 * @author hy
 * @date 2018/12/10
 * Desc:catch异常的parse
 */
public class ParseUtils {

    public static int parseInt(String sInt) {
        int number = 0;
        try {
            number = Integer.parseInt(TextUtils.isEmpty(sInt) ? "0" : sInt);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return number;
    }

    public static float parseFloat(String sFloat) {
        float floatNumber = 0;
        try {
            floatNumber = Float.parseFloat(TextUtils.isEmpty(sFloat) ? "0" : sFloat);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return floatNumber;
    }

    public static double parseDouble(String sDouble) {
        double doubleNumber = 0;
        try {
            doubleNumber = Double.parseDouble(TextUtils.isEmpty(sDouble) ? "0" : sDouble);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return doubleNumber;
    }

    public static long parseLong(String sLong) {
        long longNumber = 0;
        try {
            longNumber = Long.parseLong(TextUtils.isEmpty(sLong) ? "0" : sLong);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return longNumber;
    }
}
