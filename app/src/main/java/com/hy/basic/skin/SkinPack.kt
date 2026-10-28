package com.hy.basic.skin

/**
 * 节日皮肤包:只声明与默认皮肤不同的资源,未列出的继续使用默认皮肤。
 * 节日资源本身仍可带 values-night、drawable-night 变体。
 */
data class SkinPack(
    /** 唯一标识,用于持久化和查找 */
    val id: String,
    /** 展示名 */
    val label: String,
    /** 颜色覆盖:默认颜色 id -> 节日颜色 id */
    val colors: Map<Int, Int> = emptyMap(),
    /** 图片覆盖:默认 drawable id -> 节日 drawable id */
    val drawables: Map<Int, Int> = emptyMap(),
)
