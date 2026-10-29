package com.hy.basic.vehicle.driving

/**
 * desc: 驾驶限制等级。
 *
 * 只表达"当前允许做到什么程度",具体哪些按钮禁用由页面自己决定。
 */
enum class DrivingRestrictionLevel {
    /** 不受限 */
    NONE,

    /** 只允许简单浏览,禁止输入和视频 */
    SIMPLE,

    /** 全部锁定 */
    LOCKED,
}

/** 交互类型 */
enum class DrivingOperation {
    /** 浏览、翻页 */
    BROWSE,

    /** 输入文字、搜索 */
    INPUT_TEXT,

    /** 播放视频 */
    VIDEO,
}

/** 限制阈值,按车型标定 */
data class DrivingRestrictionConfig(
    /** 超过该车速进入简单模式(km/h) */
    val simpleAboveKph: Double = 5.0,
    /** 超过该车速完全锁定(km/h) */
    val lockedAboveKph: Double = 20.0,
) {
    init {
        require(simpleAboveKph <= lockedAboveKph) {
            "simpleAboveKph($simpleAboveKph) 不能大于 lockedAboveKph($lockedAboveKph)"
        }
    }
}

/**
 * desc: 根据车速判断驾驶限制。
 *
 * 纯策略,不依赖信号来源;调用方把车速传进来即可。
 *
 * 注意:
 * - 车速未知(null/NaN)时按不受限处理,避免信号丢失把界面全锁死;
 * - 真要做安全兜底,应该在信号层把 UNKNOWN 转成明确的 LOCKED 再传进来;
 * - 阈值只是默认值,量产必须按车型法规和标定表调整。
 */
object DrivingRestriction {

    /** 按车速计算限制等级 */
    fun levelOf(
        speedKph: Double?,
        config: DrivingRestrictionConfig = DrivingRestrictionConfig(),
    ): DrivingRestrictionLevel {
        if (speedKph == null || speedKph.isNaN()) return DrivingRestrictionLevel.NONE

        return when {
            speedKph <= config.simpleAboveKph -> DrivingRestrictionLevel.NONE
            speedKph <= config.lockedAboveKph -> DrivingRestrictionLevel.SIMPLE
            else -> DrivingRestrictionLevel.LOCKED
        }
    }

    /** 指定等级下是否允许某类交互 */
    fun isAllowed(
        operation: DrivingOperation,
        level: DrivingRestrictionLevel,
    ): Boolean = when (level) {
        DrivingRestrictionLevel.NONE -> true
        DrivingRestrictionLevel.SIMPLE -> operation == DrivingOperation.BROWSE
        DrivingRestrictionLevel.LOCKED -> false
    }

    /** 按车速直接判断是否允许某类交互 */
    fun isAllowed(
        operation: DrivingOperation,
        speedKph: Double?,
        config: DrivingRestrictionConfig = DrivingRestrictionConfig(),
    ): Boolean = isAllowed(operation, levelOf(speedKph, config))
}
