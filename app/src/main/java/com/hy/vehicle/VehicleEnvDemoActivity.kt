package com.hy.vehicle

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseViewModel
import com.hy.basic.skin.BaseSkinActivity
import com.hy.basic.vehicle.display.displayInfoList
import com.hy.basic.vehicle.input.isConfirmKey
import com.hy.basic.vehicle.input.isDirectionalKey
import com.hy.basic.vehicle.input.isFromRotaryEncoder
import com.hy.basic.vehicle.input.isMediaKey
import com.hy.basic.vehicle.input.isRotaryScroll
import com.hy.basic.vehicle.input.isVolumeKey
import com.hy.basic.vehicle.input.keyName
import com.hy.basic.vehicle.input.rotaryDelta
import com.hy.basic.vehicle.power.ScreenStateMonitor
import com.hy.basic.vehicle.power.observeScreenState
import com.hy.basic.vehicle.storage.externalVolumes
import com.hy.basic.vehicle.storage.toReadableSize
import com.hy.myapp.databinding.ActivityVehicleEnvDemoBinding
import kotlinx.coroutines.launch

/**
 * 车机环境信息示例页：多屏、存储卷、亮灭屏、按键/旋钮。
 *
 * 本页会拦截除返回键外的按键事件。
 */
class VehicleEnvDemoActivity :
    BaseSkinActivity<ActivityVehicleEnvDemoBinding, BaseViewModel>() {

    /** 显示基类 Toolbar */
    override val showToolBar: Boolean get() = true

    /** Toolbar 标题 */
    override val toolbarTitle: CharSequence get() = "车机环境"

    /** 最近按键记录,最多保留 6 条 */
    private val keyLines = mutableListOf<String>()

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater) =
        ActivityVehicleEnvDemoBinding.inflate(inflater)

    /** 绑定刷新按钮并铺首屏 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.btnRefresh.setOnClickListener { render() }
        render()
    }

    /** 订阅亮灭屏状态 */
    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                this@VehicleEnvDemoActivity.observeScreenState()
                    .collect { on -> renderPower(on) }
            }
        }
    }

    /** 回到前台时补读一次状态 */
    override fun onResume() {
        super.onResume()
        renderPower(ScreenStateMonitor.isScreenOn(this))
    }

    /** 刷新全部环境信息 */
    private fun render() {
        renderDisplays()
        renderStorage()
        renderPower(ScreenStateMonitor.isScreenOn(this))
    }

    /** 刷新显示屏列表 */
    private fun renderDisplays() {
        val displays = displayInfoList()
        binding.tvDisplay.text = if (displays.isEmpty()) {
            "未取到显示屏信息"
        } else {
            buildString {
                displays.forEach { display ->
                    appendLine("屏 ${display.id}${if (display.isPrimary) " · 主屏" else ""}")
                    appendLine("  ${display.widthPx}x${display.heightPx} @${display.densityDpi}dpi")
                    appendLine("  ${display.name}")
                }
            }
        }
    }

    /** 刷新存储卷列表 */
    private fun renderStorage() {
        val volumes = externalVolumes()
        binding.tvStorage.text = if (volumes.isEmpty()) {
            "未发现可用存储卷"
        } else {
            buildString {
                volumes.forEach { volume ->
                    appendLine(
                        "${volume.label}  ${if (volume.removable) "可移动" else "内置"}  " +
                            (if (volume.mounted) "已挂载" else "未挂载")
                    )
                    appendLine("  ${volume.root.absolutePath}")
                    appendLine(
                        "  可用 ${volume.freeBytes.toReadableSize()} / " +
                            "共 ${volume.totalBytes.toReadableSize()}"
                    )
                }
            }
        }
    }

    /** 刷新亮灭屏文案 */
    private fun renderPower(on: Boolean) {
        binding.tvPower.text = if (on) "亮屏 / 可交互" else "灭屏 / 可能已休眠"
    }

    /** 捕获方控/键盘按键,返回键放行 */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode != KeyEvent.KEYCODE_BACK) {
            appendKey(
                "${event.keyName()}  repeat=${event.repeatCount}  " +
                    "media=${event.isMediaKey()}  volume=${event.isVolumeKey()}  " +
                    "dpad=${event.isDirectionalKey()}  confirm=${event.isConfirmKey()}"
            )
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /** 捕获旋钮滚动 */
    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        if (event.isRotaryScroll()) {
            appendKey("旋钮 Δ=${event.rotaryDelta()}  rotarySource=${event.isFromRotaryEncoder()}")
            return true
        }
        return super.onGenericMotionEvent(event)
    }

    /** 追加按键记录并限制行数 */
    private fun appendKey(line: String) {
        keyLines.add(0, line)
        while (keyLines.size > 6) keyLines.removeAt(keyLines.lastIndex)
        binding.tvKeyLog.text = keyLines.joinToString("\n")
    }
}
