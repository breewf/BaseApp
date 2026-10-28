package com.hy.vehicle

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.hy.basic.BaseViewModel
import com.hy.basic.font.ignoreFontScale
import com.hy.basic.skin.BaseSkinActivity
import com.hy.demo.adapter.DemoMenuAdapter
import com.hy.demo.model.DemoEntry
import com.hy.myapp.databinding.ActivityVehicleDemoBinding

/**
 * desc: 车机能力验证入口。
 *
 * 和 DemoActivity 共用一套菜单模型,后面加能力只往 [VehicleDemoMenu] 登记一行,
 * 布局和这个页面都不用改。
 */
class VehicleDemoActivity : BaseSkinActivity<ActivityVehicleDemoBinding, BaseViewModel>() {

    /** 显示基类 Toolbar */
    override val showToolBar: Boolean get() = true

    /** Toolbar 标题 */
    override val toolbarTitle: CharSequence get() = "车机能力验证"

    /** 跟随字号设置 */
    override val followFontScale: Boolean get() = true

    /** 菜单适配器 */
    private val menuAdapter = DemoMenuAdapter(::onEntrySelected)

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater) =
        ActivityVehicleDemoBinding.inflate(inflater)

    /** 初始化网格和跨列规则 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.rvMenu.adapter = menuAdapter
        menuAdapter.submitList(VehicleDemoMenu.rows())
        binding.tvSubtitle.ignoreFontScale()

        (binding.rvMenu.layoutManager as? GridLayoutManager)?.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) =
                    if (menuAdapter.isHeaderAt(position)) 2 else 1
            }
    }

    /** 点击入口:优先页内动作,否则按 Class 跳转 */
    private fun onEntrySelected(entry: DemoEntry) {
        entry.action?.invoke() ?: entry.target?.let { startActivity(Intent(this, it)) }
    }
}
