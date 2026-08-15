/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.demo

import android.animation.ValueAnimator
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
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.view.doOnPreDraw
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import dev.bluehouse.bada.R
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * Builds the screens shown by “Tap to Share UI Demo.”
 *
 * Contact screens port the Pixel branch's Material modal-sheet structure and
 * hide-before-callback lifecycle recovered from GMS 26.30.32. Photo screens
 * port Quick Share's exact scanner/receive Lottie payloads, source-owned sizes,
 * list reveal structure, and 500 ms progress timing. All data and artwork are
 * synthetic; button callbacks only replace the current View.
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
    private const val QUICK_SHARE_LIST_TWEEN_MS = 400L
    private const val QUICK_SHARE_PROGRESS_MS = 500L
    private val listRevealMotion = PathInterpolator(0.2f, 0f, 0f, 1f)
    private val progressMotion = PathInterpolator(0.65f, 0f, 0.35f, 1f)

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
    ): View = contactStage(context, onBack, received = false, onAdvance = onReceived)

    fun contactReceived(
        context: Context,
        onBack: () -> Unit,
    ): View = contactStage(context, onBack, received = true, onAdvance = onBack)

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
            addView(label(context, R.string.photo_looking, 14f, MUTED), matchWrap(top = 6, bottom = 4))
            addView(
                quickShareAnimation(
                    context,
                    R.raw.swatchie__sharing_send_scanning_unified,
                    loop = true,
                    R.string.photo_scanning_animation,
                ),
                matchFixed(60, 60, bottom = 12),
            )
            addView(deviceButton(context, onDevice).also(::animateTargetListIn), matchWrap())
        }

    fun photoRequest(
        context: Context,
        onBack: () -> Unit,
        onAccept: () -> Unit,
    ): View =
        photoStage(context, onBack) {
            addView(
                quickShareAnimation(
                    context,
                    R.raw.swatchie__sharing_ready_to_receive_everyone,
                    loop = true,
                    R.string.photo_receive_ready_animation,
                ),
                matchFixed(24, 24, bottom = 10),
            )
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
                    progress = 0
                    progressTintList = ColorStateList.valueOf(GREEN)
                    doOnPreDraw {
                        ValueAnimator.ofInt(0, 68).apply {
                            duration = QUICK_SHARE_PROGRESS_MS
                            interpolator = progressMotion
                            addUpdateListener { progress = it.animatedValue as Int }
                            start()
                        }
                    }
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
        received: Boolean,
        onAdvance: () -> Unit,
    ): View =
        FrameLayout(context).apply {
            setBackgroundColor(STAGE)
            addView(ArtworkView(context, ArtworkView.Mode.PORTRAIT), FrameLayout.LayoutParams(-1, -1))
            addView(
                ComposeView(context).apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                    setContent { exactPixelContactSheet(received, onBack, onAdvance) }
                },
            )
            addView(backButton(context, onBack), frameWrap(Gravity.TOP or Gravity.START, 18, 18))
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

    private fun quickShareAnimation(
        context: Context,
        animation: Int,
        loop: Boolean,
        description: Int,
    ): LottieAnimationView =
        LottieAnimationView(context).apply {
            setAnimation(animation)
            repeatMode = ValueAnimator.RESTART
            repeatCount = if (loop) ValueAnimator.INFINITE else 0
            contentDescription = context.getString(description)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            playAnimation()
        }

    private fun animateTargetListIn(targets: View) {
        targets.doOnPreDraw { view ->
            view.pivotY = 0f
            view.scaleY = 0f
            view
                .animate()
                .scaleY(1f)
                .setDuration(QUICK_SHARE_LIST_TWEEN_MS)
                .setInterpolator(listRevealMotion)
                .start()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun exactPixelContactSheet(
        received: Boolean,
        onDismiss: () -> Unit,
        onAdvance: () -> Unit,
    ) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()
        val hideThenAdvance: () -> Unit = {
            scope.launch {
                sheetState.hide()
                onAdvance()
            }
            Unit
        }
        MaterialTheme(
            colorScheme =
                lightColorScheme(
                    primary = ComposeColor(GREEN),
                    surface = ComposeColor(SHEET),
                ),
        ) {
            ModalBottomSheet(
                onDismissRequest = onDismiss,
                sheetState = sheetState,
                containerColor = ComposeColor(SHEET),
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, bottom = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (received) "Danielle Holmes" else "Camille Laurent",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(
                        text = if (received) "Contact received" else "Share your contact info",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ComposeColor(MUTED),
                    )
                    if (received) {
                        Text("Picture · Personal")
                        Text("+1 555-453-2345 · Mobile")
                        Text("danielleholmes@gmail.com · Email")
                        Button(onClick = hideThenAdvance, modifier = Modifier.fillMaxWidth()) { Text("Save contact") }
                    } else {
                        contactChoice("Picture", "Personal")
                        contactChoice("+1 555-284-5555", "Mobile")
                        contactChoice("camille@example.com", "Email")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(onClick = hideThenAdvance, modifier = Modifier.weight(1f)) {
                                Text("Receive only")
                            }
                            Button(onClick = hideThenAdvance, modifier = Modifier.weight(1f)) { Text("Share") }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun contactChoice(
        primary: String,
        secondary: String,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Checkbox(checked = true, onCheckedChange = {})
            Column {
                Text(primary)
                Text(secondary, color = ComposeColor(MUTED))
            }
        }
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
