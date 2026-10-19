package com.hy.basic.banner

import android.graphics.Camera
import android.graphics.Matrix
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * desc: Banner 内置翻页动画。每个效果的 block 都跑在同一套“每帧前置复位”之上,
 * 先把旋转/缩放/透明度/轴心全部还原,再按 keepSlide 决定要不要抵消原生平移。
 * 因为每帧都从干净状态开始,连续换动画也不会留下脏状态。
 *
 * position:当前页相对屏幕中心的偏移,-1=完全在左,0=正居中,1=完全在右。
 */
enum class BannerTransformer(
    /** 中文名称,给演示页与日志用 */
    val label: CharSequence,
    /** 对应 XML `banner_transformer` 的取值,必须与 attrs.xml 里声明的一致（2 / 6 曾用于视差与层叠,移除后编号不复用） */
    internal val attrValue: Int,
    /** 含 3D 旋转的效果需要硬件层,软件渲染下旋转会被裁掉边缘 */
    internal val needsLayer: Boolean = false,
    /** true = 保留原生平移;false = 原地播放(前置复位会把 translationX 抵消为 0) */
    internal val keepSlide: Boolean = true,
    /** 每页每帧的变换;默认空实现等于 ViewPager2 的原生滑动 */
    internal val block: (page: View, position: Float) -> Unit = { _, _ -> },
) {

    /** 原生滑动,不做任何额外变换 */
    NONE("原生滑动", 0),

    /** 淡入淡出:原地交叉淡入淡出 */
    FADE(
        label = "淡入淡出",
        attrValue = 1,
        keepSlide = false,
        block = { page, position -> page.alpha = 1f - abs(position).coerceAtMost(1f) },
    ),

    /** 深度(旧 DepthPageTransformer):离场页照常滑走,入场页在原地缩小淡出压到下层 */
    DEPTH(
        label = "深度",
        attrValue = 3,
        block = { page, position ->
            if (position <= 0f) {
                page.alpha = 1f
                page.translationX = 0f
            } else {
                val scale = MIN_SCALE_DEPTH + (1f - MIN_SCALE_DEPTH) * (1f - abs(position))
                page.alpha = 1f - position
                page.translationX = -page.width * position
                page.scaleX = scale
                page.scaleY = scale
                page.translationZ = -position
            }
        },
    ),

    /** 缩放:两侧页同时缩小淡出(官方 ZoomOut 思路),居中页最大最实 */
    ZOOM(
        label = "缩放",
        attrValue = 4,
        keepSlide = false,
        block = { page, position ->
            val offset = abs(position).coerceAtMost(1f)
            val scale = (1f - offset * 0.25f).coerceAtLeast(MIN_SCALE_DEPTH)
            page.alpha = 1f - offset
            page.scaleX = scale
            page.scaleY = scale
        },
    ),

    /** 缩放滑动(旧 ZoomOutSlideTransformer):边缩边淡,淡入淡出与位移按缩放后留出的余量补偿 */
    ZOOM_SLIDE(
        label = "缩放滑动",
        attrValue = 5,
        block = { page, position ->
            val height = page.height.toFloat()
            val width = page.width.toFloat()
            val scale = (1f - abs(position)).coerceAtLeast(MIN_SCALE_SLIDE)
            val vertMargin = height * (1f - scale) / 2f
            val horzMargin = width * (1f - scale) / 2f
            page.translationX = if (position < 0f) horzMargin - vertMargin / 2f else -horzMargin + vertMargin / 2f
            page.scaleX = scale
            page.scaleY = scale
            page.alpha = MIN_ALPHA_SLIDE + (scale - MIN_SCALE_SLIDE) / (1f - MIN_SCALE_SLIDE) * (1f - MIN_ALPHA_SLIDE)
        },
    ),

    /** 手风琴(旧 AccordionTransformer):以离场方向的边为轴把页压扁,像卷轴收起 */
    ACCORDION(
        label = "手风琴",
        attrValue = 7,
        keepSlide = false,
        block = { page, position ->
            page.pivotX = if (position < 0f) 0f else page.width.toFloat()
            val scale = (if (position < 0f) 1f + position else 1f - position).coerceAtLeast(0.01f)
            page.scaleX = scale
        },
    ),

    /** 立方体(旧 CubeOutTransformer):绕外侧竖直棱翻转,像正方体的相邻两个面 */
    CUBE(
        label = "立方体",
        attrValue = 8,
        needsLayer = true,
        block = { page, position ->
            page.pivotX = if (position < 0f) page.width.toFloat() else 0f
            page.pivotY = page.height / 2f
            page.rotationY = 90f * position
        },
    ),

    /** 立方体内翻(旧 CubeInTransformer):绕内侧棱翻转,观感上像从里面折出来 */
    CUBE_IN(
        label = "立方体内翻",
        attrValue = 9,
        needsLayer = true,
        block = { page, position ->
            page.pivotX = if (position > 0f) 0f else page.width.toFloat()
            page.pivotY = 0f
            page.rotationY = -90f * position
        },
    ),

    /** 水平翻转(旧 FlipHorizontalTransformer):绕竖直中线翻 180°,过 90° 后淡出隐藏背面 */
    FLIP(
        label = "翻转",
        attrValue = 10,
        needsLayer = true,
        keepSlide = false,
        block = { page, position ->
            val rotation = 180f * position
            page.alpha = if (abs(rotation) > 90f) 0f else 1f
            page.rotationY = rotation
        },
    ),

    /** 垂直翻转(旧 FlipVerticalTransformer):绕水平中线翻 */
    FLIP_VERTICAL(
        label = "垂直翻转",
        attrValue = 11,
        needsLayer = true,
        keepSlide = false,
        block = { page, position ->
            val rotation = -180f * position
            page.alpha = if (abs(rotation) > 90f) 0f else 1f
            page.rotationX = rotation
        },
    ),

    /** 上旋(旧 RotateUpTransformer):以上边中点为轴旋出,像卡片被掀起来 */
    ROTATE_UP(
        label = "上旋",
        attrValue = 12,
        block = { page, position ->
            page.pivotX = page.width / 2f
            page.pivotY = 0f
            page.rotation = ROT_MOD * position
        },
    ),

    /** 下旋(旧 RotateDownTransformer):以下边中点为轴旋出,角度更陡一点 */
    ROTATE_DOWN(
        label = "下旋",
        attrValue = 13,
        block = { page, position ->
            page.pivotX = page.width / 2f
            page.pivotY = page.height.toFloat()
            page.rotation = ROT_MOD * position * -1.25f
        },
    ),

    /** 前景退到背景(旧 ForegroundToBackgroundTransformer):右侧页缩到远处,左侧页慢半拍跟上 */
    FORE_TO_BACK(
        label = "前景退后",
        attrValue = 14,
        keepSlide = false,
        block = { page, position ->
            val width = page.width.toFloat()
            val scale = minOf2(if (position > 0f) 1f else abs(1f + position), 0.5f)
            page.scaleX = scale
            page.scaleY = scale
            page.translationX = if (position > 0f) width * position else -width * position * 0.25f
        },
    ),

    /** 背景升到前景(旧 BackgroundToForegroundTransformer):与 [FORE_TO_BACK] 反过来 */
    BACK_TO_FORE(
        label = "背景升前",
        attrValue = 15,
        keepSlide = false,
        block = { page, position ->
            val width = page.width.toFloat()
            val scale = minOf2(if (position < 0f) 1f else abs(1f - position), 0.5f)
            page.scaleX = scale
            page.scaleY = scale
            page.translationX = if (position < 0f) width * position else -width * position * 0.25f
        },
    ),

    /** 平板翻牌(旧 TabletTransformer):用 Camera 算出旋转后的横向补偿,像一块板子侧着翻 */
    TABLET(
        label = "平板翻牌",
        attrValue = 16,
        needsLayer = true,
        keepSlide = false,
        block = { page, position ->
            val rotation = (if (position < 0f) 30f else -30f) * abs(position)
            page.translationX = cameraOffsetX(rotation, page.width, page.height)
            page.pivotX = page.width / 2f
            page.pivotY = 0f
            page.rotationY = rotation
        },
    ),

    /** 放大进入(旧 ZoomInTransformer):离场页放大着淡出,入场页从放大回到原尺寸 */
    ZOOM_IN(
        label = "放大进入",
        attrValue = 17,
        keepSlide = false,
        block = { page, position ->
            val scale = if (position < 0f) position + 1f else abs(1f - position)
            page.scaleX = scale
            page.scaleY = scale
            page.alpha = (2f - scale).coerceAtMost(1f)
        },
    ),

    /** 内外缩放(旧 ScaleInOutTransformer):以相邻边为轴横竖一起收 */
    SCALE_IN_OUT(
        label = "内外缩放",
        attrValue = 18,
        keepSlide = false,
        block = { page, position ->
            page.pivotX = if (position < 0f) 0f else page.width.toFloat()
            page.pivotY = page.height / 2f
            val scale = (if (position < 0f) 1f + position else 1f - position).coerceAtLeast(0.01f)
            page.scaleX = scale
            page.scaleY = scale
        },
    ),

    /** 卡片流:两侧页缩小、压低、半透,配合 [Banner.pageMargin] 的"露边"效果最好看 */
    FLOW(
        label = "卡片流",
        attrValue = 19,
        block = { page, position ->
            val offset = abs(position).coerceAtMost(1f)
            val scale = 1f - offset * 0.2f
            page.scaleX = scale
            page.scaleY = scale
            page.alpha = 1f - offset * 0.5f
            page.translationZ = -offset
        },
    );

    internal companion object {
        /** XML 属性值 -> 枚举,取不到回落到 [NONE] */
        fun fromAttrValue(value: Int): BannerTransformer =
            entries.firstOrNull { it.attrValue == value } ?: NONE
    }
}

/** 旧 ABaseTransformer.MIN_SCALE:深度/缩放效果的下限 */
private const val MIN_SCALE_DEPTH = 0.75f

/** 缩放滑动的缩放/透明度下限(旧 ZoomOutSlideTransformer) */
private const val MIN_SCALE_SLIDE = 0.85f
private const val MIN_ALPHA_SLIDE = 0.5f

/** 上/下旋的角度系数(旧 RotateUp/DownTransformer.ROT_MOD) */
private const val ROT_MOD = -15f

/** 旧 ABaseTransformer.min(val, min):取更小值 */
private fun minOf2(value: Float, floor: Float): Float = if (value < floor) floor else value

/**
 * 把一页恢复成完全没有被变换过的状态。换动画、改露边之后用一次,
 * RecyclerView 会复用页 View,上一次留下的 alpha/scale/rotation 不清零就会跟着页跑。
 */
internal fun View.resetFlat() {
    rotationX = 0f
    rotationY = 0f
    rotation = 0f
    scaleX = 1f
    scaleY = 1f
    alpha = 1f
    translationX = 0f
    translationY = 0f
    translationZ = 0f
    pivotX = width / 2f
    pivotY = height / 2f
    setLayerType(View.LAYER_TYPE_NONE, null)
}

/** 批量复位:对当前已 attach 的第 [index] 页做 [resetFlat] */
internal fun RecyclerView.resetChild(index: Int) = getChildAt(index).resetFlat()

/**
 * 每帧前置复位:transformer 会作用在所有被 attach 的页上,RecyclerView 会复用页 View,
 * 上一次留下的 alpha/scale/rotation 不清零就会出现脏状态。
 */
internal fun View.resetForTransform(position: Float, keepSlide: Boolean) {
    rotationX = 0f
    rotationY = 0f
    rotation = 0f
    scaleX = 1f
    scaleY = 1f
    alpha = 1f
    translationY = 0f
    translationZ = 0f
    pivotX = width / 2f
    pivotY = height / 2f
    // keepSlide = false 时先把原生平移抵消,效果就在原地播放
    translationX = if (keepSlide) 0f else -width * position
}

/**
 * 视口外的页只置空透明,不再叠加效果。
 *
 * [edge] 是"还算不算视口内"的 position 边界:铺满时是 1(旧库 hideOffscreenPages 的语义);
 * 开了露边([Banner.pageMargin])后相邻页会更靠近中心,边界要相应放宽,
 * 否则相邻页会在静止状态被当成"视口外"而整体隐藏(露边就看不见了)。
 */
internal fun View.hideFarPage() {
    alpha = 0f
}

/** 露边时相邻卡片之间保留的缝隙(占 pageMargin 的比例),不留缝两张卡会贴在一起 */
private const val PEEK_GAP_RATIO = 0.5f

/**
 * 露边:把整页往屏幕中心靠拢,让相邻页的内容填进本页内缩让出来的那一段。
 * 靠拢量与 position 成正比,居中页不动,拖动过程中两侧页连续滑进滑出。
 * 如果效果把页缩小了还要再多补一段,缩小会让朝着中心那条边往外退。
 */
internal fun View.shiftForPeek(position: Float, peek: Int) {
    if (position == 0f || peek <= 0) return
    var shift = 2f * peek * (1f - PEEK_GAP_RATIO / 2f)
    if (pivotX == width / 2f && pivotY == height / 2f && scaleX < 1f) {
        shift += (1f - scaleX) * width / 2f
    }
    translationX -= position * shift
}

/** 旧 TabletTransformer 的 Camera 补偿量:旋转后把"多让出来的宽度"平移回去 */
private val tabletCamera = Camera()
private val tabletMatrix = Matrix()
private val tabletPoints = FloatArray(2)

private fun cameraOffsetX(degrees: Float, width: Int, height: Int): Float {
    tabletMatrix.reset()
    tabletCamera.save()
    tabletCamera.rotateY(abs(degrees))
    tabletCamera.getMatrix(tabletMatrix)
    tabletCamera.restore()
    tabletMatrix.preTranslate(-width * 0.5f, -height * 0.5f)
    tabletMatrix.postTranslate(width * 0.5f, height * 0.5f)
    tabletPoints[0] = width.toFloat()
    tabletPoints[1] = height.toFloat()
    tabletMatrix.mapPoints(tabletPoints)
    return (width - tabletPoints[0]) * (if (degrees > 0f) 1f else -1f)
}
