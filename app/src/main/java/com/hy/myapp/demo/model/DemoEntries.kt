package com.hy.myapp.demo.model

import com.hy.baseapp.activity.IndexHomeActivity
import com.hy.baseapp.activity.TabLayoutActivity
import com.hy.myapp.MainActivity
import com.hy.myapp.activity.BannerTestActivity
import com.hy.myapp.activity.DialogTestActivity
import com.hy.myapp.activity.MyDialogTestActivity
import com.hy.myapp.activity.PaletteListTestActivity
import com.hy.myapp.activity.PaletteTestActivity
import com.hy.myapp.activity.PictureTestActivity
import com.hy.myapp.activity.RetrofitTestActivity
import com.hy.myapp.activity.SnackBarTestActivity
import com.hy.myapp.demo.DemoActivity
import com.hy.myapp.demo.network.NetworkTestActivity
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
                    "全局异常通道",
                    "仅 postApiException,由基类统一提示,页面零 Toast 代码",
                    action = { host.showGlobalExceptionDemo() },
                ),
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
            "弹窗与提示",
            listOf(
                DemoEntry("系统 Dialog", "AlertDialog / 多选 / 日期时间", DialogTestActivity::class.java),
                DemoEntry("自定义 Dialog", "MyDialog 样式与回调", MyDialogTestActivity::class.java),
                DemoEntry("Snackbar / Toast", "基础提示能力", SnackBarTestActivity::class.java),
            ),
        ),
        DemoGroup(
            "列表 · 图片 · 取色",
            listOf(
                DemoEntry("图片加载", "Glide 封装 ImageLoader", PictureTestActivity::class.java),
                DemoEntry("Banner 轮播", "自动滚动与手势", BannerTestActivity::class.java),
                DemoEntry("Palette 取色", "图片主色调提取", PaletteTestActivity::class.java),
                DemoEntry("Palette 列表取色", "列表中的批量取色", PaletteListTestActivity::class.java),
            ),
        ),
        DemoGroup(
            "系统能力",
            listOf(
                DemoEntry(
                    "系统权限申请",
                    "awaitPermissions 一次拿到 已授予/可再申请/已永久拒绝/未声明 四种结论",
                    PermissionDemoActivity::class.java,
                ),
            ),
        ),
        DemoGroup(
            "旧方案对照(Java / baseapp)",
            listOf(
                DemoEntry("Retrofit + RxJava", "basic/network Java 封装", RetrofitTestActivity::class.java),
                DemoEntry("旧测试总入口", "baseapp AbsBaseActivity + 硬编码按钮", MainActivity::class.java),
                DemoEntry("IndexHome", "baseapp 首页 Tab 框架", IndexHomeActivity::class.java),
                DemoEntry("TabLayout", "baseapp TabLayout 示例", TabLayoutActivity::class.java),
            ),
        ),
    )

    /** 分组 -> 扁平列表行(标题行 + 入口行) */
    fun rows(host: DemoActivity): List<DemoRow> = groups(host).flatMap { group ->
        listOf(DemoRow.Header(group.title)) + group.entries.map { DemoRow.Entry(it) }
    }
}
