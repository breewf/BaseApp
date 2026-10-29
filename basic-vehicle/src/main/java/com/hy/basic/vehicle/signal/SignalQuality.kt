package com.hy.basic.vehicle.signal

/**
 * 信号质量。
 *
 * 用法：上屏前判断 [isUsable]；INVALID/STALE 显示 "--"。
 * 注意：解码层目前只产出 VALID/INVALID，STALE 由超时逻辑补发。
 */
enum class SignalQuality {

    /** 值在有效范围内,可以正常展示 */
    VALID,

    /** 无效哨兵值,或换算后的物理值超出约定范围,UI 应显示 "--" */
    INVALID,

    /** 曾经有效,但超过约定时间没有更新,只能当历史值参考 */
    STALE,

    /** 还没收到过任何值 */
    UNKNOWN;

    /** 是否可以直接上屏 */
    val isUsable: Boolean get() = this == VALID
}
