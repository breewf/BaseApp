package com.hy.baseapp.utils;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 名称：StringUtils.java
 * 描述：字符串处理类.
 */
public class StringUtils {

    /**
     * 描述：将null转化为“”.
     *
     * @param str 指定的字符串
     * @return 字符串的String类型
     */
    public static String parseEmpty(String str) {
        if (str == null || "null".equals(str.trim())) {
            str = "";
        }
        return str.trim();
    }

    /**
     * 描述：判断一个字符串是否为null或空值.
     *
     * @param str 指定的字符串
     * @return true or false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * is null or its length is 0 or it is made by space
     *
     * @param str
     * @return if string is null or its size is 0 or it is made by space, return true, else return false.
     */
    public static boolean isBlank(String str) {
        return (str == null || str.trim().length() == 0);
    }

    /**
     * 保留小数点后2位数字
     *
     * @param num
     * @return
     */
    public static String format2Num(String num) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(num);
    }

    /**
     * 保留小数点后2位数字
     *
     * @param num
     * @return
     */
    public static BigDecimal get2Num(String num) {
        if (TextUtils.isEmpty(num)) {
            num = "0";
        }
        BigDecimal bd = new BigDecimal(num);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    /**
     * 保留小数点后几位数字
     *
     * @param num
     * @param position
     * @return
     */
    public static BigDecimal getNum(String num, int position) {
        if (TextUtils.isEmpty(num)) {
            num = "0";
        }
        BigDecimal bd = new BigDecimal(num);
        bd = bd.setScale(position, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    /**
     * 金额格式化，三位加上一个逗号，保留小数点后两位
     * 前面无￥符
     * 此方法如果输入 0.88 则返回 .88 有瑕疵
     *
     * @param amount 金额
     * @return 格式后的金额 如：10,000.50
     */
    public static String formatMoney(String amount) {
        if (TextUtils.isEmpty(amount) || "0".equals(amount)) {
            return "0.00";
        }
        double money = Double.parseDouble(amount);
        DecimalFormat df = new DecimalFormat("#,###.00");
        return df.format(money);
    }

    /**
     * 金额格式化，三位加上一个逗号，保留小数点后两位
     * 前面有￥符
     *
     * @param s 金额
     * @return 格式后的金额 如：￥10,000.50
     */
    public static String moneyFormatY(String s) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CHINA);
        return format.format(Double.valueOf(s));
    }

    /**
     * 金额格式化，三位加上一个逗号，保留小数点后两位
     * 前面不带￥符
     *
     * @param s 金额
     * @return 格式后的金额 如：10,000.50
     */
    public static String moneyFormat(String s) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CHINA);
        String money;
        try {
            money = format.format(Double.valueOf(s));
        } catch (NumberFormatException e) {
            return "0.00";
        }
        return money.replaceAll("￥", "");
    }

    /**
     * 格式化手机号，中间四位使用*代替
     *
     * @param phoneNumber 手机号
     * @return 格式后的金额 如：135****9631
     */
    public static String formatPhoneMiddle(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }
        if (phoneNumber.length() != 11) {
            return phoneNumber;
        }
        return phoneNumber.replace(phoneNumber.substring(3, 7), "****");
    }

    /**
     * 首字母大写
     * <p>
     * capitalizeFirstLetter(null)     =   null;
     * capitalizeFirstLetter("")       =   "";
     * capitalizeFirstLetter("2ab")    =   "2ab"
     * capitalizeFirstLetter("a")      =   "A"
     * capitalizeFirstLetter("ab")     =   "Ab"
     * capitalizeFirstLetter("Abc")    =   "Abc"
     *
     * @param str
     * @return
     */
    public static String capitalizeFirstLetter(String str) {
        if (isEmpty(str)) {
            return str;
        }
        char c = str.charAt(0);
        return (!Character.isLetter(c) || Character.isUpperCase(c)) ? str : new StringBuilder(str.length())
                .append(Character.toUpperCase(c)).append(str.substring(1)).toString();
    }

    /**
     * 字符串中的字母转化为大写
     *
     * @param s
     * @return
     */
    public static String toUpperCaseString(String s) {
        return s.toUpperCase();
    }

    /**
     * 描述：截取字符串从第一个指定字符.
     *
     * @param str1   原文本
     * @param str2   指定字符
     * @param offset 偏移的索引
     * @return 截取后的字符串
     */
    public static String cutStringFromChar(String str1, String str2, int offset) {
        if (isEmpty(str1)) {
            return "";
        }
        int start = str1.indexOf(str2);
        if (start != -1) {
            if (str1.length() > start + offset) {
                return str1.substring(start + offset);
            }
        }
        return "";
    }

    /**
     * 描述：ip地址转换为10进制数.
     *
     * @param ip the ip
     * @return the long
     */
    public static long ip2int(String ip) {
        ip = ip.replace(".", ",");
        String[] items = ip.split(",");
        return Long.valueOf(items[0]) << 24 | Long.valueOf(items[1]) << 16 | Long.valueOf(items[2]) << 8 | Long.valueOf(items[3]);
    }

    /**
     * 加密串串
     *
     * @param str
     * @param start 开始加密位置
     * @param end   结束加密位置
     * @return
     */
    public static String formatString(String str, int start, int end) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i >= start && i <= end) {
                sb.append('*');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
