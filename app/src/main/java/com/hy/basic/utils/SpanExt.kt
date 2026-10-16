@file:JvmName("SpanExt")

package com.hy.basic.utils

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.Layout.Alignment
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt

/**
 * desc: 富文本工具 —— 基于 Spannable 主流写法的链式 / 范围 / 关键词封装
 *
 * Android 富文本的官方底座就是 Spannable(Factory):给一段文本的某个区间挂上各种 Span
 * (StyleSpan / UnderlineSpan / ForegroundColorSpan / ClickableSpan …)。本类把繁琐的
 * `setSpan + 下标计算`收口成三类用法,页面代码只表达意图:
 * - 链式(整段):`"加粗".bold().red()` —— 返回 [SpannableStringBuilder],可继续 append 混排;
 * - 范围:`sb.styleWords("重点", listOf("重点"))` / `sb.linkAt("详情", url){ ... }`;
 * - 关键词高亮:[highlight] —— 搜索词标红等场景;
 * - 交互:[enableLinkTouch] 打开链接点击;[CollapsibleText] 长文本按最大行数展开/折叠。
 *
 * 说明:字号提供 [relativeSize](相对倍数)与 [absoluteSizeSp](绝对大小,按 sp 处理)。
 */

// ==== 整段链式:每个方法把 span 作用于接收者全文并返回可继续拼接的构建器 ====

/** 粗体 */
fun CharSequence.bold(): SpannableStringBuilder = wholeSpanned(StyleSpan(Typeface.BOLD))

/** 斜体 */
fun CharSequence.italic(): SpannableStringBuilder = wholeSpanned(StyleSpan(Typeface.ITALIC))

/** 粗斜体 */
fun CharSequence.boldItalic(): SpannableStringBuilder = wholeSpanned(StyleSpan(Typeface.BOLD_ITALIC))

/** 下划线 */
fun CharSequence.underline(): SpannableStringBuilder = wholeSpanned(UnderlineSpan())

/** 删除线 */
fun CharSequence.strike(): SpannableStringBuilder = wholeSpanned(StrikethroughSpan())

/** 前景色(文字颜色) */
fun CharSequence.textColor(@ColorInt color: Int): SpannableStringBuilder =
    wholeSpanned(ForegroundColorSpan(color))

/** 背景色 */
fun CharSequence.backgroundColor(@ColorInt color: Int): SpannableStringBuilder =
    wholeSpanned(BackgroundColorSpan(color))

/** 相对字号:[scale] 为倍数,如 1.5f 表示放大到 1.5 倍 */
fun CharSequence.relativeSize(scale: Float): SpannableStringBuilder =
    wholeSpanned(RelativeSizeSpan(scale))

/** 绝对字号:[sizeSp] 单位按 sp 处理 */
fun CharSequence.absoluteSizeSp(sizeSp: Float): SpannableStringBuilder =
    wholeSpanned(AbsoluteSizeSpan(sizeSp.toInt(), true))

/** 上标(如 x²),自动缩小字号 */
fun CharSequence.superscript(): SpannableStringBuilder =
    SpannableStringBuilder(this).apply {
        setSpan(SuperscriptSpan(), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(RelativeSizeSpan(0.75f), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

/** 下标(如 H₂O),自动缩小字号 */
fun CharSequence.subscript(): SpannableStringBuilder =
    SpannableStringBuilder(this).apply {
        setSpan(SubscriptSpan(), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        setSpan(RelativeSizeSpan(0.75f), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

/** 项目符号(左侧圆点),[gapWidth] 为像素间距 */
fun CharSequence.bullet(gapWidth: Int = 20): SpannableStringBuilder =
    wholeSpanned(BulletSpan(gapWidth))

// ==== 可点击链接 ====

/**
 * 把整段文字变成可点击链接。
 * @param color 链接颜色;null 则用文本默认色
 * @param underLine 是否带下划线,默认无(更符合现代 UI)
 * @param onClick 点击回调
 */
fun CharSequence.link(
    @ColorInt color: Int? = null,
    underLine: Boolean = false,
    onClick: () -> Unit,
): SpannableStringBuilder = wholeSpanned(ClickableTextSpan(color, underLine) { onClick() })

/**
 * 在构建器里给关键字 [keyword] 的首个匹配挂上可点击链接(常用于把"查看详情""www.xx.com"变成链接)。
 * @return 是否找到并成功挂上
 */
fun SpannableStringBuilder.linkAt(
    keyword: String,
    @ColorInt color: Int? = null,
    underLine: Boolean = false,
    onClick: () -> Unit,
): Boolean {
    val start = indexOf(keyword)
    if (start < 0) return false
    setSpan(ClickableTextSpan(color, underLine) { onClick() }, start, start + keyword.length,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return true
}

// ==== 范围 / 关键词 ====

/** 给关键字 [keyword](首个匹配)整段应用前景色 */
fun SpannableStringBuilder.colorWord(keyword: String, @ColorInt color: Int): Boolean {
    val start = indexOf(keyword)
    if (start < 0) return false
    setSpan(ForegroundColorSpan(color), start, start + keyword.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    return true
}

/** 批量高亮 [keywords] 中出现的所有位置(搜索命中、敏感词标红等) */
fun SpannableStringBuilder.highlightWords(
    vararg keywords: String,
    @ColorInt color: Int = Color.RED,
    background: Int? = null,
): SpannableStringBuilder {
    for (kw in keywords) {
        var from = 0
        while (true) {
            val start = indexOf(kw, from)
            if (start < 0) break
            val end = start + kw.length
            setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            background?.let {
                setSpan(BackgroundColorSpan(it), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            from = end
        }
    }
    return this
}

/** 便捷构建:`richText { appendSpanned("普通 "); appendSpanned("加粗".bold()) }` */
inline fun richText(builder: SpannableStringBuilder.() -> Unit): SpannableStringBuilder =
    SpannableStringBuilder().apply(builder)

/**
 * 追加一段带 span 的富文本,并把源文本上的 span 原样搬运到新位置。
 *
 * 注意:`SpannableStringBuilder.append(Spanned)` 只有 Android 12(API 31)+ 才会拷贝源 span,
 * 低版本只会追加纯字符、样式全部丢失;混排段落统一走本扩展保证全版本一致。
 */
fun SpannableStringBuilder.appendSpanned(text: CharSequence): SpannableStringBuilder {
    val start = length
    append(text)
    if (text is Spanned) {
        val spans = text.getSpans(0, text.length, Any::class.java)
        for (span in spans) {
            setSpan(
                span,
                start + text.getSpanStart(span),
                start + text.getSpanEnd(span),
                text.getSpanFlags(span),
            )
        }
    }
    return this
}

// ==== TextView 交互 ====

/**
 * 开启链接点击:设置 [LinkMovementMethod] 并把按下时的灰色高亮透明化,避免点击文字出现难看的灰底。
 * 只有文本里存在 [ClickableSpan] 时才需要调用。
 */
fun TextView.enableLinkTouch() {
    movementMethod = LinkMovementMethod.getInstance()
    highlightColor = Color.TRANSPARENT
}

// ==== 内部工具 ====

/** 把 span 作用于全文,返回可继续 append 的构建器 */
private fun CharSequence.wholeSpanned(span: Any): SpannableStringBuilder =
    SpannableStringBuilder(this).apply {
        setSpan(span, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

private fun SpannableStringBuilder.indexOf(keyword: String, from: Int = 0): Int =
    toString().indexOf(keyword, from)

/** 自定义可点击 Span:可控颜色 / 下划线,去掉系统默认蓝色 */
class ClickableTextSpan(
    @ColorInt private val color: Int?,
    private val underLine: Boolean,
    private val onClick: () -> Unit,
) : ClickableSpan() {

    override fun onClick(widget: View) = onClick()

    override fun updateDrawState(ds: TextPaint) {
        if (color != null) ds.color = color
        ds.isUnderlineText = underLine
    }
}

/**
 * desc: 长文本"按最大行数展开 / 折叠"助手 —— 富文本里最常见的正文折叠需求
 *
 * 用法:
 * ```
 * val collapsible = CollapsibleText(tvContent).apply { bind(longText, maxLines = 3) }
 * // 动态改行数:collapsible.setMaxLines(5)
 * ```
 * 折叠时取前 [maxLines] 行,末尾保留可点击的 [collapseLabel](如"…展开全部"),
 * 点击后展开全部并显示 [expandLabel](如"收起");宽度未就绪会自动重试,适配进入动画/旋转重测。
 *
 * 关键点:折叠文案用 [StaticLayout] 精确测量真实行数与每行可容纳字符,回退字符直到
 * "已裁正文 + 折叠标签"能放进最后一行,避免标签被再次省略号截断。
 */
@SuppressLint("StaticFieldLeak")
class CollapsibleText(
    private val textView: TextView,
) {
    private var full: CharSequence = ""
    private var maxLines: Int = DEFAULT_MAX_LINES
    private var collapseLabel: CharSequence = "…展开全部"
    private var expandLabel: CharSequence = " 收起"

    @ColorInt
    private var labelColor: Int = DEFAULT_LABEL_COLOR

    private var expanded = false
    private var collapsible = false

    /** 绑定文本(可选同时设定行数),会立即渲染 */
    fun bind(
        text: CharSequence,
        lines: Int = maxLines,
        collapseLabel: CharSequence = this.collapseLabel,
        expandLabel: CharSequence = this.expandLabel,
        @ColorInt labelColor: Int = this.labelColor,
    ): CollapsibleText = apply {
        full = text
        maxLines = lines
        this.collapseLabel = collapseLabel
        this.expandLabel = expandLabel
        this.labelColor = labelColor
        expanded = false
        render()
    }

    /** 动态改变折叠行数:重置为折叠态并按新行数重新测量渲染 */
    fun setMaxLines(lines: Int) = apply {
        maxLines = lines.coerceAtLeast(1)
        expanded = false
        render()
    }

    /** 手动切换展开/折叠 */
    fun toggle() = apply {
        if (collapsible) {
            expanded = !expanded
            render()
        }
    }

    val isExpanded: Boolean get() = expanded

    private fun render() {
        val tv = textView
        tv.enableLinkTouch()
        val width = tv.width - tv.paddingLeft - tv.paddingRight
        if (width <= 0) {
            // 宽度尚未就绪(post 下一帧再测),避免首帧测量为 0 导致误判
            tv.post { render() }
            return
        }
        val layout = StaticLayout.Builder
            .obtain(full, 0, full.length, tv.paint, width)
            .setAlignment(Alignment.ALIGN_NORMAL)
            .setLineSpacing(tv.lineSpacingExtra, tv.lineSpacingMultiplier)
            .build()
        collapsible = layout.lineCount > maxLines
        when {
            !collapsible -> {
                tv.maxLines = Integer.MAX_VALUE
                tv.text = full
            }

            expanded -> {
                tv.maxLines = Integer.MAX_VALUE
                val sb = SpannableStringBuilder(full)
                // 用 appendSpanned 保证折叠标签的 ClickableSpan 在 Android 12 以下也不丢失
                sb.appendSpanned(clickableLabel(expandLabel))
                tv.text = sb
            }

            else -> {
                tv.maxLines = maxLines
                val lastLine = maxLines - 1
                val lineStart = layout.getLineStart(lastLine)
                var end = layout.getLineEnd(lastLine)
                val labelWidth = tv.paint.measureText(collapseLabel.toString())
                // 从最后一行末尾逐个回退,直到"正文残留 + 折叠标签"能塞进整行宽度
                while (end > lineStart &&
                    tv.paint.measureText(full, lineStart, end) + labelWidth > width
                ) {
                    end--
                }
                val sb = SpannableStringBuilder(full.subSequence(0, end))
                sb.appendSpanned(clickableLabel(collapseLabel))
                tv.text = sb
            }
        }
    }

    private fun clickableLabel(label: CharSequence): SpannableString {
        val s = SpannableString(label)
        s.setSpan(
            ClickableTextSpan(labelColor, underLine = false) { toggle() },
            0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        return s
    }

    companion object {
        private const val DEFAULT_MAX_LINES = 3
        private const val DEFAULT_LABEL_COLOR = 0xFF3F51B5.toInt() // colorPrimary
    }
}
