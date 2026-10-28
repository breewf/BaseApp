package com.hy.basic.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

/**
 * 自定义圆角变换:支持四个角各自不同的半径(Glide 内置的 [com.bumptech.glide.load.resource.bitmap.RoundedCorners] 只能四角统一)。
 *
 * 半径单位为像素,调用方负责 dp -> px 的换算。
 *
 * Created by hy on 2026/10/15.
 */
class CornerTransformation(
    private val topLeft: Float,
    private val topRight: Float,
    private val bottomRight: Float,
    private val bottomLeft: Float,
) : BitmapTransformation() {

    /** 四角统一半径 */
    constructor(radius: Float) : this(radius, radius, radius, radius)

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID.toByteArray(Charsets.UTF_8))
        floatArrayOf(topLeft, topRight, bottomRight, bottomLeft).forEach { radius ->
            val bits = radius.toRawBits()
            messageDigest.update(
                byteArrayOf(
                    (bits shr 24 and 0xFF).toByte(),
                    (bits shr 16 and 0xFF).toByte(),
                    (bits shr 8 and 0xFF).toByte(),
                    (bits and 0xFF).toByte(),
                ),
            )
        }
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int,
    ): Bitmap {
        val config = toTransform.config ?: Bitmap.Config.ARGB_8888
        val output = pool.get(outWidth, outHeight, config)
        // 复用池里的 Bitmap 可能残留旧像素,先清空成透明再画,保证圆角外是透明的
        output.eraseColor(Color.TRANSPARENT)

        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val path = Path().apply {
            // addRoundRect 的 radii 必须是 8 个 float:每角 (rx, ry) 成对,顺序为左上、右上、右下、左下
            addRoundRect(
                RectF(0f, 0f, outWidth.toFloat(), outHeight.toFloat()),
                floatArrayOf(
                    topLeft, topLeft,
                    topRight, topRight,
                    bottomRight, bottomRight,
                    bottomLeft, bottomLeft,
                ),
                Path.Direction.CW,
            )
        }
        // 1. 先画出圆角矩形遮罩;2. 再用 SRC_IN 把原图裁进遮罩形状里(源图按输出尺寸缩放)
        canvas.drawPath(path, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(
            toTransform,
            Rect(0, 0, toTransform.width, toTransform.height),
            Rect(0, 0, outWidth, outHeight),
            paint,
        )
        return output
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CornerTransformation) return false
        return topLeft == other.topLeft &&
            topRight == other.topRight &&
            bottomRight == other.bottomRight &&
            bottomLeft == other.bottomLeft
    }

    override fun hashCode(): Int {
        var result = ID.hashCode()
        result = 31 * result + topLeft.hashCode()
        result = 31 * result + topRight.hashCode()
        result = 31 * result + bottomRight.hashCode()
        result = 31 * result + bottomLeft.hashCode()
        return result
    }

    private companion object {
        const val ID = "com.hy.basic.utils.CornerTransformation"
    }
}
