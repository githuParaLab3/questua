package com.questua.app.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public // Ícone de globo/mundo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.questua.app.core.ui.components.QuestuaButton

@Composable
fun InitialScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // LOGOTIPO
        Icon(
            imageVector = Icons.Default.Public,
            contentDescription = "Logo Questua",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // NOME DO APP
        Text(
            text = "Questua",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // SLOGAN
        Text(
            text = "Aprenda idiomas explorando o mundo real",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(64.dp))

        // BOTÃO PRIMÁRIO (Criar Conta)
        QuestuaButton(
            text = "Começar Aventura",
            onClick = onNavigateToRegister,
            containerColor = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // BOTÃO SECUNDÁRIO (Entrar)
        // Usamos a cor Secondary para diferenciar visualmente sem precisar de um novo componente "Outlined"
        QuestuaButton(
            text = "Já tenho conta",
            onClick = onNavigateToLogin,
            containerColor = MaterialTheme.colorScheme.secondary
        )
    }
}