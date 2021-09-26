package com.hy.baseapp.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则验证
 */
public class RegularUtils {

    /**
     * 邮箱验证
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\" +
                ".[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * 验证手机号
     *
     * @param phonenum
     * @return
     */
    public static boolean isPhoneNum(String phonenum) {
        Pattern p = Pattern.compile("^13[\\d]{9}$|^14[5,7,9]{1}\\d{8}$|^15[^4]{1}\\d{8}$|^17[0,1,3,5-" +
                "8]{1}\\d{8}$|^18[\\d]{9}$");
        Matcher m = p.matcher(phonenum);
        return m.matches();
    }

    /**
     * 验证密码 必须是数字与字母的混合
     *
     * @param password
     * @return
     */
    public static boolean isPassword(String password) {
        Pattern p = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]*$");
        Matcher m = p.matcher(password);
        return m.matches();
    }

    /**
     * 验证身份证  18位数字或者最后一位为X
     *
     * @param idCard
     * @return
     */
    public static boolean isIdCard(String idCard) {
        Pattern p = Pattern.compile("^(^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$)|(^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[Xx])$)$");
        Matcher m = p.matcher(idCard);
        return m.matches();
    }

    /**
     * 验证银行卡号合法性
     *
     * @param bankCard
     * @return
     */
    public static boolean isBankCard(String bankCard) {
        Pattern p = Pattern.compile("^[0-9]{16,19}$");
        Matcher m = p.matcher(bankCard);
        return m.matches();
    }

    /**
     * 是否是有效的身份证号码
     *
     * @param idNumber
     * @return
     */
    public static boolean isLegal(String idNumber) {
        if (TextUtils.isEmpty(idNumber)) {
            return false;
        }
        boolean result = idNumber.matches("[0-9]{15}|[0-9]{17}[0-9X]");
        if (result) {
            int year, month, date;
            if (idNumber.length() == 15) {
                year = Integer.parseInt(idNumber.substring(6, 8));
                month = Integer.parseInt(idNumber.substring(8, 10));
                date = Integer.parseInt(idNumber.substring(10, 12));
            } else {
                year = Integer.parseInt(idNumber.substring(6, 10));
                month = Integer.parseInt(idNumber.substring(10, 12));
                date = Integer.parseInt(idNumber.substring(12, 14));
            }
            switch (month) {
                case 2:
                    result = (date >= 1) && (year % 4 == 0 ? date <= 29 : date <= 28);
                    break;
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    result = (date >= 1) && (date <= 31);
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    result = (date >= 1) && (date <= 31);
                    break;
                default:
                    result = false;
                    break;
            }
        }
        return result;
    }

    public static boolean isWebUrl(String url) {
        Pattern pattern = Pattern.compile("^(((ht|f)tp(s?))\\://)?(www.|[a-zA-Z].)[a-zA-Z0-9\\-\\.]+\\.(com|edu|gov|mil|net|org|biz|info|name|museum|us|ca|uk)(\\:[0-9]+)*(/($|[a-zA-Z0-9\\.\\,\\;\\?\\'\\\\\\+&amp;%\\$#\\=~_\\-]+))*$");
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }
}
