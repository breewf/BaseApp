package com.hy.basic.vehicle.signal

/**
 * desc: 信号质量。
 *
 * 解码层目前只会给出 VALID / INVALID;STALE、UNKNOWN 留给后续的信号管理
 * (超时未更新、还没收到过)使用。UI 判断能不能上屏,统一看 [isUsable],
 * 不要直接把 0 当成有效值——车机上 0 和"没数据"是两回事。
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
