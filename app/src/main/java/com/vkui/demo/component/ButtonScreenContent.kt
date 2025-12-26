package com.vkui.demo.component

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.vkui.compose.components.VkButton

object ButtonScreenContent {
    @Composable
    fun Content() {
        val context = LocalContext.current
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            VkButton(text = "Click me!") {
                Toast.makeText(context, "Thanks!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
