package com.vkui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.vkui.theme.VkTheme

@Composable
fun VkAlert(
    title: String?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    positiveButton: (@Composable VkAlertButtonScope.() -> Unit)? = null,
    negativeButton: (@Composable VkAlertButtonScope.() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    val shape = RoundedCornerShape(VkTheme.dimens.sizeBorderRadiusPaper)
    @OptIn(ExperimentalMaterial3Api::class)
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.background(VkTheme.colors.colorBackgroundModal, shape),
        properties = properties,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(VkTheme.dimens.spacingSize2Xl),
            modifier = Modifier.padding(
                top = VkTheme.dimens.spacingSize4Xl,
                start = VkTheme.dimens.sizeBasePaddingHorizontal,
                end = VkTheme.dimens.sizeBasePaddingHorizontal,
                bottom = VkTheme.dimens.spacingSize2Xl
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(VkTheme.dimens.spacingSizeM),
                modifier = Modifier.padding(horizontal = VkTheme.dimens.spacingSizeM)
            ) {
                if (title != null) {
                    VkText(
                        text = title,
                        style = VkTheme.typography.fontTitle2,
                        color = VkTheme.colors.colorTextPrimary
                    )
                }
                if (subtitle != null) {
                    VkText(
                        text = subtitle,
                        style = VkTheme.typography.fontText,
                        color = VkTheme.colors.colorTextSubhead
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(VkTheme.dimens.spacingSizeM),
                modifier = Modifier.align(Alignment.End)
            ) {
                negativeButton?.invoke(VkAlertButtonScopeImpl)
                positiveButton?.invoke(VkAlertButtonScopeImpl)
            }
        }
    }
}

sealed class VkAlertButtonScope {
    @Composable
    fun NeutralAlertButton(
        text: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
    ) {
        NeutralAlertButtonImpl(text, modifier, onClick)
    }

    @Composable
    protected abstract fun NeutralAlertButtonImpl(
        text: String,
        modifier: Modifier,
        onClick: () -> Unit,
    )
}

private object VkAlertButtonScopeImpl : VkAlertButtonScope() {
    @Composable
    override fun NeutralAlertButtonImpl(
        text: String,
        modifier: Modifier,
        onClick: () -> Unit,
    ) {
        VkButton(text, modifier, onClick)
    }
}
