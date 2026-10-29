package com.hy.basic.vehicle.driving

/** 驾驶限制等级 */
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
 * 驾驶限制策略：按车速给出 NONE/SIMPLE/LOCKED。
 *
 * 用法：
 * `DrivingRestriction.levelOf(speedKph)` 取等级，再调用 `isAllowed(operation, level)`。
 * 注意：纯策略，不依赖信号来源；阈值需按车型标定，车速未知时返回 NONE。
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
