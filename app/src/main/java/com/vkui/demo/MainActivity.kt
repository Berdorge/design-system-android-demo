package com.vkui.demo

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.vkui.demo.components.ComponentScreensListFragment
import com.vkui.view.theme.VkLayoutInflaterFactory
import com.vkui.view.theme.VkThemeHelper

class MainActivity : FragmentActivity() {
    private val themeHelper = VkThemeHelper(this)
    private lateinit var layoutInflater: LayoutInflater

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        themeHelper.onCreate()
        setContentView(R.layout.activity_main)
        supportFragmentManager.commit {
            add(R.id.root_fragment_container, ComponentScreensListFragment())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        themeHelper.onConfigurationChanged(newConfig)
    }

    override fun getSystemService(name: String): Any? {
        return if (name == LAYOUT_INFLATER_SERVICE) {
            if (!::layoutInflater.isInitialized) {
                layoutInflater = LayoutInflater.from(baseContext).cloneInContext(this)
                layoutInflater.factory = VkLayoutInflaterFactory(layoutInflater)
            }
            layoutInflater
        } else {
            super.getSystemService(name)
        }
    }

    private fun setTheme(configuration: Configuration = resources.configuration) {
    }
}
