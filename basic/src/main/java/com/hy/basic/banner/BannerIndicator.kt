package com.hy.basic.banner

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.hy.basic.R
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * desc: 页码指示器,自己绘制,跟着手指走。Banner 内部已自动联动,
 * 引导页/轮播卡片想要独立指示器时可以直接摆进布局。
 *
 * 关键设计:只暴露 selection(当前页+滑动百分比),选中态是插值出来的,
 * 手指拖到一半圆点/滑块也就走一半。selection 允许越出端点一格,
 * 循环轮播时滑块滑出右边缘的同时从左边缘滑回,而不是瞬移。
 */
class BannerIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    /** 指示器样式,与 XML `indicator_style` 对应 */
    enum class Style(internal val attrValue: Int) {
        /** 圆点,选中点放大并换色 */
        DOTS(0),

        /** 小圆点 + 一个跟随滑动的胶囊滑块 */
        DOTS_SLIDER(1),

        /** 细进度条,长度按 (当前页+1)/总数 推进 */
        BAR(2),

        /** 数字 "2/5",页数很多时比圆点更清晰 */
        TEXT(3),
    }

    var style: Style = Style.DOTS
        set(value) {
            if (field == value) return
            field = value
            requestLayoutAndDraw()
        }

    /** 页总数。改变会重新测量(宽度依赖它) */
    var count: Int = 0
        set(value) {
            val v = value.coerceAtLeast(0)
            if (field == v) return
            field = v
            requestLayoutAndDraw()
        }

    /**
     * 当前位置:真实页下标+滑动百分比,允许小数。
     * 循环轮播在最后一页与第一页之间翻页时会越出端点一格,绘制时按环形处理。
     */
    var selection: Float = 0f
        set(value) {
            val v = if (count <= 1) 0f else value.coerceIn(-1f, count.toFloat())
            if (field == v) return
            field = v
            invalidate()
        }

    @ColorInt
    var dotColor: Int = ContextCompat.getColor(context, R.color.banner_indicator_dot)
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    var selectedColor: Int = ContextCompat.getColor(context, R.color.banner_indicator_selected)
        set(value) {
            field = value
            invalidate()
        }

    /** 圆点半径(px) */
    var dotRadius: Float = dp(3f)
        set(value) {
            field = value
            requestLayoutAndDraw()
        }

    /** 圆点间距(px) */
    var dotGap: Float = dp(6f)
        set(value) {
            field = value
            requestLayoutAndDraw()
        }

    /** BAR / 滑块 的粗细(px) */
    var barHeight: Float = dp(3f)
        set(value) {
            field = value
            requestLayoutAndDraw()
        }

    /** BAR 样式的轨道长度(px),默认 60dp;布局里给了宽度时以宽度为准 */
    var barTrackLength: Float = dp(60f)
        set(value) {
            field = value
            requestLayoutAndDraw()
        }

    /** 选中圆点相对普通圆点的放大倍数 */
    var selectedScale: Float = 1.35f
        set(value) {
            field = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(11f)
        textAlign = Paint.Align.CENTER
    }
    private val oval = RectF()
    private val colorEvaluator = ArgbEvaluator()

    init {
        attrs?.let { readAttrs(it) }
    }

    private fun readAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BannerIndicator)
        style = Style.entries.firstOrNull { it.attrValue == a.getInt(R.styleable.BannerIndicator_indicator_style, 0) }
            ?: Style.DOTS
        count = a.getInt(R.styleable.BannerIndicator_indicator_count, 0)
        dotColor = a.getColor(R.styleable.BannerIndicator_indicatorColor, dotColor)
        selectedColor = a.getColor(R.styleable.BannerIndicator_indicatorSelectedColor, selectedColor)
        dotRadius = a.getDimension(R.styleable.BannerIndicator_indicatorDotRadius, dotRadius)
        dotGap = a.getDimension(R.styleable.BannerIndicator_indicatorGap, dotGap)
        barHeight = a.getDimension(R.styleable.BannerIndicator_indicatorBarHeight, barHeight)
        barTrackLength = a.getDimension(R.styleable.BannerIndicator_indicatorBarLength, barTrackLength)
        selectedScale = a.getFloat(R.styleable.BannerIndicator_indicatorSelectedScale, selectedScale)
        textPaint.textSize = a.getDimension(R.styleable.BannerIndicator_indicatorTextSize, textPaint.textSize)
        val pad = a.getDimension(R.styleable.BannerIndicator_indicatorPadding, dp(4f))
        setPadding(pad.toInt(), pad.toInt(), pad.toInt(), pad.toInt())
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = desiredWidth(widthMeasureSpec)
        val h = desiredHeight(heightMeasureSpec)
        setMeasuredDimension(w, h)
    }

    private fun desiredWidth(spec: Int): Int {
        val content = when (style) {
            // 圆点行再加上两端要溢出去的那一截,少留一点就会被切掉边缘
            Style.DOTS, Style.DOTS_SLIDER -> dotRowWidth + sideBleed * 2f
            Style.BAR -> barTrackLength
            Style.TEXT -> textPaint.measureText("${count.coerceAtLeast(1)}/$count")
        }
        // 向上取整:浮点截断会白吃掉不到 1px,圆点边缘就糙了
        val wanted = ceil(content + paddingStart + paddingEnd).toInt()
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            MeasureSpec.AT_MOST -> wanted.coerceAtMost(MeasureSpec.getSize(spec))
            else -> wanted
        }
    }

    private fun desiredHeight(spec: Int): Int {
        val content = when (style) {
            Style.DOTS -> dotRadius * 2f * selectedScale
            Style.DOTS_SLIDER -> (dotRadius * 2f).coerceAtLeast(barHeight * 2f)
            Style.BAR -> barHeight * 2f
            Style.TEXT -> textPaint.textSize
        }
        val wanted = ceil(content + paddingTop + paddingBottom).toInt()
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            MeasureSpec.AT_MOST -> wanted.coerceAtMost(MeasureSpec.getSize(spec))
            else -> wanted
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (count <= 0) return
        val centerY = height / 2f
        when (style) {
            Style.DOTS -> drawDots(canvas, dotRowLeft, centerY)
            Style.DOTS_SLIDER -> drawDotsSlider(canvas, dotRowLeft, centerY)
            // 轨道和文字没有溢出概念,照旧铺满内容区
            Style.BAR -> drawBar(canvas, paddingStart.toFloat(), (width - paddingEnd).toFloat(), centerY)
            Style.TEXT -> drawText(canvas, paddingStart.toFloat(), (width - paddingEnd).toFloat(), centerY)
        }
    }

    /** 每个点按"离 selection 多远"插值出权重,颜色与半径一起过渡 */
    private fun drawDots(canvas: Canvas, left: Float, centerY: Float) {
        for (i in 0 until count) {
            val weight = weightOf(i)
            val cx = left + dotRadius + i * (dotRadius * 2f + dotGap)
            val r = dotRadius * (1f + (selectedScale - 1f) * weight)
            paint.color = blend(weight)
            canvas.drawCircle(cx, centerY, r, paint)
        }
    }

    /** 底色点保持不变,上面叠一个胶囊滑块跟着 selection 连续滑动 */
    private fun drawDotsSlider(canvas: Canvas, left: Float, centerY: Float) {
        paint.color = dotColor
        for (i in 0 until count) {
            val cx = left + dotRadius + i * (dotRadius * 2f + dotGap)
            canvas.drawCircle(cx, centerY, dotRadius, paint)
        }
        val step = dotRadius * 2f + dotGap
        val sliderWidth = step.coerceAtLeast(barHeight * 2f)
        val start = left + dotRadius - sliderWidth / 2f
        val last = (count - 1).toFloat()
        when {
            // 常规区间:一段滑块,位置就是 selection
            selection in 0f..last ->
                drawSlider(canvas, start + step * selection, centerY, sliderWidth, 1f)

            // 越过最后一格往第一页绕:右端滑出的同时左端滑入,两段透明度互补
            selection > last -> {
                val t = (selection - last).coerceIn(0f, 1f)
                drawSlider(canvas, start + step * (last + t), centerY, sliderWidth, 1f - t)
                drawSlider(canvas, start - step * (1f - t), centerY, sliderWidth, t)
            }

            // 从第一页往回越出端点:方向对称
            else -> {
                val t = (-selection).coerceIn(0f, 1f)
                drawSlider(canvas, start - step * t, centerY, sliderWidth, 1f - t)
                drawSlider(canvas, start + step * (last + 1f - t), centerY, sliderWidth, t)
            }
        }
    }

    /** 画一段滑块,[alpha] 给首尾绕回时用(一边淡出、一边淡入才看不出换端) */
    private fun drawSlider(canvas: Canvas, x: Float, centerY: Float, sliderWidth: Float, alpha: Float) {
        if (alpha <= 0.02f) return
        paint.color = selectedColor
        paint.alpha = (Color.alpha(selectedColor) * alpha).roundToInt()
        oval.set(x, centerY - barHeight, x + sliderWidth, centerY + barHeight)
        canvas.drawRoundRect(oval, barHeight, barHeight, paint)
    }

    private fun drawBar(canvas: Canvas, left: Float, right: Float, centerY: Float) {
        val trackWidth = (right - left).coerceAtLeast(1f)
        // 一格占轨道的几分之一:进度 = (游标 + 1) / count,第 1 页也有一格不会空着
        val unit = trackWidth / count
        oval.set(left, centerY - barHeight, right, centerY + barHeight)
        paint.color = dotColor
        canvas.drawRoundRect(oval, barHeight, barHeight, paint)
        val last = (count - 1).toFloat()
        when {
            // 常规区间:填充右边缘按小数游标连续推进
            selection in 0f..last -> drawBarFill(canvas, centerY, left, left + unit * (selection + 1f))

            // 首尾绕回。进度条是线性语义,绕回时不能靠钳位(那会先卡在满格再瞬移),
            // 改成擦除:满格从左边开始退场,首页那一格同时在左边长出来,两头都能接上
            selection > last -> {
                val t = (selection - last).coerceIn(0f, 1f)
                drawBarFill(canvas, centerY, left, left + unit * t)
                drawBarRange(canvas, centerY, left, right, left + trackWidth * t, right)
            }

            // 方向反过来就是铺满:从只剩一格,擦除线向左退成满格
            else -> {
                val t = (-selection).coerceIn(0f, 1f)
                drawBarFill(canvas, centerY, left, left + unit)
                drawBarRange(canvas, centerY, left, right, left, left + trackWidth * t)
            }
        }
    }

    /** 进度条的填充段,[from]..[to] 是横向范围 */
    private fun drawBarFill(canvas: Canvas, centerY: Float, from: Float, to: Float) {
        if (to - from <= 0f) return
        oval.set(from, centerY - barHeight, to, centerY + barHeight)
        paint.color = selectedColor
        canvas.drawRoundRect(oval, barHeight, barHeight, paint)
    }

    /** 只把满格画在 [keepFrom]..[keepTo] 这一段里,给绕回时的擦除过渡用 */
    private fun drawBarRange(
        canvas: Canvas,
        centerY: Float,
        from: Float,
        to: Float,
        keepFrom: Float,
        keepTo: Float,
    ) {
        if (keepTo - keepFrom <= 0f) return
        val save = canvas.save()
        canvas.clipRect(keepFrom, centerY - barHeight, keepTo, centerY + barHeight)
        drawBarFill(canvas, centerY, from, to)
        canvas.restoreToCount(save)
    }

    private fun drawText(canvas: Canvas, left: Float, right: Float, centerY: Float) {
        textPaint.color = selectedColor
        // 取最近的一格再按环形折算,游标越到 count 上时显示的仍是第 1 页
        val page = Math.floorMod(selection.roundToInt(), count) + 1
        val baseline = centerY - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText("$page/$count", (left + right) / 2f, baseline, textPaint)
    }

    /**
     * 第 index 页的“被选中程度”:1=完全在这一页,0=完全不在这页。
     * selection 越出端点时改用环形就近距离,实现交叉渐变。
     */
    private fun weightOf(index: Int): Float {
        val distance = abs(selection - index)
        val wrapped = selection < 0f || selection > count - 1
        val effective = if (wrapped) minOf(distance, count - distance) else distance
        return (1f - effective).coerceIn(0f, 1f)
    }

    private fun blend(weight: Float): Int =
        colorEvaluator.evaluate(weight, dotColor, selectedColor) as Int

    /** 滑块可以画到端点之外的余量(单侧),与 desiredWidth 里多留的那一段对应 */
    private val sliderOverhang: Float get() = if (count > 1) (dotRadius * 2f + dotGap) / 2f else 0f

    /** 圆点行本身的宽度,不含任何溢出 */
    private val dotRowWidth: Float
        get() = if (count <= 0) 0f else count * dotRadius * 2f + (count - 1) * dotGap

    /**
     * 需要画到圆点行之外的量(单侧):圆点样式是选中放大后多出来的那一圈,
     * 滑块样式是首尾绕回时越出端点的半格。测量时算进宽度,绘制时先让出它。
     */
    private val sideBleed: Float
        get() = when {
            count <= 0 -> 0f
            style == Style.DOTS -> dotRadius * (selectedScale - 1f)
            style == Style.DOTS_SLIDER -> sliderOverhang
            else -> 0f
        }

    /** 圆点行起点:先给两端溢出让位,剩下的空间里整体居中(宽度被外部写死时也不会贴边) */
    private val dotRowLeft: Float
        get() {
            val start = paddingStart + sideBleed
            val free = width - paddingStart - paddingEnd - sideBleed * 2f - dotRowWidth
            return if (free > 0f) start + free / 2f else start
        }

    private fun requestLayoutAndDraw() {
        requestLayout()
        invalidate()
    }

    private fun dp(value: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics,
    )
}
