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
import com.hy.basic.utils.absoluteSizeSp
import com.hy.basic.utils.appendSpanned
import com.hy.basic.utils.backgroundColor
import com.hy.basic.utils.bold
import com.hy.basic.utils.boldItalic
import com.hy.basic.utils.enableLinkTouch
import com.hy.basic.utils.highlightWords
import com.hy.basic.utils.italic
import com.hy.basic.utils.linkAt
import com.hy.basic.utils.relativeSize
import com.hy.basic.utils.richText
import com.hy.basic.utils.strike
import com.hy.basic.utils.subscript
import com.hy.basic.utils.superscript
import com.hy.basic.utils.textColor
import com.hy.basic.utils.underline
import com.hy.myapp.R
import com.hy.myapp.databinding.FragmentSpanBasicBinding

/**
 * desc: Spannable 常见用法演示页 —— 逐条展示字号 / 粗斜体 / 装饰线 / 颜色 / 背景 / 上下标 /
 * 关键词高亮 / 可点击链接 / 综合混排,富文本全部由 [com.hy.basic.utils.SpanExt] 的链式扩展拼装。
 *
 * 混排思路:`richText { appendSpanned(纯文本); appendSpanned("局部".bold().textColor(红)) }`,
 * 每个局部效果先作用在短串上,再用 [com.hy.basic.utils.appendSpanned] 把字符连同 span 一起
 * 搬进构建器(系统 append 在 Android 12 以下不会拷贝 span),避免把 span 误加到整段。
 */
class SpanBasicFragment : BaseFragment<FragmentSpanBasicBinding, com.hy.basic.BaseViewModel>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSpanBasicBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        val primary = ContextCompat.getColor(requireContext(), R.color.colorPrimary)

        // 字号:相对倍数 + 绝对 sp
        binding.tvSize.text = richText {
            appendSpanned("字号 · ".bold())
            appendSpanned("缩小0.8倍".relativeSize(0.8f))
            appendSpanned(" 正常 ")
            appendSpanned("放大1.5倍".relativeSize(1.5f))
            appendSpanned("  ")
            appendSpanned("28sp绝对".absoluteSizeSp(28f))
        }

        // 字体样式:粗 / 斜 / 粗斜
        binding.tvStyle.text = richText {
            appendSpanned("字体样式 · ".bold())
            appendSpanned("粗体".bold())
            appendSpanned(" ")
            appendSpanned("斜体".italic())
            appendSpanned(" ")
            appendSpanned("粗斜体".boldItalic())
        }

        // 装饰线:下划线 / 删除线 / 组合(链式叠加两个 span)
        binding.tvDecoration.text = richText {
            appendSpanned("装饰线 · ".bold())
            appendSpanned("下划线".underline())
            appendSpanned(" ")
            appendSpanned("删除线".strike())
            appendSpanned(" ")
            appendSpanned("下划线+删除线".underline().strike())
        }

        // 前景色 / 背景色
        binding.tvColor.text = richText {
            appendSpanned("颜色 · ".bold())
            appendSpanned("红字".textColor(Color.RED))
            appendSpanned(" ")
            appendSpanned("主题色".textColor(primary))
            appendSpanned(" ")
            appendSpanned("荧光笔".backgroundColor(Color.YELLOW).textColor(Color.BLACK))
        }

        // 上下标:x²、H₂O
        binding.tvSuperSub.text = richText {
            appendSpanned("上下标 · ".bold())
            appendSpanned("x")
            appendSpanned("2".superscript())
            appendSpanned(" = 100   ")
            appendSpanned("H")
            appendSpanned("2".subscript())
            appendSpanned("O 是水")
        }

        // 关键词高亮:搜索命中词标红 + 半透明背景
        val highlightBody = SpannableStringBuilder(
            "Android 富文本一般用 Spannable 实现,所有命中的 Span 关键词都会被自动高亮。",
        ).highlightWords("Spannable", "Span", color = Color.RED, background = 0x33FF0000.toInt())
        binding.tvHighlight.text = richText {
            appendSpanned("关键词高亮 · ".bold())
            appendSpanned(highlightBody)
        }

        // 可点击链接:关键字点击回调 + 真实网址跳浏览器
        val linkBody = SpannableStringBuilder("点击 查看详情 了解用法,或访问官网 www.baidu.com 打开浏览器。")
        linkBody.linkAt("查看详情", color = primary) { showToast("点击了「查看详情」") }
        linkBody.linkAt("www.baidu.com", color = Color.BLUE) { openUrl("https://www.baidu.com") }
        binding.tvLink.text = richText {
            appendSpanned("可点击链接 · ".bold())
            appendSpanned(linkBody)
        }
        // 含 ClickableSpan 的文本必须开启链接点击,否则点不动
        binding.tvLink.enableLinkTouch()

        // 综合混排:电商价格行
        binding.tvMixed.text = richText {
            appendSpanned("综合混排 · ".bold())
            appendSpanned("¥".absoluteSizeSp(13f).textColor(Color.RED))
            appendSpanned("99".textColor(Color.RED).absoluteSizeSp(26f).bold())
            appendSpanned(" ¥199 ".textColor(Color.GRAY).strike())
            appendSpanned("  ")
            // 注意:颜色必须写满 8 位 ARGB,0xFFE8D1 缺省 alpha 会透明导致文字隐形
            appendSpanned("包邮".backgroundColor(0xFFFFE8D1.toInt()).textColor(0xFFFF6600.toInt()))
        }
    }

    /** 用系统浏览器打开链接;无浏览器时降级为提示 */
    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            showToast("没有可用的浏览器:$url")
        }
    }
}
