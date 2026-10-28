package com.hy.demo.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.utils.attachAdapter
import com.hy.basic.utils.resolvePagerConflict
import com.hy.basic.utils.setupWaterfall
import com.hy.myapp.databinding.FragmentListWaterfallBinding
import com.hy.demo.list.adapter.WaterfallAdapter

/**
 * desc: 瀑布流列表页 —— StaggeredGridLayoutManager 2 列,item 高度由内容自然撑开形成错落。
 *
 * 数据为本地假数据([ListFakeData.cards] 正文档数按 index 变化 + 色块高度微调),
 * 装配只需 `rv.setupWaterfall(2).attachAdapter(adapter)` 一行,错落逻辑在 [WaterfallAdapter] 绑定阶段完成。
 */
class WaterfallFragment :
    BaseFragment<FragmentListWaterfallBinding, BaseViewModel>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentListWaterfallBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        binding.rvWaterfall
            .setupWaterfall(spanCount = 2)
            .resolvePagerConflict()
            .attachAdapter(WaterfallAdapter().apply { submitList(ListFakeData.cards()) })
    }
}
