package com.hy.myapp.demo.model

import com.hy.myapp.MainActivity
import com.hy.myapp.activity.BannerTestActivity
import com.hy.myapp.activity.PaletteTestActivity
import com.hy.myapp.activity.RetrofitTestActivity
import com.hy.myapp.demo.DemoActivity
import com.hy.myapp.demo.banner.BannerDemoActivity
import com.hy.myapp.demo.db.DbDemoActivity
import com.hy.myapp.demo.dialog.DialogDemoActivity
import com.hy.myapp.demo.dialog.DialogFragmentDemoActivity
import com.hy.myapp.demo.event.EventBusDemoActivity
import com.hy.myapp.demo.hint.HintDemoActivity
import com.hy.myapp.demo.image.ImageLoaderDemoActivity
import com.hy.myapp.demo.list.ListDemoActivity
import com.hy.myapp.demo.network.NetworkTestActivity
import com.hy.myapp.demo.oilprice.OilPriceActivity
import com.hy.myapp.demo.palette.PaletteThemeActivity
import com.hy.myapp.demo.permission.PermissionDemoActivity
import com.hy.myapp.demo.profile.ProfileDemoActivity
import com.hy.myapp.demo.richtext.RichTextDemoActivity
import com.hy.myapp.demo.result.ActivityResultDemoActivity
import com.hy.myapp.demo.storage.StorageDemoActivity
import com.hy.myapp.demo.state.FragmentPageStateActivity
import com.hy.myapp.demo.state.PageStateDemoActivity

/**
 * desc: 测试总入口数据源
 *
 * 约定:新页面写在 `demo/` 下对应子包,然后在这里登记一行即可出现在首页,
 * 不需要改动 [DemoActivity] 与任何布局文件。
 *
 * 首页是两列网格,卡片只有半屏宽:[DemoEntry.desc] 按"一行核心信息"撰写
 * (展示端截断到两行),不要把功能清单整个塞进描述。
 */
object DemoMenu {

    private fun groups(host: DemoActivity) = listOf(
        DemoGroup(
            "MVVM 基类 · 网络",
            listOf(
                DemoEntry(
                    "网络请求测试",
                    "裸 JSON / 信封接口 / 异常兜底 / loading 计数",
                    NetworkTestActivity::class.java,
                ),
                DemoEntry(
                    "油价查询测试",
                    "第三方裸 JSON:status 手动校验",
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
                DemoEntry(
                    "多状态页面测试",
                    "PageStateLayout 零 XML 侵入,四种状态驱动",
                    PageStateDemoActivity::class.java,
                ),
                DemoEntry(
                    "Fragment多状态页面测试",
                    "ViewPager + 双 Fragment 各自 enablePageState",
                    FragmentPageStateActivity::class.java,
                ),
                DemoEntry(
                    "页面结果回传",
                    "awaitResult 一行拿结果 · UiEvent 统一事件通道",
                    ActivityResultDemoActivity::class.java,
                ),
                DemoEntry(
                    "全局事件总线",
                    "EventBus 一对多广播 · Fragment 间通信 · 粘性补收",
                    EventBusDemoActivity::class.java,
                ),
            ),
        ),
        DemoGroup(
            "系统能力",
            listOf(
                DemoEntry(
                    "系统权限申请",
                    "运行时权限,基于 registerForActivityResult",
                    PermissionDemoActivity::class.java,
                ),
                DemoEntry(
                    "本地持久化",
                    "MMKV 属性委托 + SP 扩展备用,JSON 对象存取",
                    StorageDemoActivity::class.java,
                ),
                DemoEntry(
                    "Room 数据库",
                    "BaseDao + Flow 查询，增删改后 observeAll() 自动重发",
                    DbDemoActivity::class.java,
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
                    "DialogFragment 弹框基类",
                    "全屏弹框 · 返回键拦截 · 底部面板 behavior",
                    DialogFragmentDemoActivity::class.java,
                ),
                DemoEntry(
                    "Toast + Snackbar",
                    "样式在 ToastKit / SnackbarKit 收口",
                    HintDemoActivity::class.java,
                ),
            ),
        ),
        DemoGroup(
            "UI · 控件 · 列表",
            listOf(
                DemoEntry(
                    "图片加载",
                    "ImageLoader 封装:圆形 / 圆角 / 占位 / GIF / 预加载",
                    ImageLoaderDemoActivity::class.java,
                ),
                DemoEntry(
                    "Banner 轮播",
                    "自研 ViewPager2 封装:18 种动画 · 4 种指示器",
                    BannerDemoActivity::class.java,
                ),
                DemoEntry(
                    "图片吸色主题",
                    "ImagePalette 协程取色 · 主题吸顶联动",
                    PaletteThemeActivity::class.java,
                ),
                DemoEntry(
                    "列表组件演示",
                    "BRVAH4 五种形态 + SmartRefresh 下拉上拉",
                    ListDemoActivity::class.java,
                ),
                DemoEntry(
                    "富文本演示",
                    "SpanExt 链式拼接 · 展开折叠",
                    RichTextDemoActivity::class.java,
                ),
                DemoEntry(
                    "详情页折叠吸顶",
                    "CollapsingToolbarLayout 视差 / 渐变 / Tab 吸顶",
                    ProfileDemoActivity::class.java,
                ),
            ),
        ),
        DemoGroup(
            "旧方案对照(Java / baseapp)",
            listOf(
                DemoEntry("Retrofit + RxJava", "basic/network Java 封装", RetrofitTestActivity::class.java),
                DemoEntry("Banner 轮播(旧 ConvenientBanner)", "三方库写法对照", BannerTestActivity::class.java),
                DemoEntry("Palette 取色", "官方 Palette 图片主色调提取", PaletteTestActivity::class.java),
                DemoEntry("旧测试总入口", "Java BaseApp AbsBaseActivity", MainActivity::class.java),
            ),
        ),
    )

    /** 分组 -> 扁平列表行(标题行 + 入口行) */
    fun rows(host: DemoActivity): List<DemoRow> = groups(host).flatMap { group ->
        listOf(DemoRow.Header(group.title)) + group.entries.map { DemoRow.Entry(it) }
    }
}
