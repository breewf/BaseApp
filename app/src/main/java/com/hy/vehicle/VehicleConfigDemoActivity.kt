package com.hy.vehicle

import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseViewModel
import com.hy.basic.skin.BaseSkinActivity
import com.hy.basic.vehicle.config.VehicleConfig
import com.hy.myapp.databinding.ActivityVehicleConfigDemoBinding
import kotlinx.coroutines.launch

/**
 * desc: 车型功能开关验证页。
 *
 * 两个开关走 VehicleConfig,写入即落 MMKV,并通过 changed 通知页面刷新。
 * 杀掉进程再进来,状态还在;清空只删 vehicle_cfg_ 前缀的键,不动其它业务配置。
 */
class VehicleConfigDemoActivity :
    BaseSkinActivity<ActivityVehicleConfigDemoBinding, BaseViewModel>() {

    /** 显示基类 Toolbar */
    override val showToolBar: Boolean get() = true

    /** Toolbar 标题 */
    override val toolbarTitle: CharSequence get() = "车型配置"

    /** 信号仿真开关 key */
    private val simulationKey = "demo_signal_simulation"

    /** 行车限制开关 key */
    private val driveLockKey = "demo_drive_lock"

    /** 最近一次变更的 key */
    private var lastChangedKey = ""

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater) =
        ActivityVehicleConfigDemoBinding.inflate(inflater)

    /** 读默认值、绑开关、铺首屏 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.swSimulation.isChecked = VehicleConfig.boolean(simulationKey, false)
        binding.swDriveLock.isChecked = VehicleConfig.boolean(driveLockKey, true)

        binding.swSimulation.setOnCheckedChangeListener { _, checked ->
            VehicleConfig.putBoolean(simulationKey, checked)
        }
        binding.swDriveLock.setOnCheckedChangeListener { _, checked ->
            VehicleConfig.putBoolean(driveLockKey, checked)
        }
        binding.btnClear.setOnClickListener {
            VehicleConfig.clear()
            binding.swSimulation.isChecked = false
            binding.swDriveLock.isChecked = true
            render()
        }
        render()
    }

    /** 监听配置变更,收到后刷新快照 */
    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                VehicleConfig.changed.collect { key ->
                    lastChangedKey = key
                    render()
                }
            }
        }
    }

    /** 把当前配置和最近变更铺到页面上 */
    private fun render() {
        binding.tvSnapshot.text = buildString {
            appendLine("当前配置(MMKV 持久化)")
            appendLine("信号仿真: ${VehicleConfig.boolean(simulationKey, false)}")
            appendLine("行车限制: ${VehicleConfig.boolean(driveLockKey, true)}")
            appendLine()
            appendLine("已保存 key: ${VehicleConfig.keys().joinToString().ifBlank { "无" }}")
            appendLine("最近变更: ${lastChangedKey.ifBlank { "无" }}")
            appendLine()
            appendLine("清空只删 vehicle_cfg_ 前缀的键,不影响其它业务数据。")
        }
    }
}
