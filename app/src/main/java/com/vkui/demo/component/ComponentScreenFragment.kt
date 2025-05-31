package com.vkui.demo.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.vkui.theme.VkTheme

class ComponentScreenFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): ComposeView {
        val screenKey = arguments?.let { arguments ->
            BundleCompat.getSerializable(arguments, SCREEN_KEY_KEY, ComponentScreenKey::class.java)
        }
        requireNotNull(screenKey) {
            "You should create the fragment using newInstance()"
        }
        return content {
            VkTheme {
                Box(
                    modifier = Modifier.background(VkTheme.colors.colorBackground)
                ) {
                    when (screenKey) {
                        ComponentScreenKey.ALERT -> AlertScreenContent.Content()
                        ComponentScreenKey.BUTTON -> ButtonScreenContent.Content()
                        ComponentScreenKey.INPUT -> InputScreenContent.Content()
                        ComponentScreenKey.SWITCH -> SwitchScreenContent.Content()
                        ComponentScreenKey.TEXT -> TextScreenContent.Content()
                    }
                }
            }
        }
    }

    companion object {
        private const val SCREEN_KEY_KEY = "screen_key"

        fun newInstance(screenKey: ComponentScreenKey) = ComponentScreenFragment().apply {
            arguments = bundleOf(
                SCREEN_KEY_KEY to screenKey
            )
        }
    }
}
