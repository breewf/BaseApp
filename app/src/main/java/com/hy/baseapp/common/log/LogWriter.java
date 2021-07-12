/*
 * Copyright (C) 2013 litesuits.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.hy.baseapp.common.log;

import android.os.Environment;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 写日志文件
 * 文件路径在根目录下log.txt
 */
public class LogWriter extends Thread {
    public static final String TAG = "LogWriter";
    public static final String LOG_FILE_PATH = "/log.txt";
    public static final String LOG_ROOT_PATH = Environment.getExternalStorageDirectory().getPath();

    public static boolean open = true;
    private static LogWriter instance = null;
    private static Process mLogcatProcess = null;

    private BufferedReader mReader = null;
    private String packageName = "*";

    public static void startCatchLog(String packageName) {
        if (!open) return;
        if (instance == null) {
            instance = new LogWriter();
            instance.packageName = packageName;
            instance.start();
        }
    }

    public static void stopCatchLog() {
        if (!open) return;
        if (mLogcatProcess != null) {
            mLogcatProcess.destroy();
            mLogcatProcess = null;
        }
    }

    @Override
    public void run() {
        Log.i(TAG, "log writer(catcher) is running. ---------------------------");
        BufferedWriter bw = null;
        try {
            mLogcatProcess = Runtime.getRuntime().exec("logcat " + packageName + ":I");
            mReader = new BufferedReader(new InputStreamReader(mLogcatProcess.getInputStream()));

            // 打印系统信息
            logSystemInfo();

            String line;
            File file = new File(LOG_ROOT_PATH + LOG_FILE_PATH);
            if (file.exists() && isFileSizeOutOf10M(file)) {
                file.delete();
            }
            if (file.exists()) {
                Log.i(TAG, "log file size is :"
                        + FormatFileSize(file.length()));
            }
            FileWriter fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            while ((line = mReader.readLine()) != null) {
                bw.append(line);
                bw.newLine();
                bw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.i(TAG, "Log writer(catcher) and bufferWriter has closed. ------------------");
            try {
                if (mReader != null) {
                    mReader.close();
                    mReader = null;
                }
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            instance = null;
        }
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String FormatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString;
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    /**
     * 判断文件是否大于10M
     *
     * @param file
     * @return
     * @throws Exception
     */
    public static boolean isFileSizeOutOf10M(File file) throws Exception {
        if (file == null) return false;
        return file.length() >= 10485760;
    }

    public static void logSystemInfo() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = dateFormat.format(date);
        Log.w("system", "New Start $$$$$$$$$$$$$$###########   " + time + "############$$$$$$$$$$$$$$$");
        Log.w("system", "android.os.Build.BOARD:" + android.os.Build.BOARD);
        Log.w("system", "android.os.Build.DEVICE:" + android.os.Build.DEVICE);
        Log.w("system", "android.os.Build.MANUFACTURER:"
                + android.os.Build.MANUFACTURER);
        Log.w("system", "android.os.Build.MODEL:" + android.os.Build.MODEL);
        Log.w("system", "android.os.Build.PRODUCT:" + android.os.Build.PRODUCT);
        Log.w("system", "android.os.Build.VERSION.CODENAME:"
                + android.os.Build.VERSION.CODENAME);
        Log.w("system", "android.os.Build.VERSION.RELEASE:"
                + android.os.Build.VERSION.RELEASE);
    }
}
