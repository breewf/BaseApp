package com.hy.myapp.demo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.hy.basic.BaseActivity
import com.hy.basic.BaseViewModel
import com.hy.basic.network.ApiException
import com.hy.basic.network.ApiExceptionExt
import com.hy.myapp.databinding.ActivityDemoBinding
import com.hy.myapp.demo.adapter.DemoMenuAdapter
import com.hy.myapp.demo.model.DemoEntry
import com.hy.myapp.demo.model.DemoMenu

/**
 * desc: App 启动总入口 = 基类框架能力验证总入口
 *
 * 组织方式:分类(分组标题)+ 入口(卡片),全部由 [DemoMenu] 数据驱动,
 * 新增测试页只加一行登记,不改本类与布局。
 *
 * 首页条目多,列表用两列网格提密度:分组标题跨满整行,卡片半屏宽,
 * 描述在模型里就按"一行核心信息"撰写(展示端 maxLines=1 截断)。
 *
 * 本页同样走 BaseActivity 规范:ViewBinding + ViewModel + 模板方法。
 */
class DemoActivity : BaseActivity<ActivityDemoBinding, BaseViewModel>() {

    private val menuAdapter = DemoMenuAdapter(::onEntrySelected)

    override fun createBinding(inflater: LayoutInflater) = ActivityDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.rvMenu.adapter = menuAdapter
        menuAdapter.submitList(DemoMenu.rows(this))
        // XML 声明的 GridLayoutManager(spanCount=2)在此补跨列规则:标题占整行,入口各占一列
        (binding.rvMenu.layoutManager as? GridLayoutManager)?.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) =
                    if (menuAdapter.isHeaderAt(position)) 2 else 1
            }
    }

    private fun onEntrySelected(entry: DemoEntry) {
        // action 优先(页内动作),否则按 Class 跳转
        entry.action?.invoke() ?: entry.target?.let { startActivity(Intent(this, it)) }
    }

    /**
     * 演示统一异常通道:业务只上报,提示由基类 [onApiException] 完成。
     * 由 [DemoMenu] 中的"全局异常通道"入口调用。
     */
    fun showGlobalExceptionDemo() {
        viewModel.postApiException(
            ApiException(ApiExceptionExt.CODE_NET_ERROR, "这是一条走全局通道的模拟异常")
        )
    }

    /** 重写基类钩子:验证"自定义全局异常处理"能力(默认实现是裸 Toast) */
    override fun onApiException(exception: ApiException) {
        showToast("全局异常通道[code=${exception.code}] → ${exception.message}")
    }

    /** 本页无网络请求,不需要 loading 表现 */
    override fun showLoading(show: Boolean) = Unit
}
