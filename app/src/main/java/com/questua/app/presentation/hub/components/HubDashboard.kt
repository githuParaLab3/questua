package com.questua.app.presentation.hub.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.domain.model.Language
import com.questua.app.domain.model.UserLanguage

@Composable
fun HubDashboard(
    userLanguage: UserLanguage?,    // Dados de progresso (XP, Nível)
    languageDetails: Language?,     // Dados visuais (Nome, Ícone)
    onContinue: () -> Unit,
    onChangeLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Cabeçalho do Card: Ícone e Nome do Idioma
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // CORREÇÃO: Usa o objeto languageDetails para pegar o nome
                    Text(
                        text = languageDetails?.name ?: "Carregando...",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                TextButton(onClick = onChangeLanguage) {
                    Text("Mudar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Informações de Progresso
            val progressText = if (userLanguage != null) {
                "Nível ${userLanguage.gamificationLevel} • ${userLanguage.xpTotal} XP"
            } else {
                "Comece sua jornada agora!"
            }

            Text(
                text = progressText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuestuaButton(
                text = "Continuar Jornada",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primary,
                // Só habilita se tivermos os dados carregados
                enabled = userLanguage != null && languageDetails != null
            )
        }
    }
}