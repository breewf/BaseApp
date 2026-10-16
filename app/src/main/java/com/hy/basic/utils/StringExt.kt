@file:JvmName("StringExt")

package com.hy.basic.utils

import com.blankj.utilcode.util.StringUtils

/**
 * desc: 字符串常用扩展 —— 基于 blankj [StringUtils] 的判空 / 去空 / 截取 / 转换封装
 *
 * 以 String(或可空 String?)扩展暴露,判空类用可空接收者(null 视作空),
 * 转换/截取类用非空接收者,省去各处重复的 null 判断与样板封装。
 *
 * 分组:
 * - 判空:[isStrEmpty] / [isStrNotEmpty] / [isStrTrimEmpty] / [isAllSpace] / [strLength];
 * - 去空 / 默认值:[strTrim] / [strTrimAll] / [nullToEmpty];
 * - 截取:[strSub];
 * - 比较:[equalsStr] / [equalsIgnoreStr];
 * - 转换 / 格式化:[capitalizeFirstStr] / [decapitalizeFirstStr] / [reverseStr] /
 *   [toHalfWidth] / [toFullWidth] / [strFormat]。
 */

// ==== 判空 ====

/** 是否为空(null 或长度 0) */
fun String?.isStrEmpty(): Boolean = StringUtils.isEmpty(this)

/** 是否非空(不为 null 且有内容) */
fun String?.isStrNotEmpty(): Boolean = !StringUtils.isEmpty(this)

/** 去除首尾空白后是否为空 */
fun String?.isStrTrimEmpty(): Boolean = StringUtils.isTrimEmpty(this)

/** 是否全是空白字符(或为空) */
fun String?.isAllSpace(): Boolean = StringUtils.isSpace(this)

/** 长度(null 返回 0) */
fun String?.strLength(): Int = StringUtils.length(this)

// ==== 去空 / 默认值 ====

/** 去除首尾空白 */
fun String.strTrim(): String = this.trim()

/** 去除字符串中所有空白字符(含中间的空格/制表/换行) */
fun String.strTrimAll(): String = StringUtils.isSpace(this).let { if (it) "" else this.replace(WHITESPACE, "") }

/** null 转空串,非 null 原样返回 */
fun String?.nullToEmpty(): String = StringUtils.null2Length0(this)

// ==== 截取 ====

/**
 * 安全截取:自动把起止位置约束到 [0, length] 且保证 start <= end,越界不抛异常。
 * @param start 起始(含);@param end 结束(不含),默认到末尾。
 */
fun String.strSub(start: Int, end: Int = length): String {
    val s = start.coerceIn(0, length)
    val e = end.coerceIn(s, length)
    return substring(s, e)
}

// ==== 比较 ====

/** 内容相等(null 视作不等),避免 `==` 之外的空指针顾虑 */
fun String?.equalsStr(other: String?): Boolean = StringUtils.equals(this, other)

/** 忽略大小写相等 */
fun String?.equalsIgnoreStr(other: String?): Boolean = StringUtils.equalsIgnoreCase(this, other)

// ==== 转换 / 格式化 ====

/** 首字母大写 */
fun String.capitalizeFirstStr(): String = StringUtils.upperFirstLetter(this)

/** 首字母小写 */
fun String.decapitalizeFirstStr(): String = StringUtils.lowerFirstLetter(this)

/** 反转字符串 */
fun String.reverseStr(): String = StringUtils.reverse(this)

/** 全角转半角 */
fun String.toHalfWidth(): String = StringUtils.toDBC(this)

/** 半角转全角 */
fun String.toFullWidth(): String = StringUtils.toSBC(this)

/** 以当前串为模板做格式化(如 `"%s-%d".strFormat("a", 1)`) */
fun String.strFormat(vararg args: Any?): String = StringUtils.format(this, *args)

private val WHITESPACE = "\\s".toRegex()
