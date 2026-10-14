package com.hy.myapp.demo.state

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.myapp.databinding.ActivityFragmentPageStateBinding

/**
 * desc: Fragment 多状态页面测试 —— ViewPager + TabLayout 承载两个开启整页状态的 Fragment。
 *
 * 验证点:
 * - Toolbar 由基类壳布局统一提供,状态层在 ViewPager 内部(每个 Fragment 自己一层),
 *   因此 loading/错误页**不会遮住标题栏与 Tab**;
 * - 每个 Fragment 独立持有 ViewModel,两个 Tab 的状态互不干扰,回滑后状态保留;
 * - Tab1 开启 [BaseFragment.isLazyLoad],离屏页不会提前发起加载。
 */
class FragmentPageStateActivity :
    BaseActivity<ActivityFragmentPageStateBinding, FragmentPageStateViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "Fragment多状态页面测试"

    override fun createBinding(inflater: LayoutInflater) =
        ActivityFragmentPageStateBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.viewPager.adapter = PageStatePagerAdapter(supportFragmentManager)
        binding.viewPager.offscreenPageLimit = 1
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    /**
     * 沿用项目既有的 ViewPager(v1) + FragmentPagerAdapter 方案(未引入 viewpager2 依赖),
     * BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT 保证离屏页不进入 resumed,懒加载才会生效。
     */
    @Suppress("DEPRECATION")
    private class PageStatePagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int = 2

        override fun getItem(position: Int) =
            if (position == 0) PageStateStandardFragment() else PageStateCustomFragment()

        override fun getPageTitle(position: Int): CharSequence =
            if (position == 0) "默认状态" else "自定义外观"
    }
}

/** 宿主页自身没有整页状态,只作为 ViewPager 的容器 */
class FragmentPageStateViewModel : BaseViewModel()
