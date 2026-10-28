package com.hy.demo.touch

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.myapp.databinding.ActivityItemTouchDemoBinding

/**
 * desc: 拖拽 / 侧滑演示的入口,两个 Tab 分别放 [DragSortFragment] 和 [SwipeActionFragment]。
 *
 * 两个页面的手势都由 basic/utils 的 setupXxx 装配,页面只声明开关。
 */
class ItemTouchDemoActivity : BaseActivity<ActivityItemTouchDemoBinding, BaseViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "拖拽与侧滑"

    override fun createBinding(inflater: LayoutInflater) =
        ActivityItemTouchDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.viewPager.adapter = TouchPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 1
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = TAB_TITLES[position]
        }.attach()
    }

    private companion object {
        val TAB_TITLES = arrayOf("拖拽排序", "侧滑操作")
    }

    /** 按位置创建对应的演示页 */
    private class TouchPagerAdapter(activity: ItemTouchDemoActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = TAB_TITLES.size

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> DragSortFragment()
            else -> SwipeActionFragment()
        }
    }
}
