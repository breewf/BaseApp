@file:JvmName("VehicleKeyEventExt")

package com.hy.basic.vehicle.input

import android.view.InputDevice
import android.view.KeyEvent
import android.view.KeyEvent.keyCodeToString
import android.view.MotionEvent

/**
 * 方控、旋钮、媒体键判定。
 *
 * 用法：
 * `event.isMediaKey()`、`event.isDirectionalKey()`、`motionEvent.isRotaryScroll()`。
 * 注意：方控 keyCode 各家不同；旋钮滚动一般走 ACTION_SCROLL。
 */

/** 媒体控制键 */
private val MEDIA_KEYS = setOf(
    KeyEvent.KEYCODE_MEDIA_PLAY,
    KeyEvent.KEYCODE_MEDIA_PAUSE,
    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
    KeyEvent.KEYCODE_MEDIA_STOP,
    KeyEvent.KEYCODE_MEDIA_NEXT,
    KeyEvent.KEYCODE_MEDIA_PREVIOUS,
    KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
    KeyEvent.KEYCODE_MEDIA_REWIND,
)

/** 音量键 */
private val VOLUME_KEYS = setOf(
    KeyEvent.KEYCODE_VOLUME_UP,
    KeyEvent.KEYCODE_VOLUME_DOWN,
    KeyEvent.KEYCODE_VOLUME_MUTE,
)

/** 方向键 */
private val DIRECTION_KEYS = setOf(
    KeyEvent.KEYCODE_DPAD_UP,
    KeyEvent.KEYCODE_DPAD_DOWN,
    KeyEvent.KEYCODE_DPAD_LEFT,
    KeyEvent.KEYCODE_DPAD_RIGHT,
)

/** 确认键 */
private val CONFIRM_KEYS = setOf(
    KeyEvent.KEYCODE_DPAD_CENTER,
    KeyEvent.KEYCODE_ENTER,
    KeyEvent.KEYCODE_NUMPAD_ENTER,
)

/** 媒体控制键(播放/暂停/切歌等) */
fun KeyEvent.isMediaKey(): Boolean = keyCode in MEDIA_KEYS

/** 音量键 */
fun KeyEvent.isVolumeKey(): Boolean = keyCode in VOLUME_KEYS

/** 方向键,用于焦点移动 */
fun KeyEvent.isDirectionalKey(): Boolean = keyCode in DIRECTION_KEYS

/** 确认键,方向盘 OK 键/旋钮按下一般映射到这里 */
fun KeyEvent.isConfirmKey(): Boolean = keyCode in CONFIRM_KEYS

/** 是否为长按产生的重复事件 */
fun KeyEvent.isRepeatEvent(): Boolean = repeatCount > 0

/** 可读的按键名,日志和调试页里用 */
fun KeyEvent.keyName(): String = keyCodeToString(keyCode) ?: "KEYCODE_$keyCode"

/** 是否为旋钮滚动事件 */
fun MotionEvent.isRotaryScroll(): Boolean =
    action == MotionEvent.ACTION_SCROLL && getAxisValue(MotionEvent.AXIS_SCROLL) != 0f

/** 旋钮滚动步进,非滚动事件返回 0 */
fun MotionEvent.rotaryDelta(): Float =
    if (isRotaryScroll()) getAxisValue(MotionEvent.AXIS_SCROLL) else 0f

/** 是否来自旋钮输入源(部分车型的旋钮会单独标 source) */
@Suppress("InlinedApi")
fun MotionEvent.isFromRotaryEncoder(): Boolean =
    (source and InputDevice.SOURCE_ROTARY_ENCODER) == InputDevice.SOURCE_ROTARY_ENCODER
