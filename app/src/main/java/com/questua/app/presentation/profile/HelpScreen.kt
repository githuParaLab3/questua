package com.questua.app.presentation.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate

data class HelpTopic(
    val title: String,
    val content: String
)

private val helpTopics = listOf(
    HelpTopic(
        title = "Como mudar meu Idioma de Aprendizado?",
        content = "Para trocar o idioma principal, vá para a tela 'Idiomas' (acessível pelo Hub principal ou pelo menu), clique no idioma desejado e selecione 'Trocar'. Lembre-se que você só pode ter um idioma ativo por vez, mas o progresso dos outros é mantido."
    ),
    HelpTopic(
        title = "O que são Quest Points?",
        content = "Quest Points representam locais de interesse no mapa, como monumentos ou bairros. Cada ponto contém um conjunto de Quests (missões) que você pode completar para ganhar XP e avançar no aprendizado."
    ),
    HelpTopic(
        title = "Minha ofensiva (Streak) foi perdida!",
        content = "Sua ofensiva é mantida se você completar pelo menos uma lição ou atividade por dia. Se você a perdeu, confira se realizou alguma atividade após a meia-noite. Em casos de erros de sincronização, use a opção 'Relatar um Problema' abaixo."
    ),
    HelpTopic(
        title = "O que é o Modo Premium?",
        content = "O Modo Premium desbloqueia todo o conteúdo restrito, como missões especiais, cidades exclusivas e algumas funcionalidades de IA. Para assinar, visite a seção de Monetização."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReport: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajuda e Suporte", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Instruções e Perguntas Frequentes",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(helpTopics) { topic ->
                    HelpTopicItem(topic = topic)
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    Divider()
                    Text(
                        text = "Precisa de mais ajuda?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                item {
                    SettingsActionItem(
                        label = "Relatar um Problema",
                        icon = Icons.Default.SupportAgent,
                        onClick = onNavigateToReport,
                        textColor = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun HelpTopicItem(topic: HelpTopic) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.rotate(if (expanded) 90f else 0f)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = topic.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}