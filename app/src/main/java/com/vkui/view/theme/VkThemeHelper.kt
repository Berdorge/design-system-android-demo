package com.vkui.view.theme

import android.app.Activity
import android.content.res.Configuration
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.size
import androidx.core.view.children
import com.vkui.demo.R
import com.vkui.view.Themable
import com.vkui.view.resolveColor

class VkThemeHelper(private val activity: Activity) {
    val layoutInflater: LayoutInflater by lazy {
        val inflater = LayoutInflater.from(activity.baseContext).cloneInContext(activity)
        inflater.factory2 = VkLayoutInflaterFactory(inflater)
        inflater
    }

    fun onCreate() {
        setActivityTheme(activity.resources.configuration)
    }

    fun onConfigurationChanged(newConfig: Configuration) {
        setActivityTheme(newConfig)
        recolorViews(activity.window.decorView)
    }

    private fun setActivityTheme(configuration: Configuration) {
        val isDark = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val themeRes = if (isDark) {
            R.style.VkUiTheme_VkontakteAndroidDarkGenerated
        } else {
            R.style.VkUiTheme_VkontakteAndroidGenerated
        }
        activity.setTheme(themeRes)
    }

    private fun recolorViews(root: View) {
        if (root is ViewGroup) {
            for (child in root.children) {
                recolorViews(child)
            }
        }

        recolorView(root)
    }

    private fun recolorView(view: View) {
        val tag = view.getTag(R.id.theme_tag) as? SparseIntArray
        if (tag != null) {
            for (i in 0 until tag.size) {
                val themedAttribute = ThemedAttribute.entries.getOrNull(tag.keyAt(i)) ?: continue
                val attrId = tag.valueAt(i)
                applyAttribute(view, themedAttribute, attrId)
            }
        }
        if (view is Themable) {
            view.changeTheme()
        }
    }

    private fun applyAttribute(view: View, themedAttribute: ThemedAttribute, attrId: Int) {
        when (themedAttribute) {
            ThemedAttribute.TEXT_COLOR -> {
                val color = view.context.resolveColor(attrId)
                if (view is android.widget.TextView) {
                    view.setTextColor(color)
                }
            }

            ThemedAttribute.BACKGROUND_COLOR -> {
                val color = view.context.resolveColor(attrId)
                view.setBackgroundColor(color)
            }
        }
    }

    enum class ThemedAttribute {
        TEXT_COLOR,
        BACKGROUND_COLOR,
    }
}
