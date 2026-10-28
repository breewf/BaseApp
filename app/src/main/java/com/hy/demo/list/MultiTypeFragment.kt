package com.hy.demo.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.utils.addDivider
import com.hy.basic.utils.attachAdapter
import com.hy.basic.utils.resolvePagerConflict
import com.hy.basic.utils.setupLinear
import com.hy.myapp.databinding.FragmentListMultitypeBinding
import com.hy.demo.list.adapter.MultiTypeAdapter

/**
 * desc: 多类型适配器演示页 —— 一个列表同时混排"标题 / 图文卡片 / 纯文本便签"三种 item。
 *
 * 装配与竖向页一致(`setupLinear().addDivider().attachAdapter`),差别只在适配器换成
 * [MultiTypeAdapter](BRVAH4 [com.chad.library.adapter4.BaseMultiItemAdapter]),
 * 数据为本地混排假数据,点击 item 用 Toast 演示多类型下的点击回调照样可用。
 */
class MultiTypeFragment :
    BaseFragment<FragmentListMultitypeBinding, BaseViewModel>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentListMultitypeBinding.inflate(inflater, container, false)

    override fun initView(savedInstanceState: Bundle?) {
        val adapter = binding.rvMulti
            .setupLinear()
            .addDivider()
            .resolvePagerConflict()
            .attachAdapter(MultiTypeAdapter())
        adapter.submitList(ListFakeData.multiItems())

        adapter.setOnItemClickListener { _, _, position ->
            when (val item = adapter.items.getOrNull(position)) {
                is MultiItem.Header -> showToast("标题:${item.title}")
                is MultiItem.Article -> showToast("图文:${item.title}")
                is MultiItem.Note -> showToast("便签:${item.content}")
                null -> Unit
            }
        }
    }
}
