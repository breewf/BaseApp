package com.hy.demo.result

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.hy.basic.BaseActivity
import com.hy.basic.LauncherSpec
import com.hy.basic.awaitImagePicker
import com.hy.basic.awaitResult
import com.hy.basic.awaitResultBundle
import com.hy.basic.intentOf
import com.hy.basic.permission.Permission
import com.hy.basic.permission.awaitPermissions
import com.hy.myapp.databinding.ActivityResultDemoBinding
import kotlinx.coroutines.launch

/**
 * desc: ActivityResult 封装 + 统一事件通道 演示页。
 *
 * 验证 [com.hy.basic.LauncherSpec] 体系(注册时机由无 UI 宿主代持,点击里直接挂起等结果):
 * - 拉起飞页等回传:表单页提交时只发 [com.hy.basic.page.UiEvent.ShowResult] 事件,
 *   本页 `awaitResultBundle` 一行拿到 (resultCode, extras);
 * - 系统相册选图(GetContent,免权限);
 * - 相机直拍(TakePicture,FileProvider 已配,取消归一 null);
 * - 系统分享面板(Chooser,用户是否完成不可靠,ROM 已知语义)。
 *
 * 同时验证 [com.hy.basic.page.UiEvent] 统一事件通道:
 * - 事件跳转:ViewModel 发 Navigate goto(表单页),基类 onNavigate 组装启动;
 * - 事件 Toast:表单页提交路径全程零 Toast/零 finish 代码。
 */
class ActivityResultDemoActivity : BaseActivity<ActivityResultDemoBinding, ResultDemoViewModel>() {

    override val showToolBar: Boolean get() = true
    override val toolbarTitle: CharSequence get() = "页面结果回传"

    override fun createBinding(inflater: LayoutInflater) =
        ActivityResultDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        // 1) 拉起表单页并等回传:取消/放弃返回 null
        binding.btnForm.setOnClickListener {
            lifecycleScope.launch {
                val outcome = awaitResultBundle(
                    intentOf(this@ActivityResultDemoActivity, ActivityResultFormActivity::class.java)
                )
                binding.tvResult.text = if (outcome == null) {
                    "表单页:已放弃(CANCELED)"
                } else {
                    val (code, extras) = outcome
                    "表单页回传:code=$code\n" +
                        "title=${extras?.getString("title")}\n" +
                        "remark=${extras?.getString("remark")}\n" +
                        "submitAt=${extras?.getLong("submitAt")}"
                }
            }
        }

        // 2) 系统相册选一张图(免存储权限)
        binding.btnPickImage.setOnClickListener {
            lifecycleScope.launch {
                val uri = awaitImagePicker()
                binding.tvResult.text = if (uri == null) "选图:已取消" else "选图:$uri"
            }
        }

        // 3) 相机直拍:产出 FileProvider Uri,取消归一 null。
        // 部分 ROM 拉起系统相机要求本应用已持有 CAMERA,缺权限不是优雅取消而是直接
        // SecurityException 崩溃,所以走协程顺序写法:先权限后拉起(与权限演示页场景 4 同款)
        binding.btnCamera.setOnClickListener {
            lifecycleScope.launch {
                val permission = awaitPermissions(Permission.CAMERA)
                if (!permission.allGranted) {
                    binding.tvResult.text = "拍照:未授予相机权限,无法拉起系统相机"
                    return@launch
                }
                val uri = awaitResult(LauncherSpec.TakePicture()) as? Uri
                binding.tvResult.text = if (uri == null) "拍照:已取消" else "拍照:$uri"
            }
        }

        // 4) 系统分享面板
        binding.btnShare.setOnClickListener {
            lifecycleScope.launch {
                awaitResult(LauncherSpec.ShareText("来自 BaseApp 的 ActivityResult 演示"))
                binding.tvResult.text = "分享面板已关闭(用户是否完成分享在 ROM 间不可靠)"
            }
        }

        // 5) 走统一事件通道跳转:ViewModel 发 Navigate,基类 onNavigate 组装 Intent
        binding.btnEventNav.setOnClickListener { viewModel.navigateByEvent() }
    }
}
