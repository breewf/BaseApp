@file:JvmName("RegexExt")

package com.hy.basic.utils

import com.blankj.utilcode.util.RegexUtils

/**
 * desc: 正则校验扩展 —— 基于 blankj [RegexUtils] 的常见安卓校验封装
 *
 * 以 String / CharSequence 扩展暴露(调用点 `input.isMobile()` / `text.isEmail()` 等)。
 * 校验类用 [CharSequence] 接收者(String / StringBuilder 皆可),
 * 需要自定义正则或做替换的用 [String] 接收者。
 *
 * 分组:
 * - 手机号 / 电话:[isMobile] / [isMobileExact] / [isTel];
 * - 证件:[isIDCard](18 位精确或 15 位) / [isIDCard18Exact] / [isIDCard15];
 * - 网络 / 地址:[isEmail] / [isURL] / [isIPAddress];
 * - 文本属性:[isChinese] / [isUsername] / [isDate] / [isDigits] / [isNumeric] /
 *   [isAllUpperCase] / [isAllLowerCase];
 * - 自定义正则 / 处理:[matchByRegex] / [replaceRegexFirst] / [replaceRegexAll] /
 *   [emailUsername] / [emailProvider]。
 *
 * 说明:手机号 [isMobile] 为宽松校验(1 开头共 11 位),[isMobileExact] 为严格运营商号段校验。
 */

// ==== 手机号 / 电话 ====

/** 手机号(宽松:1 开头共 11 位数字) */
fun CharSequence.isMobile(): Boolean = RegexUtils.isMobileSimple(this)

/** 手机号(严格:匹配真实运营商号段) */
fun CharSequence.isMobileExact(): Boolean = RegexUtils.isMobileExact(this)

/** 固定电话(区号 + 号码) */
fun CharSequence.isTel(): Boolean = RegexUtils.isTel(this)

// ==== 证件 ====

/** 身份证号(18 位精确校验通过,或为 15 位) */
fun CharSequence.isIDCard(): Boolean = RegexUtils.isIDCard18Exact(this) || RegexUtils.isIDCard15(this)

/** 18 位身份证(含校验位严格校验) */
fun CharSequence.isIDCard18Exact(): Boolean = RegexUtils.isIDCard18Exact(this)

/** 15 位身份证 */
fun CharSequence.isIDCard15(): Boolean = RegexUtils.isIDCard15(this)

// ==== 网络 / 地址 ====

/** 邮箱 */
fun CharSequence.isEmail(): Boolean = RegexUtils.isEmail(this)

/** URL */
fun CharSequence.isURL(): Boolean = RegexUtils.isURL(this)

/** IPv4 / IPv6 地址 */
fun CharSequence.isIPAddress(): Boolean = RegexUtils.isIP(this)

// ==== 文本属性 ====

/** 全为中文字符 */
fun CharSequence.isChinese(): Boolean = RegexUtils.isZh(this)

/** 合法用户名(6~16 位,大小写字母与数字组合,必须有字母) */
fun CharSequence.isUsername(): Boolean = RegexUtils.isUsername(this)

/** 日期 yyyy-MM-dd HH:mm:ss */
fun CharSequence.isDate(): Boolean = RegexUtils.isDate(this)

/** 纯数字(至少一位,仅 0-9) */
fun String.isDigits(): Boolean = RegexUtils.isMatch(REGEX_DIGITS, this)

/** 数值(允许正负号与小数点,如 -1 / 3.14) */
fun String.isNumeric(): Boolean = RegexUtils.isMatch(REGEX_NUMERIC, this)

/** 含字母且所有字母均为大写 */
fun String.isAllUpperCase(): Boolean = lettersOf(this).let { it.isNotEmpty() && it.all { c -> c.isUpperCase() } }

/** 含字母且所有字母均为小写 */
fun String.isAllLowerCase(): Boolean = lettersOf(this).let { it.isNotEmpty() && it.all { c -> c.isLowerCase() } }

// ==== 自定义正则 / 处理 ====

/** 是否匹配自定义正则 [pattern](空串视为不匹配)。注意 blankj isMatch 参数序为 (regex, input) */
fun String.matchByRegex(pattern: String): Boolean = RegexUtils.isMatch(pattern, this)

/** 用正则 [pattern] 替换首个匹配为 [replacement] */
fun String.replaceRegexFirst(pattern: String, replacement: String): String =
    RegexUtils.getReplaceFirst(this, pattern, replacement) ?: this

/** 用正则 [pattern] 替换所有匹配为 [replacement] */
fun String.replaceRegexAll(pattern: String, replacement: String): String =
    RegexUtils.getReplaceAll(this, pattern, replacement) ?: this

/** 提取邮箱 @ 之前的用户名部分(非邮箱返回空串) */
fun String.emailUsername(): String = RegexUtils.extractEmailUsername(this) ?: ""

/** 提取邮箱 @ 之后的服务商域名(非邮箱返回空串) */
fun String.emailProvider(): String = RegexUtils.extractEmailProvider(this) ?: ""

private fun lettersOf(input: String) = input.filter { it.isLetter() }

private const val REGEX_DIGITS = "^\\d+$"
private const val REGEX_NUMERIC = "^[-+]?\\d+(\\.\\d+)?$"
