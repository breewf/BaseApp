package com.hy.basic.skin

/** 换肤引擎 */
enum class SkinEngine {
    /** 自研:运行时刷新,不重建页面 */
    SELF,

    /** 官方实现:AppCompatDelegate + recreate,资源体系不变 */
    COMPAT,
}

/** 换肤总开关,引擎和启停都收在这里 */
object SkinConfig {

    /** false:不接管布局、不响应切换 */
    @Volatile
    var enabled: Boolean = true

    /** 当前引擎,运行时切换走 [SkinManager.setEngine] */
    @Volatile
    var engine: SkinEngine = SkinEngine.SELF
}
