package com.hy.baseapp.helper;

import android.os.Environment;

import com.hy.baseapp.common.log.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by GHY on 2016/1/14.
 * 文件助手工具类
 */
public class FileHelper {

    public static final String APP_ROOT = "baseApp"; //App根目录
    public static final String APP_ROOT_LOG = "logs"; //log文件夹根目录
    public static final String APP_DOWNLOAD = "download"; //下载文件夹根目录
    public static final String APP_ROOT_COUNT = "analyse"; //统计文件夹根目录
    public static final String COUNT_FILE_NAME = "temp"; //统计文件文件名


    /**
     * 获取log文件夹
     *
     * @return
     */
    public static String getLogRootPath() {
        return createFolder(APP_ROOT_LOG);
    }

    /**
     * 获取下载文件夹
     *
     * @return
     */
    public static String getDownloadPath() {
        return createFolder(APP_DOWNLOAD);
    }

    /**
     * 获取统计文件夹
     *
     * @return
     */
    public static String getCountRootPath() {
        return createFolder(APP_ROOT_COUNT);
    }

    /**
     * 获取统计文件
     *
     * @return
     */
    public static File getCountFile() {
        return createFile(getCountRootPath(), COUNT_FILE_NAME);
    }


    /**
     * 创建一个文件夹
     *
     * @param filePath
     * @return
     */
    private static String createFolder(String filePath) {
        if (createRootPath() != null) {
            File dirFile = new File(createRootPath() + filePath);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            return dirFile + File.separator;
        } else {
            Log.e("SD卡不存在-->>创建文件夹失败");
            return null;
        }

    }

    /**
     * 创建一个APP的根目录
     *
     * @return
     */
    private static String createRootPath() {
        if (getSDCardRoot() != null) {
            File rootFile = new File(getSDCardRoot() + APP_ROOT);
            if (!rootFile.exists()) {
                rootFile.mkdirs();
            }
            return rootFile + File.separator;
        } else {
            Log.e("SD卡不存在-->>创建根目录失败");
            return null;
        }
    }

    /**
     * 创建文件
     *
     * @param path     文件保存的路径
     * @param fileName 文件名
     * @return
     */
    private static File createFile(String path, String fileName) {
        File file;
        if (path != null && fileName != null) {
            file = new File(path + fileName);
            Log.i("文件路径---->>>>" + path + fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("创建文件发生异常");
                    return null;
                }
            }
        } else {
            Log.e("创建文件时发生错误-->>文件路径或文件名为空");
            return null;
        }
        return file;
    }

    /**
     * 删除文件
     *
     * @param path 文件所在的文件夹
     * @param name 文件名
     * @return true:删除成功 false:删除失败
     */
    public static boolean deleteFile(String path, String name) {
        return delete(path + File.separator + name);
    }

    /**
     * 删除文件的方法
     *
     * @param path 文件的绝对路径
     * @return true:删除成功 false:删除失败
     */
    private static boolean delete(String path) {

        //路径为空，返回删除失败
        if (path == null) return false;

        File file = new File(path);
        //文件为空或文件不存在，返回删除成功
        if (file == null || !file.exists()) {
            Log.i("文件不存在，返回删除成功");
            return true;
        } else {
            if (file.isDirectory()) {
                Log.i("路径为文件夹，删除文件失败");
                return false;
            } else {
                boolean isDelete = file.delete();
                return isDelete;
            }
        }
    }

    /**
     * SD卡是否存在
     *
     * @return true:存在 false:不存在
     */
    private static boolean isSDCardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡的根路径
     *
     * @return
     */
    private static String getSDCardRoot() {
        return isSDCardExist() ? Environment.getExternalStorageDirectory() + File.separator : null;
    }

}
