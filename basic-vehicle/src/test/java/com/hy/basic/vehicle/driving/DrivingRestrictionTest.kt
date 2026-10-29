package com.hy.basic.vehicle.driving

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 驾驶限制策略回归测试：等级和操作权限。 */
class DrivingRestrictionTest {

    /** 默认阈值下三档等级 */
    @Test
    fun `车速对应限制等级`() {
        assertEquals(DrivingRestrictionLevel.NONE, DrivingRestriction.levelOf(0.0))
        assertEquals(DrivingRestrictionLevel.SIMPLE, DrivingRestriction.levelOf(10.0))
        assertEquals(DrivingRestrictionLevel.LOCKED, DrivingRestriction.levelOf(30.0))
    }

    /** 简单模式只允许浏览 */
    @Test
    fun `简单模式限制输入和视频`() {
        assertTrue(DrivingRestriction.isAllowed(DrivingOperation.BROWSE, DrivingRestrictionLevel.SIMPLE))
        assertFalse(DrivingRestriction.isAllowed(DrivingOperation.INPUT_TEXT, DrivingRestrictionLevel.SIMPLE))
        assertFalse(DrivingRestriction.isAllowed(DrivingOperation.VIDEO, DrivingRestrictionLevel.SIMPLE))
    }

    /** 车速未知或非法时按安全策略锁定 */
    @Test
    fun `车速未知按锁定处理`() {
        assertEquals(DrivingRestrictionLevel.LOCKED, DrivingRestriction.levelOf(null))
        assertEquals(DrivingRestrictionLevel.LOCKED, DrivingRestriction.levelOf(Double.NaN))
        assertEquals(DrivingRestrictionLevel.LOCKED, DrivingRestriction.levelOf(-1.0))
    }

    /** 允许按车型配置未知车速时的策略 */
    @Test
    fun `可覆盖未知车速等级`() {
        val config = DrivingRestrictionConfig(unknownLevel = DrivingRestrictionLevel.SIMPLE)

        assertEquals(DrivingRestrictionLevel.SIMPLE, DrivingRestriction.levelOf(null, config))
    }
}
