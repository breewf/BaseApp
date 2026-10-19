package com.hy.myapp.demo.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.hy.basic.dialog.BaseDialogFragment
import com.hy.basic.hint.ToastKit
import com.hy.myapp.R
import com.hy.myapp.databinding.DialogRateCardBinding
import com.hy.myapp.demo.model.DemoLogViewModel

/**
 * desc: 贴底卡片示例。gravity=BOTTOM + widthRatio=1f:宽度铺满、高度随内容、贴在屏幕底部。
 * 卡片皮肤把 windowIsFloating 关成 false,基类在比例为 1f 时直接给窗口 MATCH_PARENT,
 * 两侧不会留缝。返回键拦截:没选星级不放行,选了交回基类正常关闭。
 */
class RateCardDialogFragment : BaseDialogFragment<DialogRateCardBinding>() {

    /** 贴到屏幕底部(卡片皮肤:透明背景 + 蒙层) */
    override val gravity: Int get() = Gravity.BOTTOM

    /** 贴底卡片占满整行宽度 */
    override val widthRatio: Float get() = 1f

    private val hostLog: DemoLogViewModel by lazy {
        ViewModelProvider(requireActivity())[DemoLogViewModel::class.java]
    }

    private var stars = 0

    // 不用 by lazy:lazy 会把旧 View 缓存下来,转屏后仍指向已销毁的控件
    private val starViews: List<TextView>
        get() = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogRateCardBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        starViews.forEachIndexed { index, star ->
            star.setOnClickListener { setStars(index + 1) }
        }
        binding.btnSubmit.setOnClickListener {
            hostLog.log("贴底卡片 · 提交评分 $stars 星")
            dismissWithAnimation()
        }
        binding.btnLater.setOnClickListener {
            hostLog.log("贴底卡片 · 以后再说")
            dismissWithAnimation()
        }
    }

    override fun initData() {
        setStars(0)
        hostLog.log("贴底卡片 · 已弹出(没选星级时按返回键试试)")
    }

    /** 未选星级不放行;选了就交回基类按 cancelable 关闭 */
    override fun onBackPressed(): Boolean {
        if (stars > 0) return false
        ToastKit.show("先给个星级，或点「以后再说」")
        hostLog.log("贴底卡片 · 返回键被拦截(未选星级)")
        return true
    }

    override fun onDialogDismiss() {
        hostLog.log("贴底卡片 · 已关闭(向底部滑出)")
    }

    private fun setStars(count: Int) {
        stars = count
        starViews.forEachIndexed { index, star ->
            val lit = index < count
            star.text = if (lit) "★" else "☆"
            star.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (lit) R.color.color_ff8a21 else R.color.color_c7c7c7,
                ),
            )
        }
        binding.tvRate.text = if (count == 0) "还没选星级" else "$count 星 · ${wordOf(count)}"
        binding.btnSubmit.isEnabled = count > 0
    }

    /** 枚举/档位 -> 文案;兜底分支直接把原值打出来,不猜也不崩 */
    private fun wordOf(count: Int): String = when (count) {
        1 -> "很不满意"
        2 -> "不太满意"
        3 -> "一般"
        4 -> "满意"
        5 -> "非常满意"
        else -> "档位异常($count)"
    }
}
