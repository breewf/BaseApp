package com.hy.myapp.demo.result

import android.os.Bundle
import android.view.LayoutInflater
import com.hy.basic.BaseActivity
import com.hy.myapp.databinding.ActivityResultFormBinding

/**
 * desc: ActivityResult 演示的"表单回传页" —— 演示 [UiEvent.ShowResult] 事件。
 *
 * 被 [ActivityResultDemoActivity] 用 `awaitResult` 拉起,提交时不直接碰 Activity API:
 * ViewModel 只发一条 [UiEvent.ShowResult],基类自动 `setResult + finish`,
 * 拉起方的 `awaitResultBundle` 就能拿到 (resultCode, extras)。
 *
 * 「放弃并返回」发 [UiEvent.GoBack]:走返回分发器,不 setResult,
 * 拉起方收到 CANCELED(awaitResultBundle 归一为 null)。
 */
class ActivityResultFormActivity : BaseActivity<ActivityResultFormBinding, ResultFormViewModel>() {

    override fun createBinding(inflater: LayoutInflater) =
        ActivityResultFormBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        binding.btnSubmit.setOnClickListener {
            viewModel.submit(
                title = binding.etTitle.text.toString().trim(),
                remark = binding.etRemark.text.toString().trim(),
            )
        }
        binding.btnGiveUp.setOnClickListener { viewModel.giveUp() }
    }
}
