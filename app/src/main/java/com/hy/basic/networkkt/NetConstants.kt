package com.hy.basic.networkkt

/**
 * @author hy
 * @date 2021/9/27
 * desc: NetConstants
 **/
object NetConstants {
    /**
     * 超时时间
     */
    const val DEFAULT_CONNECT_TIMEOUT = 10L
    const val DEFAULT_READ_TIMEOUT = 10L
    const val DEFAULT_WRITE_TIMEOUT = 10L

    const val DEFAULT_CACHE_SIZE = 100 * 1024 * 1024


    const val BASE_URL = "https://test-app-api.huxiu.com/"
    const val BASE_URL_REVIEW = "https://t-api-review.huxiu.com/v1/"
}