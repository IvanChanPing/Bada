/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.demo

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import dev.bluehouse.bada.R

/**
 * Builds the screens shown by “Tap to Share UI Demo.”
 *
 * Contact screens reproduce the visible structure and labels observed in the
 * Google announcement frames. Photo screens reproduce the Quick Share state
 * sequence and labels recovered from Pixel 10 GMS 26.30.32 resources. All data
 * and artwork are synthetic; button callbacks only replace the current View.
 *
 * Visual map: pale full-screen stage; rounded white 32 dp cards/sheets; green
 * primary pills; 64 dp initial avatars; a drawn landscape preview. The top-left
 * outlined “Back to demos” pill always returns to the local menu.
 */
@Suppress("MagicNumber", "TooManyFunctions")
internal object TapShareDemoScreens {
    const val MENU_TAG = "tap_share_demo_menu"

    private const val STAGE = 0xFFF7F8F4.toInt()
    private const val SHEET = 0xFFFCFCF7.toInt()
    private const val TEXT = 0xFF25251F.toInt()
    private const val MUTED = 0xFF65665F.toInt()
    private const val GREEN = 0xFF47642F.toInt()
    private const val GREEN_SOFT = 0xFFE1EBCF.toInt()
    private const val OUTLINE = 0xFFCACBC3.toInt()

    fun menu(
        context: Context,
        onGlow: () -> Unit,
        onContact: () -> Unit,
        onPhoto: () -> Unit,
    ): View =
        column(context, gravity = Gravity.CENTER).apply {
            setBackgroundColor(STAGE)
            setPadding(dp(28), dp(40), dp(28), dp(40))
            addView(title(context, R.string.demo_title, 32f))
            addView(label(context, R.string.demo_subtitle, 16f, MUTED), matchWrap(top = 8, bottom = 36))
            addView(primaryButton(context, R.string.demo_glow, onGlow), matchWrap(bottom = 12))
            addView(primaryButton(context, R.string.demo_contact, onContact), matchWrap(bottom = 12))
            addView(primaryButton(context, R.string.demo_photo, onPhoto), matchWrap())
        }

    fun glow(
        context: Context,
        showSecondHalf: Boolean,
        onBack: () -> Unit,
        onStart: () -> Unit,
        onComplete: () -> Unit,
    ): View =
        FrameLayout(context).apply {
            setBackgroundColor(Color.WHITE)
            addView(backButton(context, onBack), frameWrap(Gravity.TOP or Gravity.START, 18, 18))
            val action =
                primaryButton(
                    context,
                    if (showSecondHalf) R.string.tap_share_demo_complete else R.string.tap_share_demo_start,
                ) {}
            action.setOnClickListener { button ->
                button.visibility = View.GONE
                if (showSecondHalf) onComplete() else onStart()
            }
            addView(action, frameWrap(Gravity.CENTER, width = 240))
        }

    fun contactShare(
        context: Context,
        onBack: () -> Unit,
        onReceived: () -> Unit,
    ): View =
        contactStage(context, onBack) {
            addView(title(context, R.string.contact_name_camille, 20f, Gravity.CENTER_HORIZONTAL))
            addView(label(context, R.string.contact_share_status, 13f, MUTED, Gravity.CENTER_HORIZONTAL))
            addView(selectableRow(context, R.string.contact_picture, R.string.contact_personal, checked = true))
            addView(selectableRow(context, R.string.contact_phone_value, R.string.contact_mobile, checked = true))
            addView(selectableRow(context, R.string.contact_email_value, R.string.contact_email, checked = true))
            addView(
                horizontal(context).apply {
                    addView(outlineButton(context, R.string.contact_receive_only) {}, weightParams(1f, end = 8))
                    addView(primaryButton(context, R.string.contact_share, onReceived), weightParams(1f))
                },
                matchWrap(top = 12),
            )
            addView(outlineButton(context, R.string.contact_show_received, onReceived), matchWrap(top = 8))
        }

    fun contactReceived(
        context: Context,
        onBack: () -> Unit,
    ): View =
        contactStage(context, onBack) {
            addView(title(context, R.string.contact_name_danielle, 20f, Gravity.CENTER_HORIZONTAL))
            addView(label(context, R.string.contact_received_status, 13f, MUTED, Gravity.CENTER_HORIZONTAL))
            addView(
                infoRow(
                    context,
                    "DH",
                    context.getString(R.string.contact_picture),
                    context.getString(R.string.contact_personal),
                ),
            )
            addView(infoRow(context, "☎", "+1 555-453-2345", context.getString(R.string.contact_mobile)))
            addView(infoRow(context, "✉", "danielleholmes@gmail.com", context.getString(R.string.contact_email)))
            addView(primaryButton(context, R.string.contact_save) {}, matchWrap(top = 18))
        }

    fun photoSelection(
        context: Context,
        onBack: () -> Unit,
        onDevice: () -> Unit,
    ): View =
        photoStage(context, onBack) {
            addView(title(context, R.string.photo_quick_share, 28f))
            addView(ArtworkView(context, ArtworkView.Mode.LANDSCAPE), matchFixed(188, 138, top = 20))
            addView(label(context, R.string.photo_one_selected, 14f, MUTED), matchWrap(top = 10, bottom = 26))
            addView(title(context, R.string.photo_select_device, 20f))
            addView(label(context, R.string.photo_looking, 14f, MUTED), matchWrap(top = 6, bottom = 20))
            addView(deviceButton(context, onDevice), matchWrap())
        }

    fun photoRequest(
        context: Context,
        onBack: () -> Unit,
        onAccept: () -> Unit,
    ): View =
        photoStage(context, onBack) {
            addView(title(context, R.string.photo_incoming, 24f))
            addView(photoCard(context), matchWrap(top = 22, bottom = 24))
            addView(
                horizontal(context).apply {
                    addView(outlineButton(context, R.string.photo_decline, onBack), weightParams(1f, end = 10))
                    addView(primaryButton(context, R.string.photo_accept, onAccept), weightParams(1f))
                },
                matchWrap(),
            )
        }

    fun photoProgress(
        context: Context,
        onBack: () -> Unit,
        onComplete: () -> Unit,
    ): View =
        photoStage(context, onBack) {
            addView(title(context, R.string.photo_receiving, 26f))
            addView(photoCard(context), matchWrap(top = 22))
            addView(label(context, R.string.photo_progress, 34f, GREEN, Gravity.CENTER_HORIZONTAL), matchWrap(top = 24))
            addView(
                ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
                    progress = 68
                    progressTintList = ColorStateList.valueOf(GREEN)
                },
                matchFixed(ViewGroup.LayoutParams.MATCH_PARENT, 12, top = 12, bottom = 22),
            )
            addView(primaryButton(context, R.string.photo_show_complete, onComplete), matchWrap())
        }

    fun photoReceived(
        context: Context,
        onBack: () -> Unit,
    ): View =
        photoStage(context, onBack) {
            addView(title(context, R.string.photo_received, 28f, Gravity.CENTER_HORIZONTAL))
            addView(
                label(context, R.string.photo_received_from, 15f, MUTED, Gravity.CENTER_HORIZONTAL),
                matchWrap(top = 6),
            )
            addView(photoCard(context), matchWrap(top = 22, bottom = 24))
            addView(
                horizontal(context).apply {
                    addView(outlineButton(context, R.string.photo_done, onBack), weightParams(1f, end = 10))
                    addView(primaryButton(context, R.string.photo_open) {}, weightParams(1f))
                },
                matchWrap(),
            )
        }

    private fun contactStage(
        context: Context,
        onBack: () -> Unit,
        sheetContent: LinearLayout.() -> Unit,
    ): View =
        FrameLayout(context).apply {
            setBackgroundColor(STAGE)
            addView(ArtworkView(context, ArtworkView.Mode.PORTRAIT), FrameLayout.LayoutParams(-1, -1))
            addView(backButton(context, onBack), frameWrap(Gravity.TOP or Gravity.START, 18, 18))
            addView(
                column(context).apply {
                    background = rounded(SHEET, 30f, topOnly = true)
                    elevation = dp(10).toFloat()
                    setPadding(dp(22), dp(20), dp(22), dp(18))
                    sheetContent()
                },
                FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM),
            )
        }

    private fun photoStage(
        context: Context,
        onBack: () -> Unit,
        content: LinearLayout.() -> Unit,
    ): View =
        FrameLayout(context).apply {
            setBackgroundColor(STAGE)
            addView(
                column(context).apply {
                    setPadding(dp(26), dp(96), dp(26), dp(30))
                    content()
                },
                FrameLayout.LayoutParams(-1, -1),
            )
            addView(backButton(context, onBack), frameWrap(Gravity.TOP or Gravity.START, 18, 18))
        }

    private fun photoCard(context: Context): View =
        horizontal(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(Color.WHITE, 32f)
            elevation = dp(3).toFloat()
            setPadding(dp(12), dp(12), dp(16), dp(12))
            addView(ArtworkView(context, ArtworkView.Mode.LANDSCAPE), LinearLayout.LayoutParams(dp(92), dp(72)))
            addView(
                column(context).apply {
                    setPadding(dp(14), 0, 0, 0)
                    addView(title(context, R.string.photo_filename, 15f))
                    addView(label(context, R.string.photo_filesize, 13f, MUTED), matchWrap(top = 3))
                },
                weightParams(1f),
            )
        }

    private fun deviceButton(
        context: Context,
        onClick: () -> Unit,
    ): View =
        horizontal(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            background = rounded(GREEN_SOFT, 32f)
            setPadding(dp(12), dp(10), dp(18), dp(10))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
            addView(avatar(context, "DH"), LinearLayout.LayoutParams(dp(64), dp(64)))
            addView(
                column(context).apply {
                    setPadding(dp(14), 0, 0, 0)
                    addView(title(context, R.string.photo_device, 17f))
                    addView(label(context, R.string.photo_one_selected, 13f, MUTED), matchWrap(top = 2))
                },
                weightParams(1f),
            )
        }

    private fun selectableRow(
        context: Context,
        primary: Int,
        secondary: Int,
        checked: Boolean,
    ): View =
        CheckBox(context).apply {
            isChecked = checked
            buttonTintList = ColorStateList.valueOf(GREEN)
            text = "${context.getString(primary)}\n${context.getString(secondary)}"
            textSize = 14f
            setTextColor(TEXT)
            gravity = Gravity.CENTER_VERTICAL
            minHeight = dp(58)
        }

    private fun infoRow(
        context: Context,
        icon: String,
        primary: String,
        secondary: String,
    ): View =
        horizontal(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(7), 0, dp(7))
            addView(avatar(context, icon, 36), LinearLayout.LayoutParams(dp(36), dp(36)))
            addView(
                TextView(context).apply {
                    text = "$primary\n$secondary"
                    textSize = 14f
                    setTextColor(TEXT)
                    setPadding(dp(12), 0, 0, 0)
                },
                weightParams(1f),
            )
        }

    private fun avatar(
        context: Context,
        initials: String,
        size: Int = 64,
    ): TextView =
        TextView(context).apply {
            text = initials
            gravity = Gravity.CENTER
            textSize = if (size >= 60) 20f else 13f
            setTextColor(Color.WHITE)
            background = rounded(0xFF765B42.toInt(), size / 2f)
        }

    private fun backButton(
        context: Context,
        onClick: () -> Unit,
    ): MaterialButton =
        outlineButton(context, R.string.demo_back, onClick).apply {
            minWidth = 0
            minHeight = dp(44)
            textSize = 13f
        }

    private fun primaryButton(
        context: Context,
        text: Int,
        onClick: () -> Unit,
    ): MaterialButton = button(context, text, GREEN, Color.WHITE, onClick)

    private fun outlineButton(
        context: Context,
        text: Int,
        onClick: () -> Unit,
    ): MaterialButton =
        button(context, text, Color.TRANSPARENT, GREEN, onClick).apply {
            strokeColor = ColorStateList.valueOf(OUTLINE)
            strokeWidth = dp(1)
        }

    private fun button(
        context: Context,
        text: Int,
        fill: Int,
        ink: Int,
        onClick: () -> Unit,
    ): MaterialButton =
        MaterialButton(context).apply {
            setText(text)
            textSize = 15f
            isAllCaps = false
            minHeight = dp(52)
            cornerRadius = dp(26)
            insetTop = 0
            insetBottom = 0
            backgroundTintList = ColorStateList.valueOf(fill)
            setTextColor(ink)
            setOnClickListener { onClick() }
        }

    private fun title(
        context: Context,
        text: Int,
        size: Float,
        gravity: Int = Gravity.START,
    ): TextView =
        label(context, text, size, TEXT, gravity).apply { setTypeface(typeface, android.graphics.Typeface.BOLD) }

    private fun label(
        context: Context,
        text: Int,
        size: Float,
        color: Int,
        gravity: Int = Gravity.START,
    ): TextView =
        TextView(context).apply {
            setText(text)
            textSize = size
            setTextColor(color)
            this.gravity = gravity
        }

    private fun column(
        context: Context,
        gravity: Int = Gravity.TOP,
    ): LinearLayout =
        LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            this.gravity = gravity
            setBackgroundColor(Color.TRANSPARENT)
        }

    private fun horizontal(context: Context): LinearLayout =
        LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }

    private fun rounded(
        color: Int,
        radius: Float,
        topOnly: Boolean = false,
    ): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            if (topOnly) {
                cornerRadii = floatArrayOf(dp(radius), dp(radius), dp(radius), dp(radius), 0f, 0f, 0f, 0f)
            } else {
                cornerRadius = dp(radius)
            }
        }

    private fun matchWrap(
        top: Int = 0,
        bottom: Int = 0,
    ): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(-1, -2).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        }

    private fun matchFixed(
        width: Int,
        height: Int,
        top: Int = 0,
        bottom: Int = 0,
    ): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(if (width < 0) width else dp(width), dp(height)).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        }

    private fun weightParams(
        weight: Float,
        end: Int = 0,
    ): LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, -2, weight).apply { marginEnd = dp(end) }

    private fun frameWrap(
        gravity: Int,
        start: Int = 0,
        top: Int = 0,
        width: Int = -2,
    ): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(if (width < 0) width else dp(width), -2, gravity).apply {
            marginStart = dp(start)
            topMargin = dp(top)
        }

    private fun dp(value: Int): Int =
        (
            value *
                android.content.res.Resources
                    .getSystem()
                    .displayMetrics.density
        ).toInt()

    private fun dp(value: Float): Float =
        value *
            android.content.res.Resources
                .getSystem()
                .displayMetrics.density
}

/** Synthetic non-photographic art keeps this demo permission-free and self-contained. */
private class ArtworkView(
    context: Context,
    private val mode: Mode,
) : View(context) {
    enum class Mode { PORTRAIT, LANDSCAPE }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        paint.color = if (mode == Mode.PORTRAIT) 0xFF294B3A.toInt() else 0xFFB8D9EA.toInt()
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.color = if (mode == Mode.PORTRAIT) 0xFF6E9369.toInt() else 0xFFF7D46A.toInt()
        canvas.drawCircle(w * 0.72f, h * 0.22f, h * 0.12f, paint)
        path.reset()
        path.moveTo(0f, h * 0.82f)
        path.lineTo(w * 0.32f, h * 0.42f)
        path.lineTo(w * 0.55f, h * 0.74f)
        path.lineTo(w * 0.78f, h * 0.36f)
        path.lineTo(w, h * 0.72f)
        path.lineTo(w, h)
        path.lineTo(0f, h)
        path.close()
        paint.color = if (mode == Mode.PORTRAIT) 0xFF163B2B.toInt() else 0xFF527D4C.toInt()
        canvas.drawPath(path, paint)
        if (mode == Mode.PORTRAIT) {
            paint.color = 0xFF9E6547.toInt()
            canvas.drawCircle(w * 0.5f, h * 0.28f, w * 0.15f, paint)
            paint.color = 0xFF6E3037.toInt()
            canvas.drawOval(RectF(w * 0.29f, h * 0.34f, w * 0.71f, h * 0.9f), paint)
        }
    }
}
