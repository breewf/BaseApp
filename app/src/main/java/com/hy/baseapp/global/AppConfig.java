
package com.hy.baseapp.global;


/**
 * 名称：AppConfig.java
 * 描述：初始设置类.
 *
 */
public class AppConfig {
	
	/**  UI设计的基准宽度. */
	public static int UI_WIDTH = 720;
	
	/**  UI设计的基准高度. */
	public static int UI_HEIGHT = 1280;
	
	/**  UI设计的密度. */
	public static int UI_DENSITY = 2;
	
	/** 默认 SharePreferences文件名. */
	public static String SHARED_PATH = "sp";
	
	/** 默认下载文件地址. */
	public static  String DOWNLOAD_ROOT_DIR = "download";
	
    /** 默认下载图片文件地址. */
	public static  String DOWNLOAD_IMAGE_DIR = "images";
	
    /** 默认下载文件地址. */
	public static  String DOWNLOAD_FILE_DIR = "files";
	
	/** APP缓存目录. */
	public static  String CACHE_DIR = "cache";
	
	/** DB目录. */
	public static  String DB_DIR = "db";
	
	/** 默认磁盘缓存超时时间设置，毫秒. */
	public static long DISK_CACHE_EXPIRES_TIME = 60*1000*1000;
	
	/** 内存缓存大小  单位10M. */
	public static int MAX_CACHE_SIZE_INBYTES = 10*1024*1024;
	
	/** 磁盘缓存大小  单位10M. */
	public static int MAX_DISK_USAGE_INBYTES = 10*1024*1024;
	
	/** The Constant CONNECTEXCEPTION. */
	public static String CONNECT_EXCEPTION = "无法连接到网络";
	
	/** The Constant UNKNOWNHOSTEXCEPTION. */
	public static String UNKNOWN_HOST_EXCEPTION = "连接远程地址失败";
	
	/** The Constant SOCKETEXCEPTION. */
	public static String SOCKET_EXCEPTION = "网络连接出错，请重试";
	
	/** The Constant SOCKETTIMEOUTEXCEPTION. */
	public static String SOCKET_TIMEOUT_EXCEPTION = "连接超时，请重试";
	
	/** The Constant NULLPOINTEREXCEPTION. */
	public static String NULL_POINTER_EXCEPTION = "抱歉，远程服务出错了";
	
	/** The Constant NULLMESSAGEEXCEPTION. */
	public static String NULL_MESSAGE_EXCEPTION = "抱歉，程序出错了";
	
	/** The Constant CLIENTPROTOCOLEXCEPTION. */
	public static String CLIENT_PROTOCOL_EXCEPTION = "Http请求参数错误";
	
	/** 参数个数不够. */
	public static String MISSING_PARAMETERS = "参数没有包含足够的值";
	
	/** The Constant REMOTESERVICEEXCEPTION. */
	public static String REMOTE_SERVICE_EXCEPTION = "抱歉，远程服务出错了";
	
	/** 资源未找到. */
	public static String NOT_FOUND_EXCEPTION = "请求的资源无效404";
	
	/** 没有权限访问资源. */
	public static String FORBIDDEN_EXCEPTION = "没有权限访问资源";
	
	/** 其他异常. */
	public static String UNTREATED_EXCEPTION = "未处理的异常";
	

}
