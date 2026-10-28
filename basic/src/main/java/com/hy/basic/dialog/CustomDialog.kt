package com.hy.basic.dialog

import android.content.Context
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import com.hy.basic.R

/**
 * desc: 自定义样式弹框 —— 圆角白底 + 标题(可选) + 正文(可选) + 内容插槽 + 两个按钮。
 *
 * 只消费 [DialogOptions],所以同一份配置既能弹系统样式也能弹这一种,换皮不换代码。
 *
 * 扩展方式:业务想要更彻底的自定义皮(顶部色条、图标、不同分割线……)时继承本类,
 * 只重写 [layoutRes] 返回自己的布局即可 —— 沿用约定 id
 * `tvDialogTitle` / `tvDialogMessage` / `flDialogContent` / `btnDialogCancel` /
 * `btnDialogConfirm` / `viewDialogButtonDivider`,
 * 布局里缺哪个 id 就跳过对应回填(不会崩),这点与 [com.hy.basic.page.PageStateLayout] 的状态层约定一致;
 * 还想额外绑定什么,重写 [bindExtra] 补一刀。
 */
open class CustomDialog(
    context: Context,
    protected val options: DialogOptions,
) : Dialog(context, R.style.DialogCustom) {

    /** 弹框布局,子类换成自己的皮 */
    protected open val layoutRes: Int get() = R.layout.dialog_custom

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
        setCancelable(options.cancelable)
        setCanceledOnTouchOutside(options.cancelOnTouchOutside)
        setOnDismissListener { options.onDismiss?.invoke() }
        bindOptions()
        bindExtra(findViewById(android.R.id.content) ?: window?.decorView)
        applyWindowLayout()
    }

    /** 按约定 id 回填配置:找不到对应控件就跳过,允许任意裁剪布局 */
    private fun bindOptions() {
        findViewById<TextView>(R.id.tvDialogTitle)?.apply {
            val text = options.title
            visibility = if (text == null) View.GONE else View.VISIBLE
            if (text != null) this.text = text
            gravity = options.contentGravity
        }
        findViewById<TextView>(R.id.tvDialogMessage)?.apply {
            val text = options.message
            visibility = if (text == null) View.GONE else View.VISIBLE
            if (text != null) this.text = text
            gravity = options.contentGravity
        }
        findViewById<FrameLayout>(R.id.flDialogContent)?.apply {
            val content = options.customView
            when {
                content == null -> visibility = View.GONE
                else -> {
                    removeAllViews()
                    addView(content)
                    visibility = View.VISIBLE
                }
            }
        }

        val confirm = findViewById<TextView>(R.id.btnDialogConfirm)
        val cancel = findViewById<TextView>(R.id.btnDialogCancel)
        val cancelText = options.cancelText
        cancel?.visibility = if (cancelText == null) View.GONE else View.VISIBLE
        if (cancelText != null) cancel?.text = cancelText
        findViewById<View>(R.id.viewDialogButtonDivider)?.visibility =
            if (cancelText == null) View.GONE else View.VISIBLE
        // 单按钮时铺满整行,左下也要圆角,否则按压态贴着卡片圆角处会露出直角缺口
        if (cancelText == null) {
            confirm?.setBackgroundResource(R.drawable.dialog_btn_single_bg)
        }

        confirm?.setOnClickListener {
            options.onConfirm?.invoke()
            if (options.autoClose) dismiss()
        }
        cancel?.setOnClickListener {
            options.onCancel?.invoke()
            if (options.autoClose) dismiss()
        }
    }

    /**
     * 宽度按 [DialogOptions.widthRatio] 占屏,并让输入法弹出时顶起弹框。
     * ADJUST_RESIZE 在 Activity 上因 edge-to-edge 被废弃,但 Dialog 是独立窗口,
     * 这里仍是成本最低、各版本表现最一致的写法。
     */
    @Suppress("DEPRECATION")
    private fun applyWindowLayout() {
        window?.apply {
            val screenWidth = context.resources.displayMetrics.widthPixels
            setLayout((screenWidth * options.widthRatio).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    /** 子类补充绑定钩子(自己的额外控件在这里接),默认空实现 */
    protected open fun bindExtra(root: View?) {}
}
