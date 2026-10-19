package com.hy.myapp.demo.permission

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hy.basic.BaseActivity
import com.hy.basic.permission.Permission
import com.hy.basic.permission.PermissionResult
import com.hy.basic.permission.PermissionState
import com.hy.basic.permission.awaitPermissions
import com.hy.basic.permission.permissionLabel
import com.hy.basic.permission.permissionState
import com.hy.basic.permission.requestPermissions
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.databinding.ActivityPermissionDemoBinding
import kotlinx.coroutines.launch

/**
 * desc: 运行时权限用法示例。全页没有一行注册模板代码,只有“申请→看结果→走分支”。
 * 单个权限用 awaitPermissions,一组权限用权限组自带版本自适应,
 * 不爱协程用 requestPermissions 回调式。另外演示拍照接业务和系统照片选择器免权限。
 */
class PermissionDemoActivity :
    BaseActivity<ActivityPermissionDemoBinding, PermissionDemoViewModel>() {

    override val showToolBar: Boolean get() = true

    override val toolbarTitle: CharSequence get() = "系统权限申请"

    /** 拍照预览结果(小 Bitmap) */
    private lateinit var takePicture: ActivityResultLauncher<Void?>

    /** 系统照片选择器结果(选中图片的 content Uri) */
    private lateinit var pickImage: ActivityResultLauncher<PickVisualMediaRequest>

    override fun createBinding(inflater: LayoutInflater) =
        ActivityPermissionDemoBinding.inflate(inflater)

    override fun initView(savedInstanceState: Bundle?) {
        // 注册在 onCreate 阶段完成(基类模板方法里),这是 registerForActivityResult 的合法时机
        takePicture = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap == null) {
                viewModel.logMessage("拍照被取消")
            } else {
                binding.ivPreview.setImageBitmap(bitmap)
                viewModel.logMessage("拍照成功：${bitmap.width}x${bitmap.height}")
            }
        }
        pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri == null) {
                viewModel.logMessage("未选择图片")
            } else {
                ImageLoader.loadImage(binding.ivPreview, uri)
                viewModel.logMessage("免权限选图成功：$uri")
            }
        }

        binding.btnCamera.setOnClickListener { askCameraStepByStep() }
        binding.btnMedia.setOnClickListener { askMedia() }
        binding.btnMulti.setOnClickListener { askMicrophoneAndNotification() }
        binding.btnShoot.setOnClickListener { askCameraThenShoot() }
        binding.btnPicker.setOnClickListener { pickImageWithoutPermission() }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.statusText.collect { binding.tvStatus.text = it } }
                launch { viewModel.logText.collect { binding.tvLog.text = it } }
            }
        }
    }

    /** 从设置页授权回来时刷新总览,能立刻看到状态从上次的"未授予"变成"已授予" */
    override fun onResume() {
        super.onResume()
        viewModel.refreshStatus(this)
    }

    /** 场景 1:标准的“先判断、再说明、后申请”。四个状态各对应一种 UI */
    private fun askCameraStepByStep() {
        val camera = Manifest.permission.CAMERA
        val label = permissionLabel(camera)
        when (permissionState(camera)) {
            PermissionState.GRANTED ->
                viewModel.logMessage("$label 已授予，本次不会弹窗，业务可直接执行")

            PermissionState.UNDECLARED ->
                viewModel.logMessage("$label 未在 AndroidManifest 声明，补上 uses-permission 再申请才有意义")

            PermissionState.DENIED_FOREVER ->
                showGoSettingsDialog("$label 权限已被关闭")

            PermissionState.DENIED -> showRationaleDialog(label) {
                requestPermissions(camera) { log(it, label) }
            }
        }
    }

    /** 场景 2:版本自适应权限组。业务只写“我要相册”,33+ 走媒体权限,更低版本自动回退存储权限 */
    private fun askMedia() = lifecycleScope.launch {
        log(awaitPermissions(Permission.MEDIA), "相册")
    }

    /** 场景 3:一次问多个权限,重点看“部分授予”。通知权限在 33 以下不是运行时权限 */
    private fun askMicrophoneAndNotification() = requestPermissions(
        Permission.MICROPHONE + Permission.NOTIFICATION,
    ) { result ->
        log(result, "麦克风 + 通知")
    }

    /** 场景 4:申请成功后立刻接业务 —— 协程顺序写法,不必把业务塞进回调里层层嵌套 */
    private fun askCameraThenShoot() = lifecycleScope.launch {
        val result = awaitPermissions(Permission.CAMERA)
        log(result, "相机（为拍照而申请）")
        if (result.allGranted) takePicture.launch(null)
    }

    /** 场景 5:能不要权限就不要权限。系统照片选择器零权限、零弹窗,只把选中的那一张授权给本应用 */
    private fun pickImageWithoutPermission() {
        viewModel.logMessage("系统照片选择器不需要任何运行时权限")
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    /** 统一结果处理:记录日志,并按"还能不能再问"决定是 Toast 还是跳设置 */
    private fun log(result: PermissionResult, title: String) {
        viewModel.logRequest(title, result)
        when {
            result.allGranted -> showToast("权限已就绪")
            result.needsSettings ->
                showGoSettingsDialog(result.deniedForever.joinToString("、") { permissionLabel(it) })
            else -> showToast("已拒绝，下次进入可以再问")
        }
    }

    /** 首次拒绝前的用途说明:系统弹窗只讲"允许/拒绝",用途得业务自己说清楚 */
    private fun showRationaleDialog(label: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("需要$label 权限")
            .setMessage("这个功能要用到$label。拒绝后下次仍能再申请；若勾选“禁止后不再询问”，就只能去设置里开启。")
            .setPositiveButton("继续申请") { _, _ -> onConfirm() }
            .setNegativeButton("算了", null)
            .show()
    }

    /** 永久拒绝的唯一出路:应用详情页(各 ROM 没有稳定的"权限子页"入口,统一停在这里) */
    private fun showGoSettingsDialog(title: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("系统不会再弹出授权框。到「设置 - 应用 - 权限」里手动开启，返回本页后状态会自动刷新。")
            .setPositiveButton("去设置") { _, _ -> Permission.openAppSettings(this) }
            .setNegativeButton("稍后再说", null)
            .show()
    }
}
