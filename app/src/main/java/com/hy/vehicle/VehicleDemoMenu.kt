package com.hy.vehicle

import com.hy.demo.model.DemoEntry
import com.hy.demo.model.DemoGroup
import com.hy.demo.model.DemoRow

/**
 * desc: 车机能力验证菜单。
 *
 * 分组按"纯协议工具"和"系统能力"分开,后面加新页面在这里登记一行即可。
 */
object VehicleDemoMenu {

    /** 分组 -> 扁平列表行(标题行 + 入口行) */
    fun rows(): List<DemoRow> = groups().flatMap { group ->
        listOf(DemoRow.Header(group.title)) + group.entries.map { DemoRow.Entry(it) }
    }

    /** 车机能力分组 */
    private fun groups() = listOf(
        DemoGroup(
            "协议解析 · 纯 JVM",
            listOf(
                DemoEntry(
                    "字节与校验",
                    "位域 / 大小端 / BCD · 和校验与 CRC8/16/32",
                    ByteChecksumDemoActivity::class.java,
                ),
                DemoEntry(
                    "CAN 信号解析",
                    "Intel/Motorola · factor/offset · 无效值判定",
                    CanSignalDemoActivity::class.java,
                ),
                DemoEntry(
                    "单位换算",
                    "车速 / 温度 / 压力 / 电量等常用量纲",
                    UnitConvertDemoActivity::class.java,
                ),
            ),
        ),
        DemoGroup(
            "系统能力",
            listOf(
                DemoEntry(
                    "车机环境",
                    "多屏信息 · 存储卷 · 亮灭屏 · 方控按键",
                    VehicleEnvDemoActivity::class.java,
                ),
                DemoEntry(
                    "车型配置",
                    "MMKV 功能开关 · 变更通知 · 持久化",
                    VehicleConfigDemoActivity::class.java,
                ),
            ),
        ),
    )
}
