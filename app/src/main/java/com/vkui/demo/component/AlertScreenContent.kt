package com.vkui.demo.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vkui.components.VkAlert
import com.vkui.components.VkButton

object AlertScreenContent {
    private val isDialogShown = mutableStateOf(false)

    @Composable
    fun Content() {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            VkButton(text = "Show") {
                isDialogShown.value = true
            }
        }

        if (isDialogShown.value) {
            VkAlert(
                onDismissRequest = { isDialogShown.value = false },
                title = "Удаление документа",
                subtitle = "Вы уверены, что хотите удалить этот документ?",
                positiveButton = {
                    NeutralAlertButton(text = "Удалить") {
                        isDialogShown.value = false
                    }
                },
                negativeButton = {
                    NeutralAlertButton(text = "Отмена") {
                        isDialogShown.value = false
                    }
                }
            )
        }
    }
}
