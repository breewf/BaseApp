package com.hy.myapp.demo.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.utils.appendSpanned
import com.hy.basic.utils.bold
import com.hy.basic.utils.relativeSize
import com.hy.basic.utils.richText
import com.hy.basic.utils.textColor
import com.hy.basic.utils.underline
import com.hy.myapp.R
import com.hy.myapp.databinding.FragmentProfileInfoBinding

/**
 * desc: Tab①「简介」—— 根是 NestedScrollView,竖向滚动经嵌套滚动链驱动头部折叠。
 *
 * 区块总高度刻意垫超过一屏:折叠本身靠嵌套 pre-scroll 就能完成,但吸顶后能不能继续滚,
 * 取决于 NSV 自身范围(内容高-视口高)——内容不足一屏时范围≈0,就会"吸顶后推不动"。
 *
 * 签名 / 爱好两段富文本继续走 [com.hy.basic.utils.SpanExt] 链式拼装,与富文本演示页同一套工具。
 */
class ProfileInfoFragment : BaseFragment<FragmentProfileInfoBinding, BaseViewModel>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentProfileInfoBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        val primary = ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        val accent = ContextCompat.getColor(requireContext(), R.color.colorAccent)

        binding.tvSign.text = richText {
            appendSpanned("个性签名:".bold())
            appendSpanned("写代码")
            appendSpanned(" · ".textColor(primary).bold())
            appendSpanned("摄影".textColor(accent).underline())
            appendSpanned(" · 旅行")
        }
        binding.tvHobby.text = richText {
            appendSpanned("兴趣爱好:".bold())
            appendSpanned("Android".textColor(primary).bold())
            appendSpanned(" / ")
            appendSpanned("机械键盘".relativeSize(0.85f))
            appendSpanned(" / ")
            appendSpanned("手冲咖啡".relativeSize(0.85f))
        }
    }
}
