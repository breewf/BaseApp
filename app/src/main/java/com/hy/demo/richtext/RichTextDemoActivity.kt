package com.hy.demo.richtext

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.myapp.databinding.ActivityRichTextDemoBinding

/**
 * desc: 富文本演示总入口 —— TabLayout + ViewPager2 承载两页
 *
 * 富文本在 Android 上的主流底座就是 Spannable:给文本区间挂各类 Span。两页分别演示:
 * - [SpanBasicFragment]:SpannableString 常见用法(字号 / 粗斜体 / 下划线 / 删除线 / 颜色 / 背景 /
 *   上下标 / 关键词高亮 / 可点击链接),全部走 com.hy.basic.utils.SpanExt 的链式扩展;
 * - [CollapsibleFragment]:扩展能力演示 —— 长文本按最大行数展开 / 折叠([com.hy.basic.utils.CollapsibleText])、
 *   动态切换折叠行数、正文内可点击链接与真实网页链接。
 *
 * 工具类收口在 [com.hy.basic.utils.SpanExt],页面代码只表达"这段要什么效果"。
 */
class RichTextDemoActivity : BaseActivity<ActivityRichTextDemoBinding, BaseViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "富文本演示"

    override fun createBinding(inflater: LayoutInflater) =
        ActivityRichTextDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.viewPager.adapter = RichTextPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 1
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = TAB_TITLES[position]
        }.attach()
    }

    private companion object {
        val TAB_TITLES = arrayOf("Spannable 常见用法", "展开折叠 · 链接")
    }

    /** ViewPager2 官方推荐适配器:按位置创建对应演示页 Fragment */
    private class RichTextPagerAdapter(activity: RichTextDemoActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = TAB_TITLES.size

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> SpanBasicFragment()
            else -> CollapsibleFragment()
        }
    }
}
