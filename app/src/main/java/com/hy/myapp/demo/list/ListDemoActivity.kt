package com.hy.myapp.demo.list

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.myapp.databinding.ActivityListDemoBinding

/**
 * desc: 列表组件演示总入口 —— TabLayout + ViewPager2 承载三页
 *
 * 三页分别演示 RecyclerView 的主流形态:
 * - [VerticalListFragment]:竖向列表,集成 SmartRefreshLayout 下拉/上拉 + 整页多状态(pageState),完整流程参考页;
 * - [HorizontalGridFragment]:横向列表 + 网格列表(同一 CellAdapter,换 LayoutManager 即变换排布);
 * - [WaterfallFragment]:瀑布流(StaggeredGridLayoutManager 2 列);
 * - [MultiTypeFragment]:多类型适配器(BaseMultiItemAdapter,标题/图文/便签混排)。
 *
 * 适配器统一用 BRVAH 4.x(com.chad.library.adapter4),通用装配 / 刷新收尾扩展在 com.hy.basic.utils。
 */
class ListDemoActivity : BaseActivity<ActivityListDemoBinding, BaseViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "列表组件演示"

    override fun createBinding(inflater: LayoutInflater) = ActivityListDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.viewPager.adapter = ListPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 1
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = TAB_TITLES[position]
        }.attach()
    }

    private companion object {
        val TAB_TITLES = arrayOf("竖向·刷新", "横向·网格", "瀑布流", "多类型")
    }

    /** ViewPager2 官方推荐适配器:按位置创建对应演示页 Fragment */
    private class ListPagerAdapter(activity: ListDemoActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = TAB_TITLES.size

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> VerticalListFragment()
            1 -> HorizontalGridFragment()
            2 -> WaterfallFragment()
            else -> MultiTypeFragment()
        }
    }
}
