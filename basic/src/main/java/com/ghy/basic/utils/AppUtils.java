package com.ghy.basic.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.ghy.basic.R;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * 名称：AppUtils.java
 * 描述：应用工具类
 */
public class AppUtils {

    /**
     * 回到Home页，后台运行
     *
     * @param context
     */
    public static void goHome(Context context) {
        Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(mHomeIntent);
    }

    /**
     * 判断上下文是否被销毁
     *
     * @return
     */
    public static boolean isContextDestroyed(Context context) {
        if (context == null) return true;
        if (context instanceof FragmentActivity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return ((FragmentActivity) context).isDestroyed() || ((FragmentActivity) context).isFinishing();
            } else {
                return ((FragmentActivity) context).isFinishing();
            }
        } else if (context instanceof Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return ((Activity) context).isDestroyed() || ((Activity) context).isFinishing();
            } else {
                return ((Activity) context).isFinishing();
            }
        } else {
            return false;
        }
    }

    /**
     * 获取App包信息
     *
     * @param context
     * @return
     */
    public static PackageInfo getPackageInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo;
    }

    /**
     * 描述：打开并安装文件.
     *
     * @param context the context
     * @param file    apk文件路径
     */
    public static void installApk(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 描述：卸载程序.
     *
     * @param context     the context
     * @param packageName 包名
     */
    public static void uninstallApk(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        Uri packageURI = Uri.parse("package:" + packageName);
        intent.setData(packageURI);
        context.startActivity(intent);
    }

    /**
     * 描述：判断网络是否有效.
     *
     * @param context the context
     * @return true, if is network available
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Gps是否打开
     * 需要<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />权限
     *
     * @param context the context
     * @return true, if is gps enabled
     */
    public static boolean isGpsEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 判断当前网络是否是移动数据网络.
     *
     * @param context the context
     * @return boolean
     */
    public static boolean isMobile(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    /**
     * 获取屏幕尺寸与密度.
     *
     * @param context the context
     * @return mDisplayMetrics
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        Resources mResources;
        if (context == null) {
            mResources = Resources.getSystem();
        } else {
            mResources = context.getResources();
        }
        return mResources.getDisplayMetrics();
    }

    /**
     * dip2px
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 显示或隐藏软键盘
     *
     * @param view
     * @param isShow true = show , false = hide
     */
    public static void toggleSoftKeyboard(View view, boolean isShow) {
        Context context = view.getContext();
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        if (isShow) {
            view.requestFocus();
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 显示软键盘
     *
     * @param view
     */
    public static void showSoftKeyboard(View view) {
        Context context = view.getContext();
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        view.requestFocus();
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * 隐藏软键盘
     *
     * @param view
     */
    public static void hideSoftKeyboard(View view) {
        Context context = view.getContext();
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 着色
     *
     * @param drawable
     * @param color
     * @return
     */
    public static Drawable tintDrawable(Drawable drawable, int color) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        ColorStateList colors = ColorStateList.valueOf(color);
        DrawableCompat.setTintList(wrappedDrawable, colors);
        return wrappedDrawable;
    }

    /**
     * 获取应用版本名称 如：V3.2.0
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (TextUtils.isEmpty(versionName)) return "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取应用版本号 如：64
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取手机安卓版本号，如23
     *
     * @return
     */
    public static int getAndVersionCode() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取手机安卓版本号名称，如6.0
     *
     * @return
     */
    public static String getAndVersionName() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机厂商名称
     *
     * @return
     */
    public static String getPhoneType() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取手机型号
     *
     * @return
     */
    public static String getPhoneModel() {
        return Build.MODEL;
    }

    /**
     * 获取设备ID
     *
     * @param context
     * @return
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getDeviceID(Context context) {
        String deviceId = "";
        try {
            deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception var4) {
            var4.printStackTrace();
        }
        return deviceId;
    }

    /**
     * 获取IMEI.
     *
     * @return
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getIMEI(Context context) {
        String deviceId = "";
        try {
            deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception var4) {
            var4.printStackTrace();
        }
        return deviceId;
    }

    /**
     * 判断是否安装了某个应用
     *
     * @param context
     * @param pkgName
     * @return
     */
    public static boolean isAPPInstalled(Context context, String pkgName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    /**
     * 判断是否安装了某个应用
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }

    /**
     * 判断是否安装了微信
     *
     * @param context
     * @return
     */
    public static boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断qq是否可用
     *
     * @param context
     * @return
     */
    public static boolean isQQClientAvailable(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mobileqq")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断新浪微博是否可用
     *
     * @param context
     * @return
     */
    public static boolean isSinaWeiboClientAvailable(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.sina.weibo")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 设置 MIUI系统或Flyme系统 状态栏为白底黑字
     *
     * @param activity
     * @param view
     */
    public static void statusBarMuUiFlyMe(Activity activity, View view) {
        if (AppUtils.isMiUiOS()) {//小米手机，设置为白底黑字
            AppUtils.setMiuiStatusBarDarkMode(activity, true);
            view.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
        }
        if (AppUtils.isMeizuFlymeOS()) {//魅族手机，设置为白底黑字
            StatusBarFlyMeUtils.setStatusBarDarkIcon(activity, true);
            view.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
        }
    }

    /**
     * 设置 MIUI系统或Flyme系统 状态栏是否为白色字体
     *
     * @param activity
     */
    public static void statusBarMuUiFlyMeWhite(Activity activity, boolean isWhite) {
        if (AppUtils.isMiUiOS()) {//小米手机
            AppUtils.setMiuiStatusBarDarkMode(activity, !isWhite);
        }
        if (AppUtils.isMeizuFlymeOS()) {//魅族手机
            StatusBarFlyMeUtils.setStatusBarDarkIcon(activity, !isWhite);
        }
    }

    /*-------------------------Flyme状态栏--------------------------*/

    /**
     * 判断是魅族操作系统（Flyme）
     *
     * @return
     */
    public static boolean isMeizuFlymeOS() {
        String name = getMeizuFlymeOSFlag();
        return !TextUtils.isEmpty(name) && name.toLowerCase().contains("flyme");
    }

    /**
     * 获取魅族系统版本标识
     */
    public static String getMeizuFlymeOSFlag() {
        return getSystemProperty("ro.build.display.id", "");
    }

    /**
     * 获取MIUI系统版本标识
     */
    public static String getMIUIVersionName() {
        return getSystemProperty("ro.miui.ui.version.name", "");
    }

    /**
     * 获取系统标识
     */
    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String) get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }
    /*-------------------------Flyme状态栏--------------------------*/

    /*-------------------------MIUI状态栏--------------------------*/

    /**
     * 如何检测小米设备
     * 请使用android.os.Build对象，查询MANUFACTURER和MODEL的值，MANUFACTURER值为Xiaomi即为小米设备
     * AppUtils.getPhoneType().equals("Xiaomi")
     */

    /**
     * 是否为Miui系统 (V6以上)
     *
     * @return
     */
    public static boolean isMiUiOS() {
        String name = getMIUIVersionName();
        return !TextUtils.isEmpty(name) && name.toLowerCase().matches("v[6-9]");
    }

    /**
     * MIUI v6支持4.4及以上版本，调用状态栏透明的方法可以直接用原生的安卓方法
     *
     * @param activity
     * @param on
     */
    @TargetApi(19)
    public static void setMiuiTranslucentStatus(Activity activity, boolean on) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * 下面是调用状态栏 是否为darkmode
     *
     * @param activity
     * @param darkmode
     */
    public static void setMiuiStatusBarDarkMode(Activity activity, boolean darkmode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            int darkModeFlag = 0;
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkmode ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*-------------------------MIUI状态栏--------------------------*/

    /**
     * 获取mac地址.
     *
     * @param context
     * @return
     */
    @SuppressLint("HardwareIds")
    public static String getMac(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        if (wifi != null) {
            info = wifi.getConnectionInfo();
        }
        if (info == null || info.getMacAddress() == null) {
            return null;
        } else {
            return info.getMacAddress();
        }
    }

    /**
     * 获取SSID地址.
     *
     * @param context
     * @return
     */
    public static String getSSID(Context context) {

        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = null;
        if (wifi != null) {
            info = wifi.getConnectionInfo();
        }
        if (info == null || info.getSSID() == null) {
            return null;
        } else {
            return info.getSSID();
        }
    }

    /**
     * 查询手机内所有应用（包括系统应用）
     *
     * @return
     */
    public static List<PackageInfo> getAllApps(Context context) {
        PackageManager pManager = context.getApplicationContext().getPackageManager();
        //获取手机内所有应用
        return pManager.getInstalledPackages(0);
    }

    /**
     * 查询手机内非系统应用
     *
     * @param context
     * @return
     */
    public static List<PackageInfo> getAllUserApps(Context context) {
        List<PackageInfo> apps = new ArrayList<>();
        PackageManager pManager = context.getPackageManager();
        //获取手机内所有应用
        List<PackageInfo> pakList = pManager.getInstalledPackages(0);
        for (int i = 0; i < pakList.size(); i++) {
            PackageInfo pak = pakList.get(i);
            //判断是否为非系统预装的应用程序
            if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                apps.add(pak);
            }
        }
        return apps;
    }

}
