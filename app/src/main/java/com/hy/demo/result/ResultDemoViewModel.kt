package com.hy.demo.result

import com.hy.basic.BaseViewModel
import com.hy.basic.page.UiEvent

/**
 * desc: ActivityResult 演示主页 ViewModel —— 演示 [UiEvent.Navigate] 事件跳转。
 *
 * 页面跳转是典型的一次性事件:发一条 [UiEvent.Navigate],由基类 `onNavigate`
 * 组装 Intent 启动;转屏/重订阅不会被重放(与 StateFlow 的区别正在于此)。
 */
class ResultDemoViewModel : BaseViewModel() {

    /** 走统一事件通道打开表单页(不等结果,表单页自己「放弃并返回」) */
    fun navigateByEvent() {
        emitUiEvent(
            UiEvent.Navigate.goto(ActivityResultFormActivity::class.java)
        )
        emitUiEvent(UiEvent.ShowToast("已发出 Navigate 事件,跳转由基类 onNavigate 完成"))
    }
}
