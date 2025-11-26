package com.questua.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun LoadingSpinner(
    modifier: Modifier = Modifier,
    transparentBackground: Boolean = false
) {
    val bgColor = if (transparentBackground) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}