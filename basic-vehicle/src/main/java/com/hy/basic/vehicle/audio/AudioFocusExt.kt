package com.hy.basic.vehicle.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import java.io.Closeable

/**
 * desc: 音频焦点请求/释放的轻量封装。
 *
 * 车机上导航播报、语音提示、警告音经常要临时抢焦点。这里把 API 26 前后的差异收在一处,
 * 业务只关心"拿到没有"和"什么时候释放"。
 *
 * 注意:
 * - 默认 AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,导航/提示音用这个比较合适;
 * - [usage]/[contentType] 按播放内容传,导航可用 USAGE_ASSISTANCE_NAVIGATION_GUIDANCE;
 * - 焦点变化回调在主线程,不要在回调里做耗时操作;
 * - handle 用完必须 close,否则会一直占着焦点。
 */
class AudioFocusHandle internal constructor(
    private val abandon: () -> Unit,
) : Closeable {

    private var closed = false

    /** 释放焦点,重复调用不会重复释放 */
    override fun close() {
        if (closed) return
        closed = true
        abandon()
    }
}

/**
 * 请求音频焦点。返回 null 表示系统没有授予,调用方不要继续播音。
 *
 * [onFocusChange] 会收到 AUDIOFOCUS_LOSS / LOSS_TRANSIENT / GAIN 等回调,
 * 业务按需暂停、压低音量或恢复播放。
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
