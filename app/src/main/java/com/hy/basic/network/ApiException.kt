package com.hy.basic.network

/**
 * @author hy
 * @date 2021/9/27
 * desc: ApiException
 **/
class ApiException(val code: Int?, msg: String) : Exception(msg)
