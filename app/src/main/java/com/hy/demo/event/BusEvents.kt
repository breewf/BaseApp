package com.hy.demo.event

/**
 * desc: 事件总线演示用的事件定义。“事件即类型”,直接用 data class 描述,无需继承任何基类。
 */

/** 加购事件:一处 post,多个订阅者同时收到,演示一对多全局广播 */
data class CartEvent(
    val from: String,
    val skuName: String,
    val count: Int,
)

/** 登录事件(粘性):发布后缓存最新一条,晚创建的页面订阅时也能立即补收 */
data class LoginEvent(
    val userName: String,
    val token: String,
)
