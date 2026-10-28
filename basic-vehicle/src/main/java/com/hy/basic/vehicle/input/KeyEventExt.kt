@file:JvmName("VehicleKeyEventExt")

package com.hy.basic.vehicle.input

import android.view.InputDevice
import android.view.KeyEvent
import android.view.KeyEvent.keyCodeToString
import android.view.MotionEvent

/**
 * desc: 方控、旋钮、媒体键的判定扩展。
 *
 * 车机输入比手机杂:方向盘按键走 KeyEvent,旋钮大多走 ACTION_SCROLL,
 * 还有 Touchpad/触摸板混着来。这里只做"这是哪类键"的判断和可读名称,
 * 具体业务映射(接听/挂断/切歌)由各车型自己配。
 *
 * 注意:
 * - 方控按键的 keyCode 各家可能不同,别只认标准媒体键,必要时按 keyCode 配映射表;
 * - 旋钮滚动在不同车型可能是 AXIS_SCROLL 或 AXIS_WHEEL,先看 InputDevice 的 source;
 * - 长按重复事件靠 repeatCount 判断,ACTION_DOWN 只处理一次。
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
