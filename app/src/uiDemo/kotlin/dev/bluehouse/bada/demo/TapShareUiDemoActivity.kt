/*
 * Copyright 2026 Bada contributors.
 *
 * Licensed under the Apache License, Version 2.0.
 */
package dev.bluehouse.bada.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.bluehouse.bada.databinding.ActivityTapShareUiDemoBinding
import dev.bluehouse.bada.gestureexchange.GestureEdgeGlowController
import dev.bluehouse.bada.gestureexchange.GestureVisualSignal

/**
 * Debug-only visual harness for the production Tap-to-Share edge glow.
 *
 * The launcher deliberately contains two staged buttons and no transfer setup.
 * The first emits the real opening event, then the second emits the real
 * completion event. The uiDemo manifest also removes every production component
 * and non-visual permission so Android cannot start NFC, radios, discovery,
 * Name Card, or a content transfer through another entry point.
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

        binding.tapShareDemoStart.setOnClickListener { button ->
            button.visibility = View.GONE
            binding.tapShareDemoComplete.visibility = View.VISIBLE
            GestureVisualSignal.onProtocolEvent("reader_started")
        }
        binding.tapShareDemoComplete.setOnClickListener { button ->
            button.visibility = View.GONE
            GestureVisualSignal.onProtocolEvent("reader_completed")
        }
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
