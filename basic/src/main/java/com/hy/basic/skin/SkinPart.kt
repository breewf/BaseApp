package com.hy.basic.skin

/** 可换肤的属性,代码里切控件形态时指定改哪一项 */
enum class SkinPart {

    /** 文字色 */
    TEXT_COLOR,

    /** 提示文字色 */
    TEXT_HINT,

    /** 链接文字色 */
    TEXT_COLOR_LINK,

    /** 背景 */
    BACKGROUND,

    /** 前景 */
    FOREGROUND,

    /** 图片 */
    SRC,

    /** 图片着色 */
    IMAGE_TINT,

    /** 背景着色 */
    BACKGROUND_TINT,

    /** 复合图-左 */
    COMPOUND_DRAWABLE_START,

    /** 复合图-上 */
    COMPOUND_DRAWABLE_TOP,

    /** 复合图-右 */
    COMPOUND_DRAWABLE_END,

    /** 复合图-下 */
    COMPOUND_DRAWABLE_BOTTOM,

    /** 进度色 */
    PROGRESS_TINT,

    /** 进度条底色 */
    PROGRESS_BACKGROUND_TINT,

    /** 次要进度色 */
    SECONDARY_PROGRESS_TINT,

    /** 不确定进度色 */
    INDETERMINATE_TINT,

    /** 滑块色 */
    THUMB_TINT,

    /** 刻度色 */
    TICK_MARK_TINT,

    /** 按钮着色 */
    BUTTON_TINT,

    /** 分割线 */
    DIVIDER,

    /** CardView 底色 */
    CARD_BG,
}
