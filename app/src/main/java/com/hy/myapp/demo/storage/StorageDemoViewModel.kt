package com.hy.myapp.demo.storage

import android.content.Context
import com.hy.basic.BaseViewModel
import com.hy.basic.storage.MMKVStore
import com.hy.basic.storage.clearAll
import com.hy.basic.storage.getObject
import com.hy.basic.storage.int
import com.hy.basic.storage.putInt
import com.hy.basic.storage.putObj
import com.hy.basic.storage.putStr
import com.hy.basic.storage.sp
import com.hy.basic.storage.str
import com.hy.myapp.demo.storage.MmkvConfig.nickname
import com.hy.myapp.demo.storage.MmkvConfig.profile
import com.hy.myapp.demo.storage.MmkvConfig.tags
import com.hy.myapp.demo.storage.MmkvConfig.vip
import com.hy.myapp.demo.storage.MmkvConfig.age
import com.hy.myapp.demo.storage.MmkvConfig.launchCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 存进 MMKV 的 JSON 对象:验证任意对象序列化落盘 */
data class DemoProfile(
    val name: String = "张三",
    val level: Int = 1,
    val signature: String = "还没有签名",
    val scores: List<Int> = listOf(90, 85, 78),
)

/**
 * 类型化配置持有者:字段就是存储键,`by` 委托背后直接读写 MMKV,取值/赋值不用再手写 key。
 */
object MmkvConfig {
    var nickname: String by MMKVStore.stringProp("sd_nickname", "未命名")
    var age: Int by MMKVStore.intProp("sd_age", 18)
    var vip: Boolean by MMKVStore.booleanProp("sd_vip", false)
    var launchCount: Int by MMKVStore.intProp("sd_launch_count", 0)
    var tags: Set<String> by MMKVStore.stringSetProp("sd_tags", setOf("新手"))
    var profile: DemoProfile by MMKVStore.jsonProp("sd_profile", DemoProfile())
}

/**
 * desc: 本地持久化演示。上半区走 MMKV(属性委托),下半区走官方 SP(扩展函数)做同形对照。
 */
class StorageDemoViewModel : BaseViewModel() {

    private val _snapshot = MutableStateFlow("")
    val snapshot: StateFlow<String> = _snapshot.asStateFlow()

    private val _logText = MutableStateFlow("")
    val logText: StateFlow<String> = _logText.asStateFlow()

    private val lines = ArrayDeque<String>()

    init {
        // 每次进页面自增,验证跨进程/重启后计数仍累加(而非归零)
        launchCount += 1
        refresh()
    }

    /** 改基础类型:直接对委托字段赋值,写入即刻落盘 */
    fun writeBasic() {
        nickname = "用户${(1000..9999).random()}"
        age = (18..60).random()
        vip = !vip
        log("MMKV 写基础类型 → nickname/age/vip 已更新")
        refresh()
    }

    /** 存 JSON 对象:level+1 换签名,整对象序列化进 MMKV */
    fun writeObject() {
        profile = profile.copy(
            level = profile.level + 1,
            signature = "升级到了 Lv.${profile.level + 1}",
            scores = profile.scores + (70..100).random(),
        )
        log("MMKV 存对象 → 档案 JSON 落盘,Lv.${profile.level}")
        refresh()
    }

    /** 存 StringSet:追加一个标签 */
    fun addTag() {
        tags = tags + "标签${tags.size + 1}"
        log("MMKV 存 Set → 当前 ${tags.size} 个标签")
        refresh()
    }

    /** 从磁盘重读:委托每次取值都直连 MMKV,这里刷新展示证明值仍在 */
    fun reloadFromDisk() {
        refresh()
        log("重读磁盘 → 昵称「$nickname」/ 启动 $launchCount 次 / VIP=${vip}")
    }

    /** MMKV 自增计数器:封装成 increase,免去手写读-改-写 */
    fun bumpCounter() {
        val n = MMKVStore.increase("sd_counter")
        log("MMKVStore.increase(sd_counter) → $n")
    }

    /** 备用方案:同样的数据用官方 SP 扩展写一遍(注意 apply 异步落盘) */
    fun saveToSp(context: Context) {
        val prefs = context.sp(SP_FILE)
        prefs.putStr("sp_nickname", nickname)
        prefs.putInt("sp_age", age)
        prefs.putObj("sp_profile", profile)
        // SP 也有属性委托写法:var x by prefs.intProp("key", 0),与 MMKV 同形
        log("SP 写入 → 文件 $SP_FILE(nickname/age/profile)")
    }

    /** 备用方案:从 SP 读回,与 MMKV 内容对照 */
    fun readFromSp(context: Context) {
        val prefs = context.sp(SP_FILE)
        val spNick = prefs.str("sp_nickname", "(空)")
        val spAge = prefs.int("sp_age", -1)
        val spProfile = prefs.getObject("sp_profile", DemoProfile::class.java)
        log("SP 读回 → 「$spNick」/ $spAge 岁 / ${spProfile?.name ?: "无档案"}")
    }

    fun clearSp(context: Context) {
        context.sp(SP_FILE).clearAll()
        log("SP 文件已清空")
    }

    /** 清空 MMKV 演示键(不动其他业务键) */
    fun clearMmkv() {
        MMKVStore.removeAll(
            arrayOf(
                "sd_nickname", "sd_age", "sd_vip", "sd_launch_count", "sd_tags",
                "sd_profile", "sd_counter",
            ),
        )
        refresh()
        log("MMKV 演示键已清除,回到默认值")
    }

    private fun refresh() {
        _snapshot.value = buildString {
            append("昵称:$nickname\n")
            append("年龄:$age\n")
            append("VIP:${if (vip) "是" else "否"}\n")
            append("累计启动:$launchCount 次\n")
            append("标签:${tags.joinToString("、")}\n")
            append("档案:${profile.name} Lv.${profile.level} 评分${profile.scores}")
        }
    }

    private fun log(msg: String) {
        lines.addFirst("· $msg")
        while (lines.size > MAX_LOG) lines.removeLast()
        _logText.value = lines.joinToString("\n")
    }

    private companion object {
        const val SP_FILE = "demo_sp"
        const val MAX_LOG = 8
    }
}
