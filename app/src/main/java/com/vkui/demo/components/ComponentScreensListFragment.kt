package com.vkui.demo.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.compose.content
import com.vkui.components.VkText
import com.vkui.demo.R
import com.vkui.demo.component.ComponentScreenFragment
import com.vkui.demo.component.ComponentScreenKey
import com.vkui.theme.VkTheme

class ComponentScreensListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        VkTheme {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(120.dp),
                contentPadding = PaddingValues(
                    vertical = VkTheme.dimens.sizeBasePaddingVertical,
                    horizontal = VkTheme.dimens.sizeBasePaddingHorizontal
                ),
                horizontalArrangement = Arrangement.spacedBy(VkTheme.dimens.sizeButtonGroupGapSmall),
                verticalArrangement = Arrangement.spacedBy(VkTheme.dimens.sizeButtonGroupGapSmall),
                modifier = Modifier
                    .fillMaxSize()
                    .background(VkTheme.colors.colorBackground)
                    .systemBarsPadding()
            ) {
                items(ComponentScreenKey.entries) { screenKey ->
                    ComponentButton(
                        screenKey = screenKey,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

    @Composable
    private fun ComponentButton(screenKey: ComponentScreenKey, modifier: Modifier = Modifier) {
        val text = getComponentName(screenKey)
        val shape = RoundedCornerShape(VkTheme.dimens.sizeBorderRadius)
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .background(VkTheme.colors.colorBackgroundAccentThemedAlpha, shape)
                .border(VkTheme.dimens.sizeBorder1x, VkTheme.colors.colorButtonStroke, shape)
                .heightIn(min = VkTheme.dimens.sizeButtonSmallHeight)
                .clip(shape)
                .clickable {
                    parentFragmentManager.commit {
                        add(R.id.root_fragment_container, ComponentScreenFragment.newInstance(screenKey))
                        addToBackStack(null)
                    }
                }
        ) {
            VkText(text = text, style = VkTheme.typography.fontCaption1, color = VkTheme.colors.colorButtonText)
        }
    }

    private fun getComponentName(screenKey: ComponentScreenKey): String = when (screenKey) {
        ComponentScreenKey.ALERT -> "Alert"
        ComponentScreenKey.BUTTON -> "Button"
        ComponentScreenKey.INPUT -> "Input"
        ComponentScreenKey.SWITCH -> "Switch"
        ComponentScreenKey.TEXT -> "Text"
    }
}
