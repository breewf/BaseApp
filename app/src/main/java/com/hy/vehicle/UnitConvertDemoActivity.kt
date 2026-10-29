package com.hy.vehicle

import android.os.Bundle
import android.view.LayoutInflater
import com.hy.basic.BaseViewModel
import com.hy.basic.skin.BaseSkinActivity
import com.hy.basic.vehicle.unit.barToKpa
import com.hy.basic.vehicle.unit.celsiusToFahrenheit
import com.hy.basic.vehicle.unit.fahrenheitToCelsius
import com.hy.basic.vehicle.unit.formatWithUnit
import com.hy.basic.vehicle.unit.kmToMeters
import com.hy.basic.vehicle.unit.kphToMph
import com.hy.basic.vehicle.unit.kphToMps
import com.hy.basic.vehicle.unit.kpaToBar
import com.hy.basic.vehicle.unit.kpaToPsi
import com.hy.basic.vehicle.unit.metersToKm
import com.hy.basic.vehicle.unit.millivoltToVolt
import com.hy.basic.vehicle.unit.mpsToKph
import com.hy.basic.vehicle.unit.normalizeDegrees
import com.hy.basic.vehicle.unit.psiToKpa
import com.hy.basic.vehicle.unit.tenthCelsiusToCelsius
import com.hy.basic.vehicle.unit.tenthKphToKph
import com.hy.basic.vehicle.unit.tenthPercentToPercent
import com.hy.basic.vehicle.unit.toSignedDegrees
import com.hy.basic.vehicle.unit.whToKwh
import com.hy.myapp.databinding.ActivityUnitConvertDemoBinding

/** 单位换算示例页。 */
class UnitConvertDemoActivity :
    BaseSkinActivity<ActivityUnitConvertDemoBinding, BaseViewModel>() {

    /** 显示基类 Toolbar */
    override val showToolBar: Boolean get() = true

    /** Toolbar 标题 */
    override val toolbarTitle: CharSequence get() = "单位换算"

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater) =
        ActivityUnitConvertDemoBinding.inflate(inflater)

    /** 把换算结果直接铺到页面上 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.tvResult.text = buildString {
            appendLine("车速")
            appendLine("  60 km/h = ${60.0.kphToMps().formatWithUnit("m/s", 2)}")
            appendLine("  16.67 m/s = ${16.6667.mpsToKph().formatWithUnit("km/h", 2)}")
            appendLine("  60 km/h = ${60.0.kphToMph().formatWithUnit("mph", 2)}")
            appendLine("  raw 605 (0.1 km/h) = ${605.tenthKphToKph().formatWithUnit("km/h", 1)}")
            appendLine()
            appendLine("温度")
            appendLine("  raw 350 (0.1℃) = ${350.tenthCelsiusToCelsius().formatWithUnit("℃", 1)}")
            appendLine("  35℃ = ${35.0.celsiusToFahrenheit().formatWithUnit("℉", 1)}")
            appendLine("  95℉ = ${95.0.fahrenheitToCelsius().formatWithUnit("℃", 1)}")
            appendLine()
            appendLine("压力")
            appendLine("  240 kPa = ${240.0.kpaToBar().formatWithUnit("bar", 2)}")
            appendLine("  240 kPa = ${240.0.kpaToPsi().formatWithUnit("psi", 1)}")
            appendLine("  2.4 bar = ${2.4.barToKpa().formatWithUnit("kPa", 0)}")
            appendLine("  34.8 psi = ${34.8.psiToKpa().formatWithUnit("kPa", 1)}")
            appendLine()
            appendLine("电量 / 电压")
            appendLine("  15000 Wh = ${15000.whToKwh().formatWithUnit("kWh", 1)}")
            appendLine("  raw 865 (0.1%) = ${865.tenthPercentToPercent().formatWithUnit("%", 1)}")
            appendLine("  raw 13800 mV = ${13800.millivoltToVolt().formatWithUnit("V", 1)}")
            appendLine()
            appendLine("距离 / 角度")
            appendLine("  450 m = ${450.0.metersToKm().formatWithUnit("km", 3)}")
            appendLine("  0.45 km = ${0.45.kmToMeters().formatWithUnit("m", 0)}")
            appendLine("  -30° 归一化 = ${(-30.0).normalizeDegrees().formatWithUnit("°", 0)}")
            appendLine("  210° 转有符号 = ${210.0.toSignedDegrees().formatWithUnit("°", 0)}")
            appendLine()
            appendLine("精度和 offset 以 DBC 为准,这里只做量纲换算。")
        }
    }
}
