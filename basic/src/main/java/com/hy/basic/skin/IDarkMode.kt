package com.hy.basic.skin

/**
 * 自定义绘制的换肤钩子。Canvas、Paint 里的颜色 Factory2 接不到,
 * 这类 View 实现本接口,在 [onSkinChanged] 里重新取色。
 * 取色用 [SkinManager.color],不要直接用 context.resources。
 */
interface IDarkMode {

    /** 皮肤变化,true = 深色,通常 invalidate() 或重设 Paint */
    fun onSkinChanged(isDark: Boolean)
}
