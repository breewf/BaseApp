package com.hy.demo.richtext

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.utils.CollapsibleText
import com.hy.basic.utils.appendSpanned
import com.hy.basic.utils.bold
import com.hy.basic.utils.enableLinkTouch
import com.hy.basic.utils.linkAt
import com.hy.basic.utils.richText
import com.hy.myapp.R
import com.hy.myapp.databinding.FragmentCollapsibleBinding

/**
 * desc: 富文本进阶演示 —— 长文本"按最大行数展开 / 折叠" + 正文内链接。
 *
 * - [CollapsibleText] 负责测量与折叠:正文超过当前最大行数时,末尾保留可点击的"…展开全部",
 *   点击展开并显示"收起";点控制条可动态切换折叠到 2 / 3 / 5 行([setMaxLines]),
 *   也可用"展开/收起"按钮手动切换([toggle])。
 * - 下方评论区演示富文本正文里的可点击链接(@用户、#话题、真实网址),链接能力同样来自
 *   [com.hy.basic.utils.SpanExt]。
 */
class CollapsibleFragment : BaseFragment<FragmentCollapsibleBinding, BaseViewModel>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentCollapsibleBinding.inflate(inflater, container, false)

    private lateinit var collapsible: CollapsibleText

    override fun initView(savedInstanceState: Bundle?) {
        val primary = ContextCompat.getColor(requireContext(), R.color.colorPrimary)

        // 1) 长正文折叠:默认折叠到 3 行
        collapsible = CollapsibleText(binding.tvArticle)
        collapsible.bind(ARTICLE, lines = 3)

        // 2) 动态设置"最大几行后折叠":改行数即重置为折叠态并按新行数重测
        binding.btnLines2.setOnClickListener { collapsible.setMaxLines(2) }
        binding.btnLines3.setOnClickListener { collapsible.setMaxLines(3) }
        binding.btnLines5.setOnClickListener { collapsible.setMaxLines(5) }
        binding.btnToggle.setOnClickListener { collapsible.toggle() }

        // 3) 正文内可点击链接
        val comment = SpannableStringBuilder(
            "来自 @张三 的分享:强推 #富文本 这套方案,官方文档见 https://developer.android.com 供参考。",
        )
        comment.linkAt("@张三", color = primary) { showToast("查看用户:张三") }
        comment.linkAt("#富文本", color = primary) { showToast("打开话题:#富文本") }
        comment.linkAt("https://developer.android.com", color = Color.BLUE) {
            openUrl("https://developer.android.com")
        }
        binding.tvComment.text = richText {
            appendSpanned("评论链接 · ".bold())
            appendSpanned(comment)
        }
        binding.tvComment.enableLinkTouch()
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            showToast("没有可用的浏览器:$url")
        }
    }

    private companion object {
        /** 一段足够长的正文,用于演示超过 3 行后自动折叠 */
        const val ARTICLE =
            "Android 的富文本能力主要建立在 Spannable 之上:通过给一段文本的不同区间挂上各类 Span," +
                "我们可以在同一个 TextView 里同时呈现不同的字号、颜色、粗斜体、下划线、删除线、背景色、" +
                "上下标,甚至嵌入图片、可点击链接与自定义绘制效果。相比为每种样式单独摆放一个 TextView," +
                "Spannable 能在一段连续文本里精确控制每个字符的外观与交互,这也是评论区富文本、商品详情页、" +
                "搜索结果高亮、正文折叠等场景的标准做法。 Kotlin 里再配合扩展函数与构建器,把 setSpan 和下标" +
                "计算这些繁琐易错的部分收口进工具类,业务代码只需要表达「这段文字想要什么效果」即可," +
                "可读性与复用性都会好很多。除了静态样式,ClickableSpan 让我们能给文字里的关键词绑定点击事件," +
                "再结合 Layout 对行数的测量,就能实现「超过若干行自动折叠、点击展开全部」的正文折叠交互 —— " +
                "这正是本页面正在演示的能力:试着切换上面的折叠行数,或点击右下角的展开收起按钮感受一下。"
    }
}
