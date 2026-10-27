package com.hy.basic.skin

import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.cardview.widget.CardView

/** 背景,对应 setBackground */
fun View.setBackgroundSkin(@DrawableRes resId: Int) =
    SkinManager.setSkinRes(this, SkinPart.BACKGROUND, resId)

/** 背景色,对应 setBackgroundColor */
fun View.setBackgroundColorSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.BACKGROUND, colorRes)

/** 前景,对应 setForeground */
fun View.setForegroundSkin(@DrawableRes resId: Int) =
    SkinManager.setSkinRes(this, SkinPart.FOREGROUND, resId)

/** 背景着色,对应 setBackgroundTintList */
fun View.setBackgroundTintListSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.BACKGROUND_TINT, colorRes)

/** 文字色,对应 setTextColor */
fun TextView.setTextColorSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.TEXT_COLOR, colorRes)

/** 提示文字色,对应 setHintTextColor */
fun TextView.setHintTextColorSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.TEXT_HINT, colorRes)

/** 链接文字色,对应 setLinkTextColor */
fun TextView.setLinkTextColorSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.TEXT_COLOR_LINK, colorRes)

/** 左复合图,对应 setCompoundDrawablesRelativeWithIntrinsicBounds */
fun TextView.setCompoundDrawableStartSkin(@DrawableRes resId: Int) =
    SkinManager.setSkinRes(this, SkinPart.COMPOUND_DRAWABLE_START, resId)

/** 上复合图 */
fun TextView.setCompoundDrawableTopSkin(@DrawableRes resId: Int) =
    SkinManager.setSkinRes(this, SkinPart.COMPOUND_DRAWABLE_TOP, resId)

/** 右复合图 */
fun TextView.setCompoundDrawableEndSkin(@DrawableRes resId: Int) =
    SkinManager.setSkinRes(this, SkinPart.COMPOUND_DRAWABLE_END, resId)

/** 下复合图 */
fun TextView.setCompoundDrawableBottomSkin(@DrawableRes resId: Int) =
    SkinManager.setSkinRes(this, SkinPart.COMPOUND_DRAWABLE_BOTTOM, resId)

/** 图片资源,对应 setImageResource */
fun ImageView.setImageResourceSkin(@DrawableRes resId: Int) =
    SkinManager.setSkinRes(this, SkinPart.SRC, resId)

/** 图片着色,对应 setImageTintList */
fun ImageView.setImageTintListSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.IMAGE_TINT, colorRes)

/** 进度色,对应 setProgressTintList */
fun ProgressBar.setProgressTintListSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.PROGRESS_TINT, colorRes)

/** 进度条底色,对应 setProgressBackgroundTintList */
fun ProgressBar.setProgressBackgroundTintListSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.PROGRESS_BACKGROUND_TINT, colorRes)

/** 次要进度色,对应 setSecondaryProgressTintList */
fun ProgressBar.setSecondaryProgressTintListSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.SECONDARY_PROGRESS_TINT, colorRes)

/** 不确定进度色,对应 setIndeterminateTintList */
fun ProgressBar.setIndeterminateTintListSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.INDETERMINATE_TINT, colorRes)

/** 滑块色,对应 setThumbTintList */
fun SeekBar.setThumbTintListSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.THUMB_TINT, colorRes)

/** 刻度色,对应 setTickMarkTintList */
fun SeekBar.setTickMarkTintListSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.TICK_MARK_TINT, colorRes)

/** 按钮着色,对应 setButtonTintList */
fun CompoundButton.setButtonTintListSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.BUTTON_TINT, colorRes)

/** 分割线,对应 setDividerDrawable */
fun LinearLayout.setDividerDrawableSkin(@DrawableRes resId: Int) =
    SkinManager.setSkinRes(this, SkinPart.DIVIDER, resId)

/** CardView 底色,对应 setCardBackgroundColor */
fun CardView.setCardBackgroundColorSkin(@ColorRes colorRes: Int) =
    SkinManager.setSkinRes(this, SkinPart.CARD_BG, colorRes)
