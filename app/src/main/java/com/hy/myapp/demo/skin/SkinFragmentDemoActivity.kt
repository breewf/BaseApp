package com.hy.myapp.demo.skin

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hy.basic.BaseViewModel
import com.hy.basic.skin.BaseSkinActivity
import com.hy.basic.skin.SkinManager
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivitySkinFragmentDemoBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** 换肤验证:Tab + ViewPager2 双 Fragment,Fragment 无需额外接入 */
class SkinFragmentDemoActivity :
    BaseSkinActivity<ActivitySkinFragmentDemoBinding, BaseViewModel>() {

    /** 显示基类 Toolbar */
    override val showToolBar: Boolean get() = true

    /** Toolbar 标题 */
    override val toolbarTitle: CharSequence get() = "换肤 · Fragment"

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater) =
        ActivitySkinFragmentDemoBinding.inflate(inflater)

    /** Toolbar 由基类壳提供,退出自动槽位后自己刷 */
    override fun onToolBarInitialized(toolbar: Toolbar) {
        SkinManager.ignore(toolbar)
        applyToolbarSkin(toolbar)
    }

    /** 初始化 Tab 与 ViewPager2 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            /** 两个 Fragment */
            override fun getItemCount() = 2

            /** 按位置创建页面 */
            override fun createFragment(position: Int) =
                if (position == 0) SkinColorFragment() else SkinListFragment()
        }
        binding.viewPager.offscreenPageLimit = 1
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "控件" else "列表"
        }.attach()

        applySkinExtras()
        // 深浅或节日包变化都要重刷 Tab/Toolbar
        lifecycleScope.launch {
            combine(SkinManager.isDarkFlow, SkinManager.packFlow) { _, _ -> Unit }
                .collect { applySkinExtras() }
        }
    }

    /** Toolbar 页需要自己定系统栏图标深浅 */
    override fun applySystemBarStyle() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !SkinManager.isDark
            isAppearanceLightNavigationBars = !SkinManager.isDark
        }
    }

    /** 刷新 Factory2 覆盖不到的控件:Toolbar 与 TabLayout */
    private fun applySkinExtras() {
        applyTabSkin()
        toolbar?.let(::applyToolbarSkin)
    }

    /** Toolbar 在基类壳里,不在布局槽位中,颜色手动刷 */
    private fun applyToolbarSkin(toolbar: Toolbar) {
        val textColor = SkinManager.color(R.color.skin_text_primary)
        toolbar.setBackgroundColor(SkinManager.color(R.color.skin_bg_card))
        toolbar.setTitleTextColor(textColor)
        toolbar.navigationIcon?.mutate()?.let {
            it.setTint(textColor)
            toolbar.navigationIcon = it
        }
    }

    /** TabLayout 的标签是内部 TextView,不经过 Factory2,颜色要手动刷 */
    private fun applyTabSkin() {
        binding.tabLayout.setTabTextColors(
            SkinManager.color(R.color.skin_text_tertiary),
            SkinManager.color(R.color.skin_accent),
        )
        binding.tabLayout.setSelectedTabIndicatorColor(SkinManager.color(R.color.skin_accent))
    }
}
