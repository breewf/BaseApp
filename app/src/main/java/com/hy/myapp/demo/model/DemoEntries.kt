package com.hy.myapp.demo.model

import com.hy.myapp.MainActivity
import com.hy.myapp.activity.BannerTestActivity
import com.hy.myapp.activity.PaletteListTestActivity
import com.hy.myapp.activity.PaletteTestActivity
import com.hy.myapp.activity.RetrofitTestActivity
import com.hy.myapp.demo.DemoActivity
import com.hy.myapp.demo.banner.BannerDemoActivity
import com.hy.myapp.demo.dialog.DialogDemoActivity
import com.hy.myapp.demo.hint.HintDemoActivity
import com.hy.myapp.demo.image.ImageLoaderDemoActivity
import com.hy.myapp.demo.network.NetworkTestActivity
import com.hy.myapp.demo.oilprice.OilPriceActivity
import com.hy.myapp.demo.palette.PaletteThemeActivity
import com.hy.myapp.demo.permission.PermissionDemoActivity
import com.hy.myapp.demo.state.FragmentPageStateActivity
import com.hy.myapp.demo.state.PageStateDemoActivity

/**
 * desc: 测试总入口数据源
 *
 * 约定:新页面写在 `demo/` 下对应子包,然后在这里登记一行即可出现在首页,
 * 不需要改动 [DemoActivity] 与任何布局文件。
 */
object DemoMenu {

    private fun groups(host: DemoActivity) = listOf(
        DemoGroup(
            "MVVM 基类 · 网络",
            listOf(
                DemoEntry(
                    "网络请求测试",
                    "裸 JSON 公共接口 / 信封接口 / 异常兜底 / loading 计数",
                    NetworkTestActivity::class.java,
                ),
                DemoEntry(
                    "油价查询测试",
                    "第三方裸 JSON 接口:status 手动校验 + 同域名两种返回结构",
                    OilPriceActivity::class.java,
                ),
                // DemoEntry(
                //     "全局异常通道",
                //     "仅 postApiException,由基类统一提示,页面零 Toast 代码",
                //     action = { host.showGlobalExceptionDemo() },
                // ),
            ),
        ),
        DemoGroup(
            "基类能力验证",
            listOf(
                DemoEntry("多状态页面测试", "PageStateLayout 零 XML 侵入,pageState 驱动加载/空/错误/无网络", PageStateDemoActivity::class.java),
                DemoEntry(
                    "Fragment多状态页面测试",
                    "ViewPager + 两个 Fragment 各自 enablePageState,状态层只覆盖页面内容",
                    FragmentPageStateActivity::class.java,
                ),
            ),
        ),
        DemoGroup(
            "系统能力",
            listOf(
                DemoEntry(
                    "系统权限申请",
                    "运行时权限用法示例，基于官方 registerForActivityResult",
                    PermissionDemoActivity::class.java,
                ),
            ),
        ),
        DemoGroup(
            "弹窗与提示",
            listOf(
                DemoEntry(
                    "Dialog 弹窗",
                    "showDialog / showCustomDialog 一份配置两种样式",
                    DialogDemoActivity::class.java,
                ),
                DemoEntry(
                    "Toast + Snackbar",
                    "Toast + Snackbar 封装，样式在 ToastKit/SnackbarKit 收口",
                    HintDemoActivity::class.java,
                ),
            ),
        ),
        DemoGroup(
            "UI · 控件 · 列表",
            listOf(
                DemoEntry(
                    "图片加载",
                    "ImageLoader 封装:圆形/圆角/特殊圆角 · 占位错误图 · 监听回调 · 缩略图 · GIF · 协程取图 · 预加载",
                    ImageLoaderDemoActivity::class.java,
                ),
                DemoEntry(
                    "Banner 轮播",
                    "ViewPager2 封装：循环 / 自动轮播 / 18 种切换动画 / 4 种指示器 / 露边 / 自定义页",
                    BannerDemoActivity::class.java,
                ),
                DemoEntry(
                    "图片吸色主题",
                    "ImagePalette 封装:协程取色·主题色吸顶联动·状态栏自适应·过渡动画",
                    PaletteThemeActivity::class.java,
                ),
            ),
        ),
        DemoGroup(
            "旧方案对照(Java / baseapp)",
            listOf(
                DemoEntry("Retrofit + RxJava", "basic/network Java 封装", RetrofitTestActivity::class.java),
                DemoEntry("Banner 轮播（旧 ConvenientBanner）", "三方库写法对照", BannerTestActivity::class.java),
                DemoEntry("Palette 取色", "图片主色调提取", PaletteTestActivity::class.java),
                DemoEntry("旧测试总入口", "Java BaseApp AbsBaseActivity", MainActivity::class.java),
            ),
        ),
    )

    /** 分组 -> 扁平列表行(标题行 + 入口行) */
    fun rows(host: DemoActivity): List<DemoRow> = groups(host).flatMap { group ->
        listOf(DemoRow.Header(group.title)) + group.entries.map { DemoRow.Entry(it) }
    }
}
