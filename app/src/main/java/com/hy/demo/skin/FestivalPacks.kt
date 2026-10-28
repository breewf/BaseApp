package com.hy.demo.skin

import com.hy.basic.skin.SkinPack
import com.hy.myapp.R

/** 演示用节日包;新增节日加一个对象,在 App 里注册即可 */
object FestivalPacks {

    /** 春节:覆盖主色、页面底色和按压按钮 */
    @JvmField
    val SPRING = SkinPack(
        id = "spring",
        label = "春节",
        colors = mapOf(
            R.color.skin_accent to R.color.skin_festival_accent,
            R.color.skin_bg_page to R.color.skin_festival_page,
        ),
        drawables = mapOf(
            R.drawable.skin_btn_pressable to R.drawable.skin_festival_btn_pressable,
            R.drawable.skin_mood_liked to R.drawable.skin_festival_mood_liked,
        ),
    )
}
