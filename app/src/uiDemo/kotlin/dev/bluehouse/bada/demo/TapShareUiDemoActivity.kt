/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.demo

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import dev.bluehouse.bada.databinding.ActivityTapShareUiDemoBinding
import dev.bluehouse.bada.gestureexchange.GestureEdgeGlowController
import dev.bluehouse.bada.gestureexchange.GestureVisualSignal

/**
 * Debug-only visual harness for Tap-to-Share glow, contact, and photo UI.
 *
 * The launcher is called “Tap to Share UI Demo.” Its menu opens a two-button
 * production glow harness, the reference contact-sharing sheets, or a Quick
 * Share photo walkthrough. Contact/photo values are synthetic and every state
 * transition is local to this Activity. The uiDemo manifest removes every
 * production component and non-visual permission, so no NFC, radio, discovery,
 * contact-provider, media-picker, Name Card, or transfer path is available.
 *
 * Status: source-grounded UI reconstruction. Build/manifest status is recorded
 * in the task journal; this iteration intentionally has no device click test.
 */
class TapShareUiDemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTapShareUiDemoBinding
    private lateinit var glow: GestureEdgeGlowController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GestureVisualSignal.clear()
        binding = ActivityTapShareUiDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        glow = GestureEdgeGlowController(this)
        showMenu()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.tapShareDemoHost.tag == TapShareDemoScreens.MENU_TAG) {
                        finish()
                    } else {
                        showMenu()
                    }
                }
            },
        )
    }

    private fun showMenu() {
        GestureVisualSignal.clear()
        show(
            TapShareDemoScreens.menu(
                context = this,
                onGlow = ::showGlowStart,
                onContact = ::showContactShare,
                onPhoto = ::showPhotoSelection,
            ),
            TapShareDemoScreens.MENU_TAG,
        )
    }

    private fun showGlowStart() {
        GestureVisualSignal.clear()
        show(
            TapShareDemoScreens.glow(
                context = this,
                showSecondHalf = false,
                onBack = ::showMenu,
                onStart = {
                    GestureVisualSignal.onProtocolEvent("reader_started")
                    showGlowSecondHalf()
                },
                onComplete = {},
            ),
        )
    }

    private fun showGlowSecondHalf() {
        show(
            TapShareDemoScreens.glow(
                context = this,
                showSecondHalf = true,
                onBack = ::showMenu,
                onStart = {},
                onComplete = {
                    GestureVisualSignal.onProtocolEvent("reader_completed")
                },
            ),
        )
    }

    private fun showContactShare() {
        GestureVisualSignal.clear()
        show(TapShareDemoScreens.contactShare(this, ::showMenu, ::showContactReceived))
    }

    private fun showContactReceived() {
        show(TapShareDemoScreens.contactReceived(this, ::showMenu))
    }

    private fun showPhotoSelection() {
        GestureVisualSignal.clear()
        show(TapShareDemoScreens.photoSelection(this, ::showMenu, ::showPhotoRequest))
    }

    private fun showPhotoRequest() {
        show(TapShareDemoScreens.photoRequest(this, ::showMenu, ::showPhotoProgress))
    }

    private fun showPhotoProgress() {
        show(TapShareDemoScreens.photoProgress(this, ::showMenu, ::showPhotoReceived))
    }

    private fun showPhotoReceived() {
        show(TapShareDemoScreens.photoReceived(this, ::showMenu))
    }

    private fun show(
        view: android.view.View,
        tag: String? = null,
    ) {
        binding.tapShareDemoHost.removeAllViews()
        binding.tapShareDemoHost.tag = tag
        binding.tapShareDemoHost.addView(view)
    }

    override fun onResume() {
        super.onResume()
        glow.attach()
    }

    override fun onPause() {
        glow.detach()
        super.onPause()
    }

    override fun onDestroy() {
        glow.close()
        super.onDestroy()
    }
}
