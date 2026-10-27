package com.hy.myapp.demo.skin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.skin.SkinManager
import com.hy.basic.skin.SkinMode
import com.hy.basic.skin.setBackgroundSkin
import com.hy.basic.skin.setTextColorSkin
import com.hy.myapp.R
import com.hy.myapp.databinding.FragmentSkinColorBinding
import kotlinx.coroutines.launch

/** 换肤验证 Fragment 一:静态控件、按压 drawable、多形态切态 */
class SkinColorFragment : BaseFragment<FragmentSkinColorBinding, BaseViewModel>() {

    /** 当前形态,默认正常态 */
    private var mood = Mood.NORMAL

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSkinColorBinding.inflate(inflater, container, false)

    /** 绑定换肤按钮与按压按钮,订阅皮肤变化 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.tvFollow.setOnClickListener { SkinManager.switchTo(SkinMode.FOLLOW_SYSTEM) }
        binding.tvLight.setOnClickListener { SkinManager.switchTo(SkinMode.LIGHT) }
        binding.tvDark.setOnClickListener { SkinManager.switchTo(SkinMode.DARK) }
        binding.tvMood.setOnClickListener {
            mood = when (mood) {
                Mood.NORMAL -> Mood.LIKED
                Mood.LIKED -> Mood.DISLIKED
                Mood.DISLIKED -> Mood.NORMAL
            }
            renderMood()
        }
        renderMood()
        binding.tvMark.setOnClickListener { showToast("按压态由 drawable 提供") }
        viewLifecycleOwner.lifecycleScope.launch {
            SkinManager.isDarkFlow.collect { renderState() }
        }
    }

    /** 刷新代码生成的文案,颜色由框架处理 */
    private fun renderState() {
        binding.tvState.text =
            "当前：${SkinManager.mode.label} · ${if (SkinManager.isDark) "深色" else "浅色"}"
    }

    /** 用 setSkinRes 写入当前形态,切肤时会按同一个资源重新取色 */
    private fun renderMood() {
        val bgRes: Int
        val textColorRes: Int
        val label: String
        when (mood) {
            Mood.NORMAL -> {
                bgRes = R.drawable.skin_mood_normal
                textColorRes = R.color.skin_text_primary
                label = "正常态"
            }
            Mood.LIKED -> {
                bgRes = R.drawable.skin_mood_liked
                textColorRes = R.color.skin_accent
                label = "已喜欢"
            }
            Mood.DISLIKED -> {
                bgRes = R.drawable.skin_mood_disliked
                textColorRes = R.color.skin_danger
                label = "不喜欢"
            }
        }
        binding.tvMood.setBackgroundSkin(bgRes)
        binding.tvMood.setTextColorSkin(textColorRes)
        binding.tvMood.text = label
    }

    /** 三种形态 */
    private enum class Mood { NORMAL, LIKED, DISLIKED }
}
