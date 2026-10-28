package com.hy.demo.skin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.skin.SkinManager
import com.hy.myapp.databinding.FragmentSkinListBinding
import com.hy.demo.adapter.DemoMenuAdapter
import com.hy.demo.model.DemoEntry
import com.hy.demo.model.DemoRow

/** 换肤验证 Fragment 二:列表复用与 Canvas 自绘 */
class SkinListFragment : BaseFragment<FragmentSkinListBinding, BaseViewModel>() {

    /** 复用首页卡片适配器,验证 item 复用也不串色 */
    private val listAdapter = DemoMenuAdapter { showToast(it.title) }

    /** 加载布局 */
    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentSkinListBinding.inflate(inflater, container, false)

    /** 绑定换肤开关与列表数据 */
    override fun initView(savedInstanceState: Bundle?) {
        binding.tvToggle.setOnClickListener { SkinManager.toggle() }
        binding.rvList.adapter = listAdapter
        listAdapter.submitList(rows())
    }

    /** 列表数据 */
    private fun rows(): List<DemoRow> =
        (1..20).map {
            DemoRow.Entry(DemoEntry("Fragment 条目 $it", "切肤后滚动列表，颜色应保持一致"))
        }
}
