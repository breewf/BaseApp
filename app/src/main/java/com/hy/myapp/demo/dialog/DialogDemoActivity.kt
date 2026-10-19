package com.hy.myapp.demo.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.EditText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseActivity
import com.hy.basic.dialog.DialogOptions
import com.hy.basic.dialog.showConfirm
import com.hy.basic.dialog.showCustomDialog
import com.hy.basic.dialog.showDialog
import com.hy.basic.dialog.showItems
import com.hy.basic.dialog.showMultiChoice
import com.hy.basic.dialog.showSingleChoice
import com.hy.basic.dialog.showTip
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivityDialogDemoBinding
import com.hy.myapp.demo.model.DemoLogViewModel
import kotlinx.coroutines.launch

/**
 * desc: 弹框封装用法示例。showDialog/showCustomDialog 吃同一份 DialogOptions,换函数就是换皮。
 * 常用形态有一行版:showTip/showConfirm/showItems/showSingleChoice/showMultiChoice。
 */
class DialogDemoActivity : BaseActivity<ActivityDialogDemoBinding, DemoLogViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "Dialog 弹窗"

    private val payWays = listOf("余额支付", "微信支付", "支付宝", "货到付款")

    private val tags = listOf("常用", "新品", "清仓", "赠品", "已下架")

    /** 单选结果,下次打开时作为默认选中项 */
    private var pickedWay: String? = null

    /** 多选结果,下次打开时作为默认勾选项 */
    private var checkedTags: Set<Int> = setOf(0, 2)

    override fun createBinding(inflater: LayoutInflater) =
        ActivityDialogDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        viewModel.header = "操作日志：下面每一条弹框的点法都会写在这里。"
        binding.btnConfirm.setOnClickListener { showSystemExitDialog() }
        binding.btnTip.setOnClickListener { showTipDemo() }
        binding.btnCustom.setOnClickListener { showCustomExitDialog() }
        binding.btnForce.setOnClickListener { showForceUpdateDialog() }
        binding.btnItems.setOnClickListener { showPayWayList() }
        binding.btnSingle.setOnClickListener { showSingleChoiceDemo() }
        binding.btnMulti.setOnClickListener { showMultiChoiceDemo() }
        binding.btnCustomInput.setOnClickListener { showCustomInputDemo() }
        binding.btnConfirmRow.setOnClickListener { showConfirmRowDemo() }
        binding.btnClearLog.setOnClickListener { viewModel.clear() }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logText.collect { binding.tvLog.text = it }
            }
        }
    }

    /** 一份配置抽成 DSL lambda,系统样式和自定义样式都能吃,第 1、3 条按钮弹的是同一个配置 */
    private val exitLoginConfig: DialogOptions.() -> Unit = {
        title = "退出登录"
        message = "退出后需要重新输入账号密码，当前页面未保存的内容会丢失。"
        confirm("退出") { viewModel.log("系统/自定义 · 点了「退出」") }
        cancel("再想想") { viewModel.log("系统/自定义 · 点了「再想想」") }
    }

    /** 1. 系统样式AlertDialog */
    private fun showSystemExitDialog() {
        showDialog(exitLoginConfig)
    }

    /** 3. 自定义样式:配置一字不改,只是换了函数 */
    private fun showCustomExitDialog() {
        showCustomDialog(exitLoginConfig)
    }

    /** 2. 单按钮提示:最常用的"说完就走" */
    private fun showTipDemo() {
        showTip(
            message = "本次同步已完成，共更新 12 条数据。",
            title = "同步完成",
            confirmText = "知道了",
        ) { viewModel.log("系统 · 提示已确认") }
    }

    /** 4. 强制更新:左对齐 + 返回键/点外部都关不掉 + 只有一个按钮 */
    private fun showForceUpdateDialog() {
        showCustomDialog {
            title = "发现新版本 v2.3.0"
            message =
                "· 重构弹窗与提示封装\n· 修复状态栏适配问题\n· 提升冷启动速度\n\n本次更新后才能继续使用。"
            contentGravity = Gravity.START
            cancelable = false
            cancelOnTouchOutside = false
            singleButton()
            confirm("立即更新") { viewModel.log("自定义 · 强制更新 -> 跳应用市场") }
        }
    }

    /** 5. 列表选择:点某项即关闭 */
    private fun showPayWayList() {
        showItems(title = "选择支付方式", items = payWays) { index, item ->
            pickedWay = item.toString()
            viewModel.log("系统 · 列表选中 [$index] $item")
        }
    }

    /** 6. 单选:打开时带上上次的选择 */
    private fun showSingleChoiceDemo() {
        showSingleChoice(
            title = "支付方式（单选）",
            items = payWays,
            checkedIndex = payWays.indexOf(pickedWay),
        ) { index, item ->
            pickedWay = item.toString()
            viewModel.log("系统 · 单选 [$index] $item")
        }
    }

    /** 7. 多选:点确定才提交,勾中的下标存起来供下次默认勾选 */
    private fun showMultiChoiceDemo() {
        showMultiChoice(
            title = "筛选标签（多选）",
            items = tags,
            checked = checkedTags
        ) { indices, selected ->
            checkedTags = indices.toSet()
            viewModel.log("系统 · 多选 -> ${selected.joinToString("、")}")
        }
    }

    /** 8. 自定义样式的扩展位一起用:customView 塞自己的 View,autoClose=false 校验不过留在弹框里 */
    private fun showCustomInputDemo() {
        val input =
            LayoutInflater.from(this).inflate(R.layout.dialog_input, null, false) as EditText
        input.hint = "请输入 11 位手机号"
        showCustomDialog {
            title = "绑定手机号"
            message = "格式不对时点确定不会关闭弹框。"
            customView = input
            autoClose = false
            confirm("提交") {
                val text = input.text?.toString()?.trim().orEmpty()
                if (text.length != 11) {
                    input.error = "请输入 11 位手机号"
                    viewModel.log("自定义 · 手机号格式不对，弹框保持打开")
                } else {
                    dismissDialog()
                    viewModel.log("自定义 · 绑定成功 -> $text")
                }
            }
            cancel("不绑了") {
                dismissDialog()
                viewModel.log("自定义 · 取消绑定")
            }
        }
    }

    /** 9. 不写 DSL 的行版:参数命名清楚,showTip/showConfirm/showItems 都是这一路 */
    private fun showConfirmRowDemo() {
        showConfirm(
            message = "删除后不可恢复。",
            title = "删除这条记录?",
            confirmText = "删除",
            cancelText = "再想想",
        ) { viewModel.log("系统 · 行版确认 -> 已删除") }
    }
}
