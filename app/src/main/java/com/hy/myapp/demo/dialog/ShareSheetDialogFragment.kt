package com.hy.myapp.demo.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hy.basic.dialog.BaseBottomSheetDialogFragment
import com.hy.basic.dialog.bottomSheetStateName
import com.hy.myapp.databinding.BottomSheetShareBinding
import com.hy.myapp.demo.model.DemoLogViewModel

/**
 * desc: Material 底部面板示例 —— 一个类用参数摆出两套 [BottomSheetBehavior] 配置,
 * 对应演示页的第 3、4 个按钮。
 *
 * - 第 3 条(默认形态):`peekRatio = 0.5f` 收起态露半屏 + `hideable = true`,
 *   手指往上拖能拉满,**往下拖到底自动关闭**(STATE_HIDDEN 由基类接成 dismiss);
 * - 第 4 条(钉住形态):`expanded = true` 一进来就满屏(此时 [fitToContents] = false,
 *   展开高度按屏幕而不是按内容,内部长文继续可滚)+ `hideable = false` + `draggable = false`
 *   面板钉在满屏,拖不动也下滑不掉,只能点选项或「取消」。
 *   再加 `fillHeight = true`([BaseBottomSheetDialogFragment.fillSheetHeight])
 *   把面板铺满可见区。
 *
 * 布局采用三段式:顶部拖拽条+标题固定 / 中间选项列表在 NestedScrollView 内可滚 /
 * 底部「取消」按钮固定——标题和按钮不跟随页面滚动。
 */
class ShareSheetDialogFragment : BaseBottomSheetDialogFragment<BottomSheetShareBinding>() {

    /** 收起态露出高度占屏比例;<=0 表示不设置,交给内容/fitToContents 决定 */
    private val peekRatio: Float get() = arguments?.getFloat(ARG_PEEK_RATIO) ?: 0f

    /** 是否一进来就展开到满屏 */
    private val startExpanded: Boolean get() = arguments?.getBoolean(ARG_EXPANDED) ?: false

    override val peekHeightPx: Int
        get() = if (peekRatio <= 0f) PEEK_UNSET else (resources.displayMetrics.heightPixels * peekRatio).toInt()

    override val initialState: Int
        get() = if (startExpanded) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED

    /** 满屏形态下按屏幕高度铺开,内容比屏幕高时才能继续在面板里滚 */
    override val fitToContents: Boolean get() = !startExpanded

    /** 拖到底是否关闭:true 时下滑到底会自动 dismiss(基类接的 STATE_HIDDEN) */
    override val hideable: Boolean get() = arguments?.getBoolean(ARG_HIDEABLE, true) ?: true

    /** 能否拖拽:钉住形态置 false,否则面板仍会被手指拽回收起态 */
    override val draggable: Boolean get() = arguments?.getBoolean(ARG_DRAGGABLE, true) ?: true

    /** 是否把面板铺满可见区(内容比屏幕矮时白底也能铺到屏幕底) */
    override val fillSheetHeight: Boolean get() = arguments?.getBoolean(ARG_FILL_HEIGHT, false) ?: false

    private val hostLog: DemoLogViewModel by lazy {
        ViewModelProvider(requireActivity())[DemoLogViewModel::class.java]
    }

    /**
     * 拖拽回调可能在 View 销毁前后各打到一次,这里抓一个可空引用刷新进度文案,
     * 避免在回调里直接摸 binding 触发"binding 仅在弹框显示期间可用"。
     */
    private var tvSlide: TextView? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        BottomSheetShareBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        tvSlide = binding.tvSlide
        val options = listOf(binding.opt1, binding.opt2, binding.opt3, binding.opt4)
        options.forEach { option ->
            option.setOnClickListener {
                hostLog.log("底部面板 · 选择「${option.text}」")
                dismissWithAnimation()
            }
        }
        binding.btnCancel.setOnClickListener {
            hostLog.log("底部面板 · 点「取消」下滑退场")
            dismissWithAnimation()
        }
    }

    override fun initData() {
        binding.tvExtra.text = buildString {
            append("这段长正文用来验证 fitToContents：\n")
            for (index in 1..22) {
                append("第 ").append(index).append(" 行 面板内容可以比屏幕还高,拉满之后内部继续滚动。\n")
            }
        }
        hostLog.log(
            "底部面板 · 已弹出 · peek=${(peekRatio * 100).toInt()}%" +
                " · 初始${bottomSheetStateName(initialState)} · 可下滑关闭=${hideable}" +
                " · 可拖拽=${draggable} · 铺满=${fillSheetHeight}",
        )
    }

    override fun onSheetStateChanged(state: Int) {
        hostLog.log("底部面板 · 状态 -> ${bottomSheetStateName(state)}")
    }

    /** 0 = 收起只露 peekHeight,1 = 完全展开;往隐藏方向继续拖会是负数,原样显示便于观察 */
    override fun onSheetSlide(slideOffset: Float) {
        tvSlide?.text = "滑动进度 ${(slideOffset * 100).toInt()}%（上拖展开 / 下拖关闭）"
    }

    override fun onDialogDismiss() {
        tvSlide = null
        hostLog.log("底部面板 · 已关闭")
    }

    companion object {
        private const val ARG_PEEK_RATIO = "arg_peek_ratio"
        private const val ARG_EXPANDED = "arg_expanded"
        private const val ARG_HIDEABLE = "arg_hideable"
        private const val ARG_DRAGGABLE = "arg_draggable"
        private const val ARG_FILL_HEIGHT = "arg_fill_height"

        /**
         * @param peekRatio 收起态露出高度占屏比例,0f = 不设置
         * @param expanded  是否一进来就展开满屏
         * @param hideable  是否允许下滑到底关闭
         * @param draggable 是否允许拖拽;钉住满屏的面板要传 false
         * @param fillHeight 是否把面板铺满可见区(铺满意就传 true)
         */
        fun newInstance(
            peekRatio: Float = 0.5f,
            expanded: Boolean = false,
            hideable: Boolean = true,
            draggable: Boolean = true,
            fillHeight: Boolean = false,
        ): ShareSheetDialogFragment = ShareSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putFloat(ARG_PEEK_RATIO, peekRatio)
                putBoolean(ARG_EXPANDED, expanded)
                putBoolean(ARG_HIDEABLE, hideable)
                putBoolean(ARG_DRAGGABLE, draggable)
                putBoolean(ARG_FILL_HEIGHT, fillHeight)
            }
        }
    }
}
