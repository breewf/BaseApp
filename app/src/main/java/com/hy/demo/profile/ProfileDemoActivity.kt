package com.hy.demo.profile

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivityProfileDemoBinding

/**
 * desc: 个人详情页(CollapsingToolbarLayout 折叠吸顶方案演示)
 *
 * Android 的"工具栏/头部收缩"标准方案就是 Material 三件套:
 * CoordinatorLayout(调度) + AppBarLayout(可折叠容器) + CollapsingToolbarLayout(标题渐变缩放),
 * 属性驱动即可完成 视差头图 / contentScrim·statusScrim 背景渐变 / pin 住 Toolbar / Tab 吸顶 等效果,
 * 无需手写 IntersectionOffset 计算。更自由但成本更高的替代是 MotionLayout,常规详情页 CTL 足够。
 *
 * 本页演示的 CTL 能力:
 * - scroll|exitUntilCollapsed 折叠策略,Toolbar collapseMode=pin 钉顶;
 * - 头图 parallax 视差(collapseParallaxMultiplier 控制纵深速度);
 * - expandedTitle/collapsedTitle 两套 TextAppearance + titleCollapseMode=scale 缩放换场;
 * - contentScrim/statusBarScrim 随折叠进度渐显(背景渐变),scrimVisibleHeightTrigger 控制提前量;
 * - OnOffsetChangedListener 联动:头部叠层文字渐隐。
 *
 * 状态栏/Toolbar 颜色处理(edge-to-edge,手动分配 inset,不用 fitsSystemWindows 链):
 * - 本 Activity 不用基类 Toolbar([showToolBar]=false),头部全部页面自理;
 * - CTL 链上的 fitsSystemWindows 会把顶部 inset 变成 CTL 的 margin/padding,头图被顶下
 *   状态栏高度:展开态露出 AppBarLayout 白底、内容整体下移底部被裁,故一律弃用;
 * - 根 View 的 OnApplyWindowInsetsListener 统一分配:Toolbar paddingTop+加高=状态栏
 *   (同基类壳做法,收缩后 contentScrim 连状态栏带一起盖住),ViewPager2 paddingBottom=导航栏;
 * - 头图从 y=0 自然伸进状态栏,状态栏图标固定浅色(展开压在压暗渐变头图上,收起落在深色 scrim)。
 *
 * 滑动冲突与 inset 排查(均已规避):
 * - 内容用 NestedScrollView/RecyclerView 等嵌套滚动容器,普通 ScrollView 不联动(经典坑),
 *   且页内容必须明显超过一屏,否则吸顶后自身没有剩余滚动范围,看起来"推不动";
 * - Tab 页内列表大角度斜滑误切页 → RecyclerViewExt.resolvePagerConflict() 方向感知;
 * - inset 链引发的"展开态状态栏白底 + 页底被导航栏裁"是同一根因,见上方 inset 策略。
 */
class ProfileDemoActivity : BaseActivity<ActivityProfileDemoBinding, BaseViewModel>() {

    /** 页面用 CTL 内的 Toolbar,不套基类壳布局(壳 Toolbar 会与折叠头部打架) */
    override val showToolBar: Boolean get() = false

    override fun createBinding(inflater: LayoutInflater) =
        ActivityProfileDemoBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        // edge-to-edge:Android 15+ 本来就被迫开启,低版本主动 opt-in 保证行为一致 ——
        // 头图才能伸到状态栏底下,fitSystemWindows 链负责后续 inset 消化
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    override fun initView(savedInstanceState: Bundle?) {
        setupToolBar()
        ImageLoader.loadCircleImage(binding.ivAvatar, R.mipmap.cat_touxiang)

        // 关注按钮:演示头部交互(真实场景接状态与网络)
        binding.tvFollow.setOnClickListener {
            // 按文本内容判状态时别用 startsWith("已"):"✓ 已关注"并不以"已"开头
            val followed = binding.tvFollow.text.contains("已")
            binding.tvFollow.text = if (followed) "＋ 关注" else "✓ 已关注"
            showToast(if (followed) "已取消关注" else "关注成功")
        }

        binding.viewPager.adapter = ProfilePagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 1
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = TAB_TITLES[position]
        }.attach()

        // edge-to-edge inset 手动分配(布局里已摘掉全部 fitsSystemWindows):
        // - 顶部:头图从 y=0 伸进状态栏,只需把 pin 住的 Toolbar 补 paddingTop 并同步加高,
        //   收缩后 Toolbar 的 contentScrim 连状态栏带一起盖住,标题/图标落在状态栏下方;
        // - 底部:ViewPager2 补 paddingBottom=导航栏,页尾内容不被导航栏盖住。
        // 监听挂在根 CoordinatorLayout 上统一拿 inset,返回不消费,不影响其他分发。
        val toolbarContentHeight = binding.toolbar.layoutParams.height
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            if (binding.toolbar.paddingTop != bars.top) {
                binding.toolbar.setPadding(0, bars.top, 0, 0)
                binding.toolbar.layoutParams.height = toolbarContentHeight + bars.top
                binding.toolbar.requestLayout()
            }
            binding.viewPager.setPadding(0, 0, 0, bars.bottom)
            insets
        }

        // —— 折叠联动:0(完全展开) → 1(收缩到只剩 Toolbar) ——
        binding.appBar.addOnOffsetChangedListener { appBar, verticalOffset ->
            val range = appBar.totalScrollRange
            val percent = if (range > 0) (-verticalOffset.toFloat() / range).coerceIn(0f, 1f) else 0f
            // 前半程把头像/签名叠层渐隐,避免与 CTL 大标题"张三"同屏打架;contentScrim 自带渐变无需干预
            binding.headerInfo.alpha = (1f - percent * 2f).coerceIn(0f, 1f)
        }
    }

    /** CTL 里的 Toolbar:基类没帮忙装配,返回键/菜单在此自理;图标统一染白 */
    private fun setupToolBar() {
        val tb = binding.toolbar
        tb.navigationIcon = ContextCompat.getDrawable(this, com.hy.basic.R.drawable.abs_ic_arrow_back)
            ?.apply { setTint(Color.WHITE) }
        tb.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        tb.inflateMenu(R.menu.menu_profile_demo)
        repeat(tb.menu.size()) { i -> tb.menu.getItem(i).icon?.setTint(Color.WHITE) }
        tb.setOnMenuItemClickListener { item ->
            showToast("点击了:${item.title}")
            true
        }
    }

    private companion object {
        val TAB_TITLES = arrayOf("简介", "动态", "相册")
    }

    /** ViewPager2 官方推荐适配器:三个 Tab 页 */
    private class ProfilePagerAdapter(activity: ProfileDemoActivity) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = TAB_TITLES.size

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> ProfileInfoFragment()
            1 -> ProfileFeedFragment()
            else -> ProfileAlbumFragment()
        }
    }
}
