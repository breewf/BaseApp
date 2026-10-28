package com.hy.demo.skin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.hy.basic.skin.IDarkMode
import com.hy.basic.skin.SkinManager
import com.hy.myapp.R
import kotlin.math.min

/** 自定义绘制的换肤示例:Canvas 颜色框架接不到,靠 [IDarkMode] 重新取色 */
class SkinRingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs), IDarkMode {

    /** 圆环画笔 */
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f * resources.displayMetrics.density
    }

    /** 换肤后重绘 */
    override fun onSkinChanged(isDark: Boolean) {
        invalidate()
    }

    /** 三层同心圆,颜色都取当前皮肤 */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f - paint.strokeWidth

        paint.style = Paint.Style.STROKE
        paint.color = SkinManager.color(R.color.skin_accent)
        canvas.drawCircle(cx, cy, radius, paint)

        paint.color = SkinManager.color(R.color.skin_text_tertiary)
        canvas.drawCircle(cx, cy, radius * 0.64f, paint)

        paint.style = Paint.Style.FILL
        paint.color = SkinManager.color(R.color.skin_bg_card)
        canvas.drawCircle(cx, cy, radius * 0.32f, paint)
    }
}
