package com.hy.myapp.demo.palette

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.hy.basic.BaseViewModel
import com.hy.basic.network.ApiException
import com.hy.basic.utils.ImagePalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * desc: 图片吸色演示页 ViewModel
 *
 * 示范"取色"这种非网络异步任务如何复用同一套基类能力:
 * - 用 [startLoading]/[stopLoading] 手动驱动局部 loading(取色不是网络请求,不走 launchRequest);
 * - 取色逻辑全部委托给 [ImagePalette.fromImage](挂起、后台线程、带缓存),ViewModel 只做编排;
 * - 结果用 [MutableStateFlow] 暴露,页面 repeatOnLifecycle 收集;
 * - 图片加载失败(返回 null)走 [postApiException],由基类统一 Toast,页面零提示代码。
 */
class PaletteThemeViewModel : BaseViewModel() {

    private val _theme = MutableStateFlow<ImagePalette.Result?>(null)

    /** 当前封面的取色结果(null 表示尚未成功) */
    val theme = _theme.asStateFlow()

    private val _selectedIndex = MutableStateFlow(-1)

    /** 当前选中的封面下标,用于缩略图高亮 */
    val selectedIndex = _selectedIndex.asStateFlow()

    /**
     * 对指定封面取色。
     *
     * @param model 图片模型(url / resId),同时作为缓存 key
     */
    fun loadPalette(context: Context, model: Any, index: Int) = viewModelScope.launch {
        startLoading()
        val result = ImagePalette.fromImage(context, model)
        stopLoading()
        if (result == null) {
            postApiException(ApiException(null, "图片加载失败,无法取色"))
            return@launch
        }
        _selectedIndex.value = index
        _theme.value = result
    }
}
