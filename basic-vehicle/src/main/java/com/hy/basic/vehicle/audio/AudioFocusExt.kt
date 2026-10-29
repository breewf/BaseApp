package com.hy.basic.vehicle.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import java.io.Closeable

/**
 * 音频焦点请求/释放，兼容 API 26 前后差异。
 *
 * 用法：
 * `context.requestAudioFocus(usage = USAGE_ASSISTANCE_NAVIGATION_GUIDANCE) { ... }?.close()`。
 * 注意：返回 null 表示未授予；handle 要在播放结束或页面销毁时 close。
 */
class AudioFocusHandle internal constructor(
    /** 释放动作 */
    private val abandon: () -> Unit,
) : Closeable {

    /** 是否已释放 */
    private var closed = false

    /** 释放焦点,重复调用不会重复释放 */
    override fun close() {
        if (closed) return
        closed = true
        abandon()
    }
}

/**
 * 请求音频焦点。返回 null 表示系统未授予焦点。
 *
 * @param gainType 焦点类型，默认 transient may duck
 * @param streamType API 26 以下使用的流类型
 * @param usage 播放用途，导航可传 USAGE_ASSISTANCE_NAVIGATION_GUIDANCE
 * @param contentType 内容类型
 * @param onFocusChange 焦点变化回调，在主线程触发
 */
@Suppress("DEPRECATION")
fun Context.requestAudioFocus(
    gainType: Int = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
    streamType: Int = AudioManager.STREAM_MUSIC,
    usage: Int = AudioAttributes.USAGE_MEDIA,
    contentType: Int = AudioAttributes.CONTENT_TYPE_MUSIC,
    onFocusChange: (Int) -> Unit = {},
): AudioFocusHandle? {
    val manager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return null
    val listener = AudioManager.OnAudioFocusChangeListener { onFocusChange(it) }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val request = AudioFocusRequest.Builder(gainType)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(usage)
                    .setContentType(contentType)
                    .build(),
            )
            .setOnAudioFocusChangeListener(listener)
            .build()

        val granted = manager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return if (granted) {
            AudioFocusHandle { manager.abandonAudioFocusRequest(request) }
        } else {
            null
        }
    }

    val granted = manager.requestAudioFocus(listener, streamType, gainType) ==
        AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    return if (granted) {
        AudioFocusHandle { manager.abandonAudioFocus(listener) }
    } else {
        null
    }
}
