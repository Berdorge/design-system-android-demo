package com.vkui.analysis.test.mock

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import com.vkui.analysis.runtime.DesignAnalysisLayoutInflaterFactory
import com.vkui.analysis.test.R

class TestActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TestTheme)
        super.onCreate(savedInstanceState)
        setContentView(View(this))
    }

    override fun getSystemService(name: String): Any? {
        return if (name == LAYOUT_INFLATER_SERVICE) {
            val layoutInflater = LayoutInflater.from(baseContext).cloneInContext(this)
            layoutInflater.factory2 = DesignAnalysisLayoutInflaterFactory(layoutInflater)
            layoutInflater
        } else {
            super.getSystemService(name)
        }
    }
}
