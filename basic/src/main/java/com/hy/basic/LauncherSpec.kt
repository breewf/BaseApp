package com.hy.basic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.io.File
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * desc: ActivityResult 启动谱 —— 把「注册时机」问题一次性解决的声明式描述。
 *
 * 官方约束:`registerForActivityResult` 必须在 STARTED 之前调用,而业务跳转几乎都在点击回调里。
 * 本方案由无 UI 的 [ResultHostFragment] 代持 launcher,每个谱键专属一个宿主,
 * 在 onAttach 时机注册,之后 launch 全部复用。页面只在点击里写一行:
 * ```
 * val (code, extras) = awaitResultBundle(intentOf(EditActivity::class.java)) ?: return@launch
 * val uri = awaitImagePicker()
 * ```
 */
sealed class LauncherSpec {

    /**
     * 宿主内部:类型擦除的统一 contract。产出物按谱不同:
     * [StartActivity]/[ShareText]/[IntentSender] 给 [ActivityResult],
     * 选择类给 [Uri] / `List<Uri>` / Boolean,均经 [Wrapped] 的 parseResult 归一。
     */
    internal abstract fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?>

    /** 未显式传 input 时的默认入参(选择类为 MIME 过滤,文档类为建议文件名) */
    internal open fun defaultInput(): Any? = null

    /** 通用逃生门:包装任意 [ActivityResultContract],input 传 null 时走默认值 */
    class Custom<I, O>(
        private val contract: ActivityResultContract<I, O>,
        private val input: I,
    ) : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> = Wrapped(contract)
        override fun defaultInput(): Any? = input
    }

    /**
     * 拉起一个 Activity(Intent 自备,可指向任意目标)并等待返回 ——
     * 目标页用 `setResult` 或基类事件 [com.hy.basic.page.UiEvent.ShowResult] 回传,
     * 这里拿到完整 [ActivityResult](含 resultCode 与数据 Intent)。
     */
    class StartActivity(val intent: Intent) : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> =
            Wrapped(object : ActivityResultContract<Intent, ActivityResult>() {
                override fun createIntent(context: Context, input: Intent): Intent = input
                override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult =
                    ActivityResult(resultCode, intent)
            })

        override fun defaultInput(): Any? = intent
    }

    /** 打开系统设置等需要 IntentSender 的场景;产出 [ActivityResult] */
    class IntentSender(val request: IntentSenderRequest) : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> =
            Wrapped(ActivityResultContracts.StartIntentSenderForResult())

        override fun defaultInput(): Any? = request
    }

    /** 选一张媒体图片(系统相册/文档,免权限);产出 [Uri],缺省按图片 MIME 通配过滤 */
    data object PickImage : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> =
            Wrapped(ActivityResultContracts.GetContent())

        override fun defaultInput(): Any? = "image/*"
    }

    /** [PickImage] 的指定类型版([mimeType] 如图片通配或 "application/pdf");产出 [Uri] */
    class GetContent(val mimeType: String) : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> =
            Wrapped(ActivityResultContracts.GetContent())

        override fun defaultInput(): Any? = mimeType
    }

    /** 选多张媒体图片;产出 `List<Uri>` */
    data object PickImages : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> =
            Wrapped(ActivityResultContracts.GetMultipleContents())

        override fun defaultInput(): Any? = "image/*"
    }

    /** 系统照片选择器选视频(免权限);产出 [Uri] */
    data object PickVideo : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> =
            Wrapped(ActivityResultContracts.GetContent())

        override fun defaultInput(): Any? = "video/*"
    }

    /** 选取任意类型文档(系统授予持久读权限);产出 [Uri] */
    data object OpenDocument : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> =
            Wrapped(ActivityResultContracts.OpenDocument())

        override fun defaultInput(): Any? = arrayOf("*/*")
    }

    /** 创建并打开写入文档([suggestedName] 为默认文件名);产出写入位置 [Uri] */
    class CreateDocument(val mimeType: String, val suggestedName: String) : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> =
            Wrapped(ActivityResultContracts.CreateDocument(mimeType))

        override fun defaultInput(): Any? = suggestedName
    }

    /**
     * 系统相机直拍:产出照片 [Uri](自动经 FileProvider 落到缓存目录,免存储权限)。
     * Manifest 需声明 authority 为 `包名.fileprovider` 的 FileProvider。
     * 注意:部分机型拉起系统相机要求已持有 CAMERA 权限,缺权限会抛 SecurityException,
     * 发起前务必先 `awaitPermissions(Permission.CAMERA)`。
     */
    class TakePicture(val outputFile: File? = null) : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> =
            Wrapped(object : ActivityResultContract<Uri, Uri?>() {
                private var target: Uri? = null
                private val inner = ActivityResultContracts.TakePicture()

                override fun createIntent(context: Context, input: Uri): Intent {
                    target = input
                    return inner.createIntent(context, input)
                }

                // 取消时 inner 回调 false,不产出 Uri,调用侧按 null 分支即可
                override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
                    if (inner.parseResult(resultCode, intent)) target else null
            })

        override fun defaultInput(): Any? = null

        /** 宿主内部:launch 前把目标文件经 FileProvider 转成 Uri 入参 */
        internal fun targetUri(app: Context): Uri {
            val file = outputFile ?: File(app.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
            file.parentFile?.mkdirs()
            return FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
        }
    }

    /** 系统分享面板(纯文本);用户是否完成分享在 ROM 间不可靠,产出 [ActivityResult] */
    class ShareText(val text: CharSequence) : LauncherSpec() {
        override fun wrappedContract(app: Context): ActivityResultContract<Any?, Any?> =
            Wrapped(object : ActivityResultContract<Intent, ActivityResult>() {
                override fun createIntent(context: Context, input: Intent): Intent = input
                override fun parseResult(resultCode: Int, intent: Intent?): ActivityResult =
                    ActivityResult(resultCode, intent)
            })

        override fun defaultInput(): Any? = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }
}

/** 把任意 contract 适配成 `Contract<Any?, Any?>`,供宿主按统一类型注册/缓存 */
private class Wrapped<I, O>(private val inner: ActivityResultContract<I, O>) :
    ActivityResultContract<Any?, Any?>() {
    @Suppress("UNCHECKED_CAST")
    override fun createIntent(context: Context, input: Any?): Intent =
        inner.createIntent(context, input as I)

    @Suppress("UNCHECKED_CAST")
    override fun parseResult(resultCode: Int, intent: Intent?): Any? =
        inner.parseResult(resultCode, intent)
}

/** 组装指向 App 内页面的 Intent,供 [LauncherSpec.StartActivity] / [awaitResultBundle] 使用 */
fun intentOf(
    context: Context,
    activityClass: Class<out FragmentActivity>,
    block: Intent.() -> Unit = {},
): Intent = Intent(context, activityClass).apply(block)

/**
 * 无 UI 的 ActivityResult 宿主,代持 launcher。新版 fragment 禁止 created 后注册,
 * 因此每个谱键一个宿主,在 onAttach 时机注册,之后复用。
 */
internal class ResultHostFragment : Fragment() {

    private var boundKey: String? = null
    private var boundContract: ActivityResultContract<Any?, Any?>? = null
    private val launchers = HashMap<String, ActivityResultLauncher<Any?>>()
    private val pending = HashMap<String, CancellableContinuation<Any?>>()

    /** add 之前调用:告诉宿主要在 attach 时注册哪个 launcher */
    fun bind(key: String, contract: ActivityResultContract<Any?, Any?>) {
        boundKey = key
        boundContract = contract
    }

    /** 返回 false 说明这是进程重建恢复的实例,调用侧需换建 */
    fun handles(key: String): Boolean = launchers.containsKey(key)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val key = boundKey ?: return
        val contract = boundContract ?: return
        if (!launchers.containsKey(key)) {
            @Suppress("UNCHECKED_CAST")
            launchers[key] = registerForActivityResult(contract) { output ->
                // 同谱并发时后来者覆盖:只回调当前等待中的那一个续体
                val waiting = pending.remove(key)
                if (waiting != null && waiting.isActive) {
                    waiting.resumeWith(Result.success(output))
                }
            } as ActivityResultLauncher<Any?>
        }
    }

    /** 发起并挂起等待结果;宿主状态已保存时返回 null */
    suspend fun launch(spec: LauncherSpec, explicitInput: Any? = null): Any? {
        val app = requireContext().applicationContext
        val key = keyOf(spec)
        val input = explicitInput ?: when (spec) {
            // 相机直拍的入参是 FileProvider 产出的目标 Uri
            is LauncherSpec.TakePicture -> spec.targetUri(app)
            else -> spec.defaultInput()
        }
        return suspendCancellableCoroutine { cont ->
            pending[key] = cont
            cont.invokeOnCancellation { if (pending[key] === cont) pending.remove(key) }
            launchers.getValue(key).launch(input)
        }
    }
}

/** 同一个谱复用同一 launcher:内置谱按类型(StartActivity 各实例共享),Custom 按实例 */
internal fun keyOf(spec: LauncherSpec): String = when (spec) {
    is LauncherSpec.Custom<*, *> -> "custom_" + System.identityHashCode(spec)
    is LauncherSpec.StartActivity -> "start_activity"
    else -> spec.javaClass.name
}

// ==== 对外 API:页面里一行完成「拉起 -> 等结果」 ====

/**
 * 挂起拉起 [LauncherSpec] 并等待结果,原样返回 contract 产出
 * ([ActivityResult] / [Uri] / Boolean / `List<Uri>`,高频场景优先用下面的 typed 帮助函数)。
 * [input] 传 null 走各谱默认入参。宿主状态已保存时返回 null,点击回调里可直接调用。
 */
suspend fun FragmentActivity.awaitResult(spec: LauncherSpec, input: Any? = null): Any? =
    resultHost(spec)?.launch(spec, input)

/** [awaitResult] 的 Fragment 版,宿主挂在自身 childFragmentManager 上,与页面其他申请互不串扰 */
suspend fun Fragment.awaitResult(spec: LauncherSpec, input: Any? = null): Any? =
    resultHost(spec)?.launch(spec, input)

/** 常用捷径:拉起飞页并等返回;CANCELED(含目标页进程被杀)返回 null,否则给出 (resultCode, 数据 Bundle,目标页无数据时为 null) */
suspend fun FragmentActivity.awaitResultBundle(intent: Intent): Pair<Int, Bundle?>? {
    val spec = LauncherSpec.StartActivity(intent)
    return (resultHost(spec)?.launch(spec) as? ActivityResult)
        ?.takeIf { it.resultCode != Activity.RESULT_CANCELED }
        ?.let { it.resultCode to it.data?.extras }
}

/** [awaitResultBundle] 的 Fragment 版 */
suspend fun Fragment.awaitResultBundle(intent: Intent): Pair<Int, Bundle?>? {
    val spec = LauncherSpec.StartActivity(intent)
    return (resultHost(spec)?.launch(spec) as? ActivityResult)
        ?.takeIf { it.resultCode != Activity.RESULT_CANCELED }
        ?.let { it.resultCode to it.data?.extras }
}

/** 常用捷径:选一张图片直接拿 [Uri](取消返回 null);[mimeType] 缺省按图片 MIME 通配过滤 */
suspend fun FragmentActivity.awaitImagePicker(mimeType: String = "image/*"): Uri? {
    val spec = LauncherSpec.GetContent(mimeType)
    return resultHost(spec)?.launch(spec) as? Uri
}

/** [awaitImagePicker] 的 Fragment 版 */
suspend fun Fragment.awaitImagePicker(mimeType: String = "image/*"): Uri? {
    val spec = LauncherSpec.GetContent(mimeType)
    return resultHost(spec)?.launch(spec) as? Uri
}

/** typed 版:声明返回类型并强转 contract 产出,省掉调用处 `as` */
suspend fun <O> FragmentActivity.awaitTypedResult(spec: LauncherSpec, input: Any? = null): O? =
    @Suppress("UNCHECKED_CAST")
    (resultHost(spec)?.launch(spec, input) as O?)

/** [awaitTypedResult] 的 Fragment 版 */
suspend fun <O> Fragment.awaitTypedResult(spec: LauncherSpec, input: Any? = null): O? =
    @Suppress("UNCHECKED_CAST")
    (resultHost(spec)?.launch(spec, input) as O?)

internal fun FragmentActivity.resultHost(spec: LauncherSpec): ResultHostFragment? =
    supportFragmentManager.attachResultHost(spec, applicationContext)

internal fun Fragment.resultHost(spec: LauncherSpec): ResultHostFragment? =
    takeIf { isAdded }?.childFragmentManager
        ?.attachResultHost(spec, requireContext().applicationContext)

private const val HOST_TAG = "com.hy.basic.ResultHostFragment"

private fun FragmentManager.attachResultHost(spec: LauncherSpec, app: Context): ResultHostFragment? {
    if (isStateSaved) return null
    val key = keyOf(spec)
    val tag = "$HOST_TAG/$key"
    (findFragmentByTag(tag) as? ResultHostFragment)?.let {
        if (it.handles(key)) return it
        // 进程重建后随页面恢复的宿主丢了注入信息(恢复只会走空参构造):移除换建
        beginTransaction().remove(it).commitNow()
    }
    return ResultHostFragment().apply { bind(key, spec.wrappedContract(app)) }.also {
        beginTransaction().add(it, tag).commitNow()
    }
}
