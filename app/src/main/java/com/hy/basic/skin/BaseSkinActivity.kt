package com.hy.basic.skin

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.myapp.R
import kotlinx.coroutines.launch

/**
 * 支持换肤的 Activity 基类(自研引擎,不重建页面)。
 * 负责装 Factory2、注册根 View、窗口底色和系统栏图标。
 */
abstract class BaseSkinActivity<VB : ViewBinding, VM : BaseViewModel> :
    BaseActivity<VB, VM>() {

    /** super.onCreate 之前装 Factory2,晚一步就会被 AppCompat 占用 */
    override fun onConfigureInflater(inflater: LayoutInflater) {
        SkinManager.install(inflater)
    }

    /** 注册页面,同步窗口底色和系统栏 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyWindowBackground()
        SkinManager.register(window.decorView)
        applySystemBarStyle()
        applyContentInsets()
        lifecycleScope.launch {
            SkinManager.isDarkFlow.collect {
                applyWindowBackground()
                applySystemBarStyle()
            }
        }
    }

    /** 窗口底色跟随皮肤,根布局透明时也不会露出旧色 */
    private fun applyWindowBackground() {
        // 和页面颜色一样走 colorStateList,避免 getColor 拿不到夜间值
        val color = SkinManager.colorStateList(R.color.skin_bg_page)?.defaultColor
            ?: SkinManager.color(R.color.skin_bg_page)
        window.setBackgroundDrawable(ColorDrawable(color))
    }

    /** 页面销毁时反注册 */
    override fun onDestroy() {
        SkinManager.unregister(window.decorView)
        super.onDestroy()
    }

    /** 系统栏图标深浅跟随皮肤;Toolbar 页交给基类 */
    protected open fun applySystemBarStyle() {
        if (showToolBar) return
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !SkinManager.isDark
            isAppearanceLightNavigationBars = !SkinManager.isDark
        }
    }

    /** 无 Toolbar 时补系统栏留白 */
    private fun applyContentInsets() {
        if (showToolBar || enablePageState) return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val root: View = binding.root
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }
}
