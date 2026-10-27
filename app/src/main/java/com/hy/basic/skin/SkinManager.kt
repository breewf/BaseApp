package com.hy.basic.skin

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.AnyRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.hy.basic.storage.MMKVStore
import com.hy.myapp.R
import java.util.WeakHashMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 换肤入口:颜色取自 values-night,切换时遍历 View 树就地刷新,不重建页面。
 *
 * 页面继承 [BaseSkinActivity]、颜色用 skin_xxx 即可;
 * 切到 [SkinEngine.COMPAT] 则退回官方 AppCompatDelegate + recreate。
 */
object SkinManager {

    /** 模式存储键 */
    private const val KEY_MODE = "skin.mode"

    /** 官方引擎标记,用于下次冷启动复位 AppCompat */
    private const val KEY_COMPAT = "skin.compat"

    /** 应用 Context */
    private var appContext: Context? = null

    /** 是否已初始化 */
    private var initialized = false

    /** 当前模式 */
    private var currentMode = SkinMode.FOLLOW_SYSTEM

    /** 当前资源来源 */
    private var currentSource: SkinSource? = null

    /** 已注册的页面根 View */
    private val roots = WeakHashMap<View, Boolean>()

    /** 深色状态源,对外暴露 [isDarkFlow] */
    private val _isDark = MutableStateFlow(false)

    /** 当前是否深色,订阅即拿到当前值 */
    val isDarkFlow: StateFlow<Boolean> = _isDark.asStateFlow()

    /** 当前模式 */
    val mode: SkinMode get() = currentMode

    /** 当前是否深色,未初始化时按模式推算 */
    val isDark: Boolean get() = currentSource?.isDark ?: computeDark()

    // ==== 初始化 ====

    /** 在 Application.onCreate 调一次,读模式并处理官方引擎残留 */
    @JvmStatic
    fun initialize(context: Context) {
        ensureInit(context)
        if (SkinConfig.engine == SkinEngine.COMPAT) {
            applyCompat()
        } else if (readCompatFlag()) {
            // 上次用过官方引擎,把 AppCompat 的夜间配置交还系统
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            persistCompatFlag(false)
        }
    }

    /** 包装 inflater,需在 super.onCreate 之前调用;两种引擎都记槽位,官方引擎下不套色 */
    @MainThread
    fun install(inflater: LayoutInflater) {
        if (!SkinConfig.enabled) return
        ensureInit(inflater.context)
        SkinInflater.install(inflater)
    }

    /** 懒初始化,重复调用无副作用 */
    private fun ensureInit(context: Context) {
        if (initialized) return
        val app = context.applicationContext
        appContext = app
        currentMode = readMode()
        val source = NightSkinSource(app).apply { applyMode(computeDark()) }
        currentSource = source
        _isDark.value = source.isDark
        app.registerComponentCallbacks(configCallbacks)
        initialized = true
    }

    /** 配置变化监听 */
    private val configCallbacks = object : android.content.ComponentCallbacks {
        /** 清缓存,跟随系统时刷新 */
        override fun onConfigurationChanged(newConfig: Configuration) {
            currentSource?.invalidate()
            if (currentMode == SkinMode.FOLLOW_SYSTEM) {
                currentSource?.applyMode(computeDark())
                refreshAllNow()
                _isDark.value = isDark
            }
        }

        /** 内存回调无需处理 */
        @Deprecated("Deprecated in Java", ReplaceWith("Unit"))
        override fun onLowMemory() = Unit
    }

    // ==== 切换 ====

    /** 切肤,模式没变直接返回 */
    @JvmStatic
    @MainThread
    fun switchTo(target: SkinMode) {
        if (target == currentMode) return
        currentMode = target
        persistMode(target)
        currentSource?.applyMode(computeDark())
        if (SkinConfig.engine == SkinEngine.COMPAT) {
            _isDark.value = isDark
            applyCompat()
            return
        }
        refreshAllNow()
        _isDark.value = isDark
    }

    /** 深/浅来回切 */
    @JvmStatic
    fun toggle() = switchTo(if (isDark) SkinMode.LIGHT else SkinMode.DARK)

    /** 切换引擎,切到官方会重建页面 */
    @JvmStatic
    @MainThread
    fun setEngine(engine: SkinEngine) {
        if (SkinConfig.engine == engine) return
        SkinConfig.engine = engine
        currentSource?.applyMode(computeDark())
        _isDark.value = isDark
        if (engine == SkinEngine.COMPAT) {
            applyCompat()
            return
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        persistCompatFlag(false)
        refreshAllNow()
    }

    /** 开关框架 */
    @JvmStatic
    fun setEnabled(enabled: Boolean) {
        SkinConfig.enabled = enabled
    }

    /** 替换资源来源,节日皮肤入口 */
    @MainThread
    fun setSource(source: SkinSource) {
        currentSource = source
        source.applyMode(computeDark())
        refreshAllNow()
        _isDark.value = source.isDark
    }

    /** 手动刷新已注册页面 */
    @MainThread
    fun refresh() {
        refreshAllNow()
        _isDark.value = isDark
    }

    // ==== 页面注册 ====

    /** 注册页面根 View,立即套一遍 */
    @MainThread
    fun register(root: View) {
        if (!SkinConfig.enabled) return
        roots[root] = true
        applyTo(root)
    }

    /** 反注册 */
    @MainThread
    fun unregister(root: View) {
        roots.remove(root)
    }

    /** 退出自动换肤,颜色完全自己控制(如基类壳里的 Toolbar) */
    @MainThread
    fun ignore(view: View) {
        view.setTag(R.id.skin_slot_tag, null)
    }

    /**
     * 代码里切换控件形态:更新属性槽位并立即生效。
     * 之后切肤会继续使用这里设置的资源,不会被打回 XML 默认值。
     */
    @MainThread
    fun setSkinRes(view: View, part: SkinPart, @AnyRes resId: Int) {
        if (resId == 0) return
        ensureInit(view.context)

        @Suppress("UNCHECKED_CAST")
        val slots = view.getTag(R.id.skin_slot_tag) as? MutableList<SkinSlot>
            ?: mutableListOf<SkinSlot>().also { view.setTag(R.id.skin_slot_tag, it) }

        val slot = slots.firstOrNull { it.kind == part }
        val target = if (slot != null) {
            slot.resId = resId
            slot
        } else {
            SkinSlot(resId, part).also { slots.add(it) }
        }
        currentSource?.let { applySlot(view, it, target) }
    }

    // ==== 取色 ====

    /** 取当前皮肤色 */
    @JvmStatic
    fun color(@ColorRes id: Int): Int {
        currentSource?.let { return it.color(id) }
        val ctx = appContext ?: return 0
        return runCatching {
            ResourcesCompat.getColor(
                ctx.resources,
                id,
                ctx.theme
            )
        }.getOrDefault(0)
    }

    /** 取当前皮肤 ColorStateList */
    @JvmStatic
    fun colorStateList(@ColorRes id: Int): ColorStateList? =
        currentSource?.colorStateList(id)

    /** 取当前皮肤 Drawable */
    @JvmStatic
    fun drawable(@DrawableRes id: Int): Drawable? = currentSource?.drawable(id)

    // ==== 刷新 ====

    /** 递归套色:改属性、回调 IDarkMode、给 RecyclerView 挂子 View 监听 */
    @MainThread
    fun applyTo(view: View?) {
        if (view == null || !SkinConfig.enabled) return
        if (SkinConfig.engine != SkinEngine.SELF) return
        val source = currentSource ?: return

        @Suppress("UNCHECKED_CAST")
        val slots = view.getTag(R.id.skin_slot_tag) as? List<SkinSlot> ?: emptyList()
        // 先快照:回调里调 setSkinRes 会改这个列表
        slots.toList().forEach { applySlot(view, source, it) }
        (view as? IDarkMode)?.onSkinChanged(source.isDark)
        if (view is RecyclerView) watchChildren(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) applyTo(view.getChildAt(i))
        }
    }

    /** View 创建后记槽位,自研引擎下立即套色 */
    internal fun attach(view: View, attrs: AttributeSet) {
        if (!SkinConfig.enabled) return
        val slots = SkinInflater.parse(view, attrs) ?: return
        view.setTag(R.id.skin_slot_tag, slots)
        if (SkinConfig.engine == SkinEngine.SELF) applyTo(view)
    }

    /** 刷新所有已注册页面 */
    private fun refreshAllNow() {
        if (!SkinConfig.enabled || SkinConfig.engine != SkinEngine.SELF) return
        roots.keys.toList().forEach { applyTo(it) }
    }

    /** 按槽位类型改对应属性 */
    private fun applySlot(view: View, source: SkinSource, slot: SkinSlot) {
        when (slot.kind) {
            SkinPart.TEXT_COLOR -> (view as? TextView)?.let { tv ->
                source.colorStateList(slot.resId)?.let { tv.setTextColor(it) }
            }

            SkinPart.TEXT_HINT -> (view as? TextView)?.let { tv ->
                source.colorStateList(slot.resId)?.let { tv.setHintTextColor(it) }
            }

            SkinPart.TEXT_COLOR_LINK -> (view as? TextView)?.let { tv ->
                source.colorStateList(slot.resId)?.let { tv.setLinkTextColor(it) }
            }

            SkinPart.BACKGROUND -> {
                val csl = source.colorStateList(slot.resId)
                if (csl != null && !csl.isStateful) {
                    view.background = ColorDrawable(csl.defaultColor)
                } else {
                    source.drawable(slot.resId)?.let { view.background = it.mutate() }
                }
            }

            SkinPart.FOREGROUND -> source.drawable(slot.resId)?.let {
                view.foreground = it.mutate()
            }

            SkinPart.SRC -> (view as? ImageView)?.let { iv ->
                source.drawable(slot.resId)?.let { iv.setImageDrawable(it.mutate()) }
            }

            SkinPart.IMAGE_TINT -> (view as? ImageView)?.let { iv ->
                source.colorStateList(slot.resId)?.let { iv.imageTintList = it }
            }

            SkinPart.BACKGROUND_TINT -> source.colorStateList(slot.resId)?.let {
                ViewCompat.setBackgroundTintList(view, it)
            }

            SkinPart.COMPOUND_DRAWABLE_START,
            SkinPart.COMPOUND_DRAWABLE_TOP,
            SkinPart.COMPOUND_DRAWABLE_END,
            SkinPart.COMPOUND_DRAWABLE_BOTTOM -> (view as? TextView)?.let { tv ->
                applyCompoundDrawable(tv, slot.kind, source.drawable(slot.resId)?.mutate())
            }

            SkinPart.PROGRESS_TINT -> (view as? ProgressBar)?.let { pb ->
                source.colorStateList(slot.resId)?.let { pb.progressTintList = it }
            }

            SkinPart.PROGRESS_BACKGROUND_TINT -> (view as? ProgressBar)?.let { pb ->
                source.colorStateList(slot.resId)?.let { pb.progressBackgroundTintList = it }
            }

            SkinPart.SECONDARY_PROGRESS_TINT -> (view as? ProgressBar)?.let { pb ->
                source.colorStateList(slot.resId)?.let { pb.secondaryProgressTintList = it }
            }

            SkinPart.INDETERMINATE_TINT -> (view as? ProgressBar)?.let { pb ->
                source.colorStateList(slot.resId)?.let { pb.indeterminateTintList = it }
            }

            SkinPart.THUMB_TINT -> (view as? SeekBar)?.let { sb ->
                source.colorStateList(slot.resId)?.let { sb.thumbTintList = it }
            }

            SkinPart.TICK_MARK_TINT -> (view as? SeekBar)?.let { sb ->
                source.colorStateList(slot.resId)?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        sb.tickMarkTintList = it
                    }
                }
            }

            SkinPart.BUTTON_TINT -> (view as? CompoundButton)?.let { btn ->
                source.colorStateList(slot.resId)?.let { btn.buttonTintList = it }
            }

            SkinPart.DIVIDER -> (view as? LinearLayout)?.let {
                source.drawable(slot.resId)?.let { d -> it.dividerDrawable = d.mutate() }
            }

            SkinPart.CARD_BG -> (view as? CardView)?.let { card ->
                source.colorStateList(slot.resId)?.let { card.setCardBackgroundColor(it) }
            }
        }
    }

    /** 复合图只改一个位置,其余保持原样 */
    private fun applyCompoundDrawable(tv: TextView, part: SkinPart, drawable: Drawable?) {
        val current = tv.compoundDrawablesRelative ?: return
        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
            if (part == SkinPart.COMPOUND_DRAWABLE_START) drawable else current.getOrNull(0),
            if (part == SkinPart.COMPOUND_DRAWABLE_TOP) drawable else current.getOrNull(1),
            if (part == SkinPart.COMPOUND_DRAWABLE_END) drawable else current.getOrNull(2),
            if (part == SkinPart.COMPOUND_DRAWABLE_BOTTOM) drawable else current.getOrNull(3),
        )
    }

    /** RecyclerView 子 View attach 时补色,避免复用串色 */
    private fun watchChildren(rv: RecyclerView) {
        if (rv.getTag(R.id.skin_rv_tag) != null) return
        rv.setTag(R.id.skin_rv_tag, true)
        rv.addOnChildAttachStateChangeListener(
            object : RecyclerView.OnChildAttachStateChangeListener {
                /** 子 View 上屏时补色 */
                override fun onChildViewAttachedToWindow(view: View) = applyTo(view)

                /** 离屏不用处理 */
                override fun onChildViewDetachedFromWindow(view: View) = Unit
            },
        )
    }

    // ==== 状态读写 ====

    /** 按模式算出当前是否深色 */
    private fun computeDark(): Boolean = when (currentMode) {
        SkinMode.DARK -> true
        SkinMode.LIGHT -> false
        SkinMode.FOLLOW_SYSTEM -> {
            val uiMode = appContext?.resources?.configuration?.uiMode
                ?: Configuration.UI_MODE_NIGHT_NO
            uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }
    }

    /** 交给 AppCompatDelegate 并记标记 */
    private fun applyCompat() {
        persistCompatFlag(true)
        AppCompatDelegate.setDefaultNightMode(currentMode.compatValue)
    }

    /** 读模式,脏值回落到跟随系统 */
    private fun readMode(): SkinMode {
        val raw = runCatching { MMKVStore.getInt(KEY_MODE, SkinMode.FOLLOW_SYSTEM.ordinal) }
            .getOrDefault(SkinMode.FOLLOW_SYSTEM.ordinal)
        return SkinMode.entries.getOrElse(raw) { SkinMode.FOLLOW_SYSTEM }
    }

    /** 存模式 */
    private fun persistMode(mode: SkinMode) =
        runCatching { MMKVStore.putInt(KEY_MODE, mode.ordinal) }

    /** 读官方引擎标记 */
    private fun readCompatFlag(): Boolean =
        runCatching { MMKVStore.getBoolean(KEY_COMPAT, false) }.getOrDefault(false)

    /** 存官方引擎标记 */
    private fun persistCompatFlag(applied: Boolean) =
        runCatching { MMKVStore.putBoolean(KEY_COMPAT, applied) }
}
