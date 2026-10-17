package com.hy.myapp.demo.result

import com.hy.basic.BaseViewModel
import com.hy.basic.network.ApiException
import com.hy.basic.page.UiEvent

/**
 * desc: 表单回传页 ViewModel —— 演示"业务只发事件,不碰 Activity"。
 *
 * [submit] 校验后发 [UiEvent.ShowResult](基类自动 setResult + finish 回传 extras);
 * 校验失败发 [UiEvent.ShowError] 走统一错误通道;
 * [giveUp] 发 [UiEvent.GoBack],拉起方按 CANCELED 处理。
 */
class ResultFormViewModel : BaseViewModel() {

    fun submit(title: String, remark: String) {
        if (title.isEmpty()) {
            postApiException(ApiException(null, "标题不能为空"))
            return
        }
        emitUiEvent(
            UiEvent.ShowResult(
                values = mapOf(
                    "title" to title,
                    "remark" to remark,
                    "submitAt" to System.currentTimeMillis(),
                ),
            )
        )
    }

    fun giveUp() {
        emitUiEvent(UiEvent.GoBack)
    }
}
