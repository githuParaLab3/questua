package com.questua.app.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuestuaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    isSecondary: Boolean = false // Flag para estilo "outline" ou secundário
) {
    val backgroundColor = if (isSecondary) Color.White else containerColor
    val textColor = if (isSecondary) MaterialTheme.colorScheme.secondary else contentColor
    val elevation = if (isSecondary) 0.dp else 8.dp
    val borderStroke = if (isSecondary) androidx.compose.foundation.BorderStroke(2.dp, com.questua.app.core.ui.theme.Slate200) else null

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp) // Botões mais altos como no design (py-4)
            .shadow(
                elevation = if (isSecondary) 0.dp else 10.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isSecondary) Color.Transparent else containerColor.copy(alpha = 0.5f)
            ),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp), // rounded-xl
        border = borderStroke,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp, // Sombra controlada pelo modifier .shadow acima para cor customizada
            pressedElevation = 0.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = textColor,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}