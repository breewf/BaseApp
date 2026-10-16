package com.hy.myapp.demo.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.utils.addDivider
import com.hy.basic.utils.attachAdapter
import com.hy.basic.utils.resolvePagerConflict
import com.hy.basic.utils.setupLinear
import com.hy.myapp.R
import com.hy.myapp.databinding.FragmentProfileFeedBinding
import com.hy.myapp.demo.profile.adapter.Moment
import com.hy.myapp.demo.profile.adapter.ProfileMomentAdapter

/**
 * desc: Tab②「动态」—— RecyclerView 时间线。
 *
 * 列表在横向 ViewPager2 页内:大角度斜滑容易被 pager 误判成切页,
 * [resolvePagerConflict] 方向感知处理(竖向为主期间关掉 pager 手势输入);
 * 竖向滚动本身走嵌套滚动链,可直接驱动外层头部折叠。
 */
class ProfileFeedFragment : BaseFragment<FragmentProfileFeedBinding, BaseViewModel>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentProfileFeedBinding.inflate(inflater, container, false)

    private val adapter by lazy { ProfileMomentAdapter() }

    override fun initView(savedInstanceState: Bundle?) {
        binding.rvFeed.setupLinear().addDivider().resolvePagerConflict().attachAdapter(adapter)
        adapter.setOnItemClickListener { _, _, position ->
            adapter.items.getOrNull(position)?.let {
                showToast("动态:${it.content.take(16)}")
            }
        }
    }

    override fun initData() {
        adapter.submitList(MOMENTS)
    }

    private companion object {
        /** 配图混排网络 url 与本地 resId,无图的条目演示"可选配图"分支 */
        val MOMENTS = listOf(
            Moment(
                "今天 09:30",
                "早会前把折叠吸顶的 Demo 合进主干:CollapsingToolbarLayout 的 contentScrim 渐变过渡真的顺滑。",
                "https://www.gstatic.com/webp/gallery/1.jpg",
            ),
            Moment(
                "昨天 21:14",
                "复盘:嵌套滚动链里把 NestedScrollView 换成普通 ScrollView,头部直接不联动了 —— 教训写进页面注释。",
            ),
            Moment(
                "周一 18:02",
                "给 Tab 页列表加了方向感知的滑动冲突处理,大角度斜滑不再误切 Tab。",
                R.mipmap.img_lab_view1,
            ),
            Moment(
                "上周 22:47",
                "夜跑 5km,顺手用镜头记录城市夜色。",
                "https://www.gstatic.com/webp/gallery/4.jpg",
            ),
            Moment(
                "上周 10:15",
                "Glide 圆角 + centerCrop 拉伸坑解决:先 CenterCrop 再 RoundedCorners,圆角不变形。",
            ),
            Moment(
                "三天前",
                "个人主页新头图上线,视差收缩 + 底部压暗渐变,叠字终于处处清晰。",
                R.mipmap.bg_home_create,
            ),
            Moment(
                "本月 01",
                "月度目标:基础库文档补齐,交互细节全部过一遍。",
            ),
            Moment(
                "上个月",
                "线下 Droid 交流,分享 MVVM 基类从 DataBinding 到 ViewBinding + 协程的演进。",
                "https://www.gstatic.com/webp/gallery/5.jpg",
            ),
        )
    }
}
