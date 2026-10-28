package com.hy.basic.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.LruCache
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.palette.graphics.Palette
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * desc: 图片吸色(Palette)工具集 —— 协程挂起式取色 + 主题色落地常用扩展
 *
 * 现代 App 里"从封面/头图提取主题色,再刷到页面背景、状态栏、按钮、文字"是提升设计感的
 * 常规做法(如网易云专辑页顶部吸顶色)。本文件把这条链路收敛成三步:
 * 1. [ImagePalette.from] / [ImagePalette.fromImage] 挂起取色(带 Lru 缓存,重复选同一张图零开销);
 * 2. 拿到 [ImagePalette.Result] —— 其中 [ImagePalette.Result.themeColor] 是"一定能用"的主题色
 *    (内部按 vibrant → dominant → darkVibrant → 兜底挑),
 *    [ImagePalette.Result.onTheme] 是配在它上面的可读前景色(标题/按钮文字该白还是黑);
 * 3. 用扩展把颜色刷到界面:
 *    [ComponentActivity.applyThemeColor] 状态栏着色 + 按亮度自动切换状态栏图标深浅,
 *    [Int.toGradientBackground] 主题色渐变背景(专辑页头部常用),
 *    [View.setBackgroundColorTransition] 带颜色过渡动画的换色,
 *    [View.setGradientBackgroundTransition] 带过渡动画的渐变背景切换(头部渐变流动),
 *    [Int.darken] / [Int.lighten] / [Int.toHexString] 配套的颜色计算。
 *
 * 设计约定:
 * - Palette 生成是 CPU 密集操作,统一切到 [Dispatchers.Default],主线程不会被卡出 jank;
 * - 所有"可能取不到色"的入口都有兜底,调用方不需要再写 `?:` 判空;
 * - 不依赖页面上 ImageView 是否已显示图片(旧写法常从 Drawable 反取 Bitmap),
 *   网络图走 [ImageLoader.loadBitmap] 解码后再取色,链路更短也更可靠。
 */
object ImagePalette {

    /**
     * 一次取色的结果:既给好"开箱即用"的主题色/前景色,也保留原始命名色板供自由挑选。
     *
     * @param themeColor 页面主题色(保证可刷背景/状态栏,不会是黑或透明)
     * @param onTheme    主题色上 readable 的前景色(标题/正文该用白还是黑)
     * @param swatches   命名色板(名称 → 色值,已过滤取不到的),用于展示"这张图有哪些主色"
     */
    data class Result(
        val themeColor: Int,
        val onTheme: Int,
        val swatches: List<Swatch>,
    )

    /** 单个命名色板项 */
    data class Swatch(val name: String, val color: Int)

    /** 取不到任何有效色时的兜底主题(蓝灰,与常见占位背景协调) */
    const val FALLBACK_THEME: Int = 0xFF607D8B.toInt()

    /** 以 model(通常是 url / resId)为 key 的色板缓存 */
    private val cache = LruCache<Any, Result>(MAX_CACHE)

    /**
     * 从 [Bitmap] 取色(挂起,默认在 [Dispatchers.Default] 执行)。
     *
     * @param key 可空缓存键;传入(如 url)后同键复用结果,避免列表/反复切换时重复生成。
     */
    suspend fun from(
        bitmap: Bitmap,
        key: Any? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ): Result = withContext(dispatcher) {
        key?.let { cache[it] }?.let { return@withContext it }
        val palette = Palette.from(bitmap).maximumColorCount(MAX_COLOR_COUNT).generate()
        val theme = palette.pickThemeColor()
        val result = Result(theme, theme.readableOn(), palette.namedSwatches())
        if (key != null) cache.put(key, result)
        result
    }

    /**
     * 从任意图片模型(url / resId / File / Uri)取色(挂起):
     * 先用 [ImageLoader.loadBitmap] 解码,再走 [from]。
     *
     * @return 取色结果;图片本身加载失败返回 null(与"加载到了但图太平"区分,便于页面提示)。
     */
    suspend fun fromImage(
        context: Context,
        model: Any,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
    ): Result? {
        val bitmap = ImageLoader.loadBitmap(context, model) ?: return null
        return from(bitmap, key = model, dispatcher = dispatcher)
    }

    /** 清掉某张图的色板缓存(如图片被替换/要求重新取色) */
    fun evict(model: Any) {
        cache.remove(model)
    }

    /** maximumColorCount 官方建议缩略图 8~16、大图 24~40,常规卡片图取中间值 */
    private const val MAX_COLOR_COUNT = 24
    private const val MAX_CACHE = 64

    /** 按可用性优先级挑主题色:vibrant → dominant → darkVibrant → 兜底 */
    private fun Palette.pickThemeColor(): Int {
        vibrantSwatch?.let { return it.rgb }
        dominantSwatch?.let { return it.rgb }
        darkVibrantSwatch?.let { return it.rgb }
        return FALLBACK_THEME
    }

    /** 收集有值的命名色板,顺序即展示顺序 */
    private fun Palette.namedSwatches(): List<Swatch> = buildList {
        fun add(name: String, swatch: Palette.Swatch?) {
            swatch?.let { add(Swatch(name, it.rgb)) }
        }
        add("Vibrant", vibrantSwatch)
        add("DarkVibrant", darkVibrantSwatch)
        add("LightVibrant", lightVibrantSwatch)
        add("Muted", mutedSwatch)
        add("DarkMuted", darkMutedSwatch)
        add("LightMuted", lightMutedSwatch)
    }
}

// ------------------------------------------------------------------
// 颜色计算扩展(Android 颜色即 Int,配套吸色落地的"计算三件套")
// ------------------------------------------------------------------

/**
 * 在该颜色上用什么前景色可读性好(白 / 黑)。
 *
 * 按 WCAG 感知亮度判定:主题色深了文字用白、浅了用黑 —— 专辑页标题、按钮文字
 * 颜色都该由它决定,而不是写死。
 */
fun Int.readableOn(): Int {
    val luminance = 0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)
    return if (luminance < DARK_LUMA_THRESHOLD) Color.WHITE else Color.BLACK
}

/** 加深:HSV 空间降亮度、提饱和度,[factor] 越大越深(状态栏比主题色深一档的经典做法) */
fun Int.darken(factor: Float = 0.16f): Int = adjustHS(this, satDelta = factor, valueDelta = -factor)

/** 变浅:提亮度、降饱和度(浅色背景、分割线、禁用态常用) */
fun Int.lighten(factor: Float = 0.16f): Int = adjustHS(this, satDelta = -factor, valueDelta = factor)

/** 转 `#AARRGGBB` 文本(演示/日志/设计走查标注用) */
fun Int.toHexString(): String = String.format("#%08X", this)

private fun adjustHS(color: Int, satDelta: Float, valueDelta: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    hsv[1] = (hsv[1] + satDelta).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] + valueDelta).coerceIn(0f, 1f)
    return Color.HSVToColor(Color.alpha(color), hsv)
}

// ------------------------------------------------------------------
// 主题落地扩展
// ------------------------------------------------------------------

/**
 * 把主题色应用到窗口:状态栏着色 + 图标深浅自适应。
 *
 * edge-to-edge(targetSdk 35+)下状态栏本就透明,这里主要按主题色亮度切换
 * [WindowInsetsControllerCompat.isAppearanceLightStatusBars],保证状态栏图标在彩色头部下始终可见。
 */
fun ComponentActivity.applyThemeColor(themeColor: Int) {
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
        themeColor.readableOn() != Color.WHITE
}

/**
 * 生成"主题色 → 深一档"的垂直渐变,作任意 View 的背景。
 *
 * 专辑页/个人主页头部那种"从封面取色往下渐渐沉"的背景,一行调用即可,无需预设背景 XML。
 */
fun Int.toGradientBackground(depth: Float = 0.2f): GradientDrawable =
    GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM,
        intArrayOf(this, darken(depth)),
    )

/**
 * 带过渡动画地换背景色(ARGB 插值,[durationMs] 内从当前色滑向目标色)。
 *
 * 切歌/翻页时主题色突变很生硬,老写法是手写 ValueAnimator;这里收成扩展,
 * 页面一行 `view.setBackgroundColorTransition(color)` 即可。
 */
fun View.setBackgroundColorTransition(color: Int, durationMs: Long = 350L) {
    val start = (background as? ColorDrawable)?.color ?: Color.TRANSPARENT
    ValueAnimator.ofObject(ArgbEvaluator(), start, color).apply {
        duration = durationMs
        addUpdateListener { setBackgroundColor(it.animatedValue as Int) }
    }.start()
}

/**
 * 带过渡动画地把 View 背景换成"topColor → bottomColor"的垂直渐变。
 *
 * 与 [setBackgroundColorTransition] 的区别:后者是纯色插值,这里渐变两端各自 ARGB 插值,
 * 因此切换封面时整个头部是"渐变到渐变"平滑流动,而非瞬间跳一块新渐变。
 * 上一帧的起止色记在 View 的 tag([GradientState])里;连续快速切换会先 cancel 上一段动画,
 * 避免两个动画抢同一个 background。首次调用(start 与目标相同)等价于直接铺好渐变。
 */
fun View.setGradientBackgroundTransition(
    topColor: Int,
    bottomColor: Int,
    durationMs: Long = 350L,
) {
    val state = (tag as? GradientState) ?: GradientState(topColor, bottomColor, null).also { tag = it }
    state.animator?.cancel()
    val startTop = state.top
    val startBottom = state.bottom
    state.top = topColor
    state.bottom = bottomColor
    val evaluator = ArgbEvaluator()
    state.animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = durationMs
        addUpdateListener { anim ->
            val f = anim.animatedValue as Float
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    evaluator.evaluate(f, startTop, topColor) as Int,
                    evaluator.evaluate(f, startBottom, bottomColor) as Int,
                ),
            )
        }
        start()
    }
}

/** [setGradientBackgroundTransition] 存在 View tag 上的状态:当前渐变起止色 + 正在跑的动画 */
private class GradientState(var top: Int, var bottom: Int, var animator: ValueAnimator?)

/** 感知亮度低于此值算"深色背景",前景用白字 */
private const val DARK_LUMA_THRESHOLD = 160f
