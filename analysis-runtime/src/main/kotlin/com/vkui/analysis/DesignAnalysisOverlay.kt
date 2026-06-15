package com.vkui.analysis

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout
import com.vkui.analysis.runtime.DesignAnalysisHelper
import com.vkui.analysis.runtime.databinding.AnalysisOverlayBinding
import kotlinx.coroutines.Runnable

internal class DesignAnalysisOverlay(context: Context) : ConstraintLayout(context) {
    private val runnable = object : Runnable {
        override fun run() {
            val taintedMethodCount = DesignAnalysisHelper.taintedMethodCount.getAndSet(0)
            if (taintedMethodCount > 0) {
                val instrumentedMethodCount = DesignAnalysisHelper.totalMethodCount.getAndSet(0)
                println("Instrumented $instrumentedMethodCount, tainted $taintedMethodCount, ratio ${taintedMethodCount.toDouble() / instrumentedMethodCount}")
            }
            postDelayed(this, 1000)
        }
    }

    init {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        fitsSystemWindows = true
        val binding = AnalysisOverlayBinding.inflate(LayoutInflater.from(context), this)

        binding.overlayButton.setOnClickListener {
            DesignAnalysisHelper.setAnyViewTouchListener(this) { view ->
                DesignAnalysisHelper.clearAnyViewTouchListener(this)
                val viewProperties = DesignAnalysisHelper.getPropertiesOf(view)
                val lpProperties = DesignAnalysisHelper.getPropertiesOf(view.layoutParams)
                println("Selected view properties: $viewProperties")
                println("Selected view lp properties: $lpProperties")
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postDelayed(runnable, 1000)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(runnable)
        super.onDetachedFromWindow()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchListener = DesignAnalysisHelper.anyViewTouchListener
        DesignAnalysisHelper.anyViewTouchListener = null
        val result = super.onTouchEvent(event)
        DesignAnalysisHelper.anyViewTouchListener = touchListener
        return result
    }
}
