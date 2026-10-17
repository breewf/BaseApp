package com.hy.myapp.demo.event

/**
 * desc: 事件总线演示用的事件定义 —— "事件即类型",直接用 data class 描述,无需继承任何基类。
 *
 * 约定:一个业务动作 = 一个事件类;需要带多个字段时全放进这一个 data class,
 * 而不是靠重载或字符串 tag 区分(那样 IDE 无法跳转、排查也看不清)。
 */

/**
 * 加购事件:一处 [com.hy.basic.event.EventBus.post],多个订阅者
 * (本页两个 Fragment + Activity 自身)同时收到 —— 演示**一对多全局广播**。
 * [from] 标记是谁发的,方便在两侧日志里对照"左发右收 / 头发全收"。
 */
data class CartEvent(
    val from: String,
    val skuName: String,
    val count: Int,
)

/**
 * 登录事件(**粘性**):发布后缓存最新一条,晚创建的页面(如 [LoginReceiverActivity])
 * 订阅时也能立即补收到,无需关心发送与接收的先后顺序。
 */
data class LoginEvent(
    val userName: String,
    val token: String,
)
