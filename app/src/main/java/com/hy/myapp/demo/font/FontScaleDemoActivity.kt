package com.hy.myapp.demo.font

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.basic.font.FontScaleLevel
import com.hy.basic.font.FontScaleManager
import com.hy.myapp.R
import com.hy.myapp.databinding.ActivityFontScaleDemoBinding

/**
 * 字体大小设置页。颜色用 font_demo_* 独立一套，不接换肤。
 *
 * 预览区按档位手算字号，用 dp 而不是 sp：sp 会再吃一次页面的 fontScale，
 * 预览就放大两次。点一下就能看到效果，真正生效靠返回后 Demo 页重建。
 *
 * 本页不跟随字号，免得设置页自己跳动。
 */
class FontScaleDemoActivity : BaseActivity<ActivityFontScaleDemoBinding, BaseViewModel>() {

    /** 显示基类 Toolbar */
    override val showToolBar: Boolean get() = true

    /** Toolbar 标题 */
    override val toolbarTitle: CharSequence get() = "字体大小"

    /** 本页不跟随字号 */
    override val followFontScale: Boolean get() = false

    /** 档位和控件按顺序对应，加档位时两边一起改 */
    private val chips by lazy {
        listOf(
            FontScaleLevel.SMALL to binding.tvSmall,
            FontScaleLevel.NORMAL to binding.tvNormal,
            FontScaleLevel.LARGE to binding.tvLarge,
            FontScaleLevel.XLARGE to binding.tvXLarge,
        )
    }

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater) =
        ActivityFontScaleDemoBinding.inflate(inflater)

    /** 绑定点击，按当前档位铺一遍 */
    override fun initView(savedInstanceState: Bundle?) {
        chips.forEach { (level, chip) -> chip.setOnClickListener { pick(level) } }
        render()
    }

    /** 刷新预览字号和选中态 */
    private fun render() {
        val level = FontScaleManager.level
        // dp 走 density，不含 fontScale，预览不会被系统字号再放一次
        binding.tvPreviewTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f * level.scale)
        binding.tvPreviewBody.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f * level.scale)
        binding.tvPreviewHint.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f * level.scale)

        chips.forEach { (item, chip) ->
            val selected = item == level
            chip.setBackgroundResource(
                if (selected) R.drawable.font_demo_chip_selected else R.drawable.font_demo_chip_normal,
            )
            chip.setTextColor(
                ContextCompat.getColor(
                    this,
                    if (selected) R.color.font_demo_on_accent else R.color.font_demo_text,
                ),
            )
        }
    }

    /** 同档位不重复保存 */
    private fun pick(level: FontScaleLevel) {
        if (level == FontScaleManager.level) return
        FontScaleManager.level = level
        render()
    }
}
