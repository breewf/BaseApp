package com.hy.basic.vehicle.diagnostic

import android.util.Log
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

/**
 * desc: 轻量诊断日志。
 *
 * 除了写 Logcat,还在内存里保留一段环形缓冲。车机出问题时可以把最近几百条日志导出来,
 * 不用连 adb。真正落盘/上报交给外部设置 [sink]。
 *
 * 注意:
 * - 环形缓冲只保留最近 [capacity] 条,进程重启就没了;
 * - [sink] 在调用线程执行,别在里面做重 IO;
 * - 日志里不要打 VIN、手机号、定位坐标,需要的话先脱敏。
 */
object VehicleLog {

    /** 日志级别,顺序和 Logcat 一致 */
    enum class Level {
        /** 最详细,只在排查时开 */
        VERBOSE,

        /** 调试信息 */
        DEBUG,

        /** 正常流程关键节点 */
        INFO,

        /** 可恢复的异常 */
        WARN,

        /** 影响功能的问题 */
        ERROR,
    }

    /** 一条内存日志 */
    data class Record(
        /** 墙钟时间,导出时展示 */
        val wallTimeMs: Long,
        /** 单调时间,算间隔用 */
        val uptimeMs: Long,
        /** 日志级别 */
        val level: Level,
        /** 业务 tag */
        val tag: String,
        /** 日志内容 */
        val message: String,
        /** 关联异常 */
        val throwable: Throwable? = null,
    )

    /** 低于该级别的日志不记录 */
    @Volatile
    var minLevel: Level = Level.DEBUG

    /** 环形缓冲容量 */
    @Volatile
    var capacity: Int = 300

    /** 外部落盘/上报入口,默认不处理 */
    @Volatile
    var sink: ((Record) -> Unit)? = null

    private val ring = ArrayDeque<Record>()

    /** 详细日志 */
    fun v(tag: String, message: String, throwable: Throwable? = null) =
        log(Level.VERBOSE, tag, message, throwable)

    /** 调试日志 */
    fun d(tag: String, message: String, throwable: Throwable? = null) =
        log(Level.DEBUG, tag, message, throwable)

    /** 正常流程日志 */
    fun i(tag: String, message: String, throwable: Throwable? = null) =
        log(Level.INFO, tag, message, throwable)

    /** 警告日志 */
    fun w(tag: String, message: String, throwable: Throwable? = null) =
        log(Level.WARN, tag, message, throwable)

    /** 错误日志 */
    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log(Level.ERROR, tag, message, throwable)

    /** 通用入口,自定义级别时用 */
    fun log(level: Level, tag: String, message: String, throwable: Throwable? = null) {
        if (level.ordinal < minLevel.ordinal) return

        val record = Record(
            wallTimeMs = System.currentTimeMillis(),
            uptimeMs = System.nanoTime() / 1_000_000L,
            level = level,
            tag = tag,
            message = message,
            throwable = throwable,
        )

        synchronized(ring) {
            ring.addLast(record)
            val max = capacity.coerceAtLeast(0)
            while (ring.size > max) ring.removeFirst()
        }

        when (level) {
            Level.VERBOSE -> Log.v(tag, message, throwable)
            Level.DEBUG -> Log.d(tag, message, throwable)
            Level.INFO -> Log.i(tag, message, throwable)
            Level.WARN -> Log.w(tag, message, throwable)
            Level.ERROR -> Log.e(tag, message, throwable)
        }

        runCatching { sink?.invoke(record) }
    }

    /** 最近 [limit] 条,按时间正序返回 */
    fun recent(limit: Int = 100): List<Record> = synchronized(ring) {
        ring.toList().takeLast(limit.coerceAtLeast(0))
    }

    /** 清空环形缓冲 */
    fun clear() = synchronized(ring) { ring.clear() }

    /** 导出成文本,方便复制到 U 盘或日志文件 */
    fun exportText(limit: Int = 100): String {
        val formatter = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)
        return recent(limit).joinToString("\n") { record ->
            val time = formatter.format(Date(record.wallTimeMs))
            val error = record.throwable?.let { " | ${it.message}" } ?: ""
            "$time ${record.level} ${record.tag}: ${record.message}$error"
        }
    }
}
