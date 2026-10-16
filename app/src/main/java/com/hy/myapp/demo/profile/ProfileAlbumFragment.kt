package com.hy.myapp.demo.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hy.basic.BaseFragment
import com.hy.basic.BaseViewModel
import com.hy.basic.utils.attachAdapter
import com.hy.basic.utils.resolvePagerConflict
import com.hy.basic.utils.setupGrid
import com.hy.myapp.R
import com.hy.myapp.databinding.FragmentProfileAlbumBinding
import com.hy.myapp.demo.profile.adapter.ProfileAlbumAdapter

/**
 * desc: Tab③「相册」—— 3 列网格。
 *
 * 网格同样在横向 pager 页内,竖向为主的手势用 [resolvePagerConflict] 保住列表滚动;
 * 图片 model 网络 url 与本地 resId 混排,统一交给 [com.hy.basic.utils.ImageLoader]。
 */
class ProfileAlbumFragment : BaseFragment<FragmentProfileAlbumBinding, BaseViewModel>() {

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentProfileAlbumBinding.inflate(inflater, container, false)

    private val adapter by lazy { ProfileAlbumAdapter() }

    override fun initView(savedInstanceState: Bundle?) {
        binding.rvAlbum.setupGrid(3).resolvePagerConflict().attachAdapter(adapter)
        adapter.setOnItemClickListener { _, _, position ->
            showToast("看大图:第 ${position + 1} 张")
        }
    }

    override fun initData() {
        adapter.submitList(ALBUM)
    }

    private companion object {
        /** 12 张:网络图与本地资源交替,演示 ImageLoader 双 model 混排 */
        val ALBUM = listOf<Any>(
            "https://www.gstatic.com/webp/gallery/1.jpg",
            R.mipmap.img_lab_view1,
            "https://www.gstatic.com/webp/gallery/2.jpg",
            R.mipmap.img_lab_view2,
            "https://www.gstatic.com/webp/gallery/3.jpg",
            R.mipmap.bg_home_daily,
            "https://www.gstatic.com/webp/gallery/4.jpg",
            R.mipmap.bg_home_fav,
            "https://www.gstatic.com/webp/gallery/5.jpg",
            R.mipmap.bg_home_like,
            "https://www.gstatic.com/webp/gallery/1.jpg",
            R.mipmap.ic_img_profile_bg,
        )
    }
}
