package com.questua.app.presentation.admin.content.achievements

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.questua.app.core.common.URIPathHelper
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.enums.AchievementConditionType
import com.questua.app.domain.enums.RarityType
import com.questua.app.domain.model.Achievement
import com.questua.app.presentation.admin.content.dialogues.MediaPickerField
import com.questua.app.presentation.admin.content.dialogues.SectionTitle
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementFormDialog(
    achievement: Achievement? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        key: String, name: String, desc: String,
        icon: Any?,
        rarity: RarityType, xp: Int, isHidden: Boolean, isGlobal: Boolean,
        category: String, conditionType: AchievementConditionType, targetId: String,
        requiredAmount: Int
    ) -> Unit
) {
    val context = LocalContext.current

    var keyName by remember { mutableStateOf(achievement?.keyName ?: "") }
    var name by remember { mutableStateOf(achievement?.name ?: "") }
    var description by remember { mutableStateOf(achievement?.description ?: "") }
    var xpReward by remember { mutableStateOf(achievement?.xpReward?.toString() ?: "50") }
    var rarity by remember { mutableStateOf(achievement?.rarity ?: RarityType.COMMON) }

    var isHidden by remember { mutableStateOf(achievement?.isHidden ?: false) }
    var isGlobal by remember { mutableStateOf(achievement?.isGlobal ?: true) }
    var category by remember { mutableStateOf(achievement?.category ?: "") }
    var conditionType by remember { mutableStateOf(achievement?.conditionType ?: AchievementConditionType.COMPLETE_SPECIFIC_QUEST) }
    var targetId by remember { mutableStateOf(achievement?.targetId ?: "") }
    var requiredAmount by remember { mutableStateOf(achievement?.requiredAmount?.toString() ?: "1") }

    var selectedIcon by remember { mutableStateOf<Any?>(achievement?.iconUrl) }

    var showRarityPicker by remember { mutableStateOf(false) }
    var expandedCondition by remember { mutableStateOf(false) }

    val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedIcon = URIPathHelper.getFileFromUri(context, it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (achievement == null) "Nova Conquista" else "Editar Conquista", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionTitle("Informações Básicas")
                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome da Conquista")
                QuestuaTextField(value = keyName, onValueChange = { keyName = it }, label = "Chave Única (ID interno)")
                QuestuaTextField(value = description, onValueChange = { description = it }, label = "Descrição")
                QuestuaTextField(value = xpReward, onValueChange = { xpReward = it }, label = "Recompensa (XP)")

                OutlinedCard(
                    onClick = { showRarityPicker = true },
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    ListItem(
                        headlineContent = { Text("Raridade: ${rarity.name}", fontWeight = FontWeight.Bold) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                SectionTitle("Regras e Condições")

                ExposedDropdownMenuBox(
                    expanded = expandedCondition,
                    onExpandedChange = { expandedCondition = !expandedCondition }
                ) {
                    OutlinedTextField(
                        value = conditionType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Condição de Desbloqueio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCondition) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCondition,
                        onDismissRequest = { expandedCondition = false }
                    ) {
                        AchievementConditionType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    conditionType = type
                                    expandedCondition = false
                                }
                            )
                        }
                    }
                }

                QuestuaTextField(value = targetId, onValueChange = { targetId = it }, label = "Target ID (Opcional)")
                QuestuaTextField(value = requiredAmount, onValueChange = { requiredAmount = it }, label = "Quantidade Necessária")
                QuestuaTextField(value = category, onValueChange = { category = it }, label = "Categoria do Jogo")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isHidden, onCheckedChange = { isHidden = it })
                        Text("Oculto")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isGlobal, onCheckedChange = { isGlobal = it })
                        Text("Global")
                    }
                }

                SectionTitle("Visual")
                MediaPickerField(
                    label = "Ícone",
                    value = selectedIcon,
                    icon = Icons.Default.Image,
                    onPick = { iconPicker.launch("image/*") },
                    onClear = { selectedIcon = null }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        keyName, name, description, selectedIcon,
                        rarity, xpReward.toIntOrNull() ?: 0, isHidden, isGlobal,
                        category, conditionType, targetId, requiredAmount.toIntOrNull() ?: 1
                    )
                },
                enabled = name.isNotBlank() && keyName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107), contentColor = Color.Black)
            ) { Text("Salvar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) { Text("Cancelar") }
        },
        shape = RoundedCornerShape(24.dp)
    )

    if (showRarityPicker) {
        AlertDialog(
            onDismissRequest = { showRarityPicker = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Selecione a Raridade", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    RarityType.values().forEach { type ->
                        val color = when (type) {
                            RarityType.LEGENDARY -> Color(0xFFFFD700)
                            RarityType.EPIC -> Color(0xFF9C27B0)
                            RarityType.RARE -> Color(0xFF2196F3)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        ListItem(
                            modifier = Modifier.clickable { rarity = type; showRarityPicker = false },
                            headlineContent = { Text(type.name, color = color, fontWeight = FontWeight.Bold) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showRarityPicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) { Text("Fechar") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}