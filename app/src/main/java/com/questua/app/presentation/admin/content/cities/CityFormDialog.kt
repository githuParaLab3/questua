package com.questua.app.presentation.admin.content.cities

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.questua.app.core.common.URIPathHelper
import com.questua.app.core.common.toFullImageUrl
import com.questua.app.core.ui.components.QuestuaTextField
import com.questua.app.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityFormDialog(
    city: City? = null,
    languages: List<Language>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, desc: String, langId: String, poly: BoundingPolygon?, lat: Double, lon: Double, img: File?, icon: File?, prem: Boolean, unlock: UnlockRequirement?, ai: Boolean, pub: Boolean) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(city?.name ?: "") }
    var code by remember { mutableStateOf(city?.countryCode ?: "") }
    var desc by remember { mutableStateOf(city?.description ?: "") }
    var langId by remember { mutableStateOf(city?.languageId ?: "") }
    var lat by remember { mutableStateOf(city?.lat?.toString() ?: "") }
    var lon by remember { mutableStateOf(city?.lon?.toString() ?: "") }
    var isPremium by remember { mutableStateOf(city?.isPremium ?: false) }
    var isPublished by remember { mutableStateOf(city?.isPublished ?: true) }

    var selectedImageFile by remember { mutableStateOf<File?>(null) }
    var selectedIconFile by remember { mutableStateOf<File?>(null) }

    // Para preview apenas
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedIconUri by remember { mutableStateOf<Uri?>(null) }

    var polyText by remember { mutableStateOf(city?.boundingPolygon?.let { Json.encodeToString(it.coordinates) } ?: "[]") }

    // Estados amigáveis para UnlockRequirement
    var reqPremium by remember { mutableStateOf(city?.unlockRequirement?.premiumAccess ?: false) }
    var reqLevel by remember { mutableStateOf(city?.unlockRequirement?.requiredGamificationLevel?.toString() ?: "") }
    var reqCefr by remember { mutableStateOf(city?.unlockRequirement?.requiredCefrLevel ?: "") }

    var showLangPicker by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            selectedImageFile = URIPathHelper.getFileFromUri(context, it)
        }
    }
    val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedIconUri = it
            selectedIconFile = URIPathHelper.getFileFromUri(context, it)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(if (city == null) "Nova Cidade" else "Editar Cidade", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Seção de Imagens
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Seletor de Imagem Principal
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, QuestuaGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { imagePicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val imgModel = selectedImageUri ?: city?.imageUrl?.toFullImageUrl()
                        if (imgModel != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(imgModel).crossfade(true).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Capa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Seletor de Ícone
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, QuestuaGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { iconPicker.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val iconModel = selectedIconUri ?: city?.iconUrl?.toFullImageUrl()
                        if (iconModel != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(iconModel).crossfade(true).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.padding(16.dp).fillMaxSize()
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Ícone", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                QuestuaTextField(value = name, onValueChange = { name = it }, label = "Nome da Cidade")

                OutlinedCard(
                    onClick = { showLangPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    val current = languages.find { it.id == langId }
                    ListItem(
                        headlineContent = { Text(current?.name ?: "Selecionar Idioma") },
                        supportingContent = { Text(if(langId.isEmpty()) "Obrigatório" else "Código: ${current?.code ?: ""}") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) { QuestuaTextField(value = lat, onValueChange = { lat = it }, label = "Latitude") }
                    Box(Modifier.weight(1f)) { QuestuaTextField(value = lon, onValueChange = { lon = it }, label = "Longitude") }
                }

                QuestuaTextField(value = code, onValueChange = { code = it }, label = "Código País (ex: BR)")
                QuestuaTextField(value = desc, onValueChange = { desc = it }, label = "Descrição")
                QuestuaTextField(value = polyText, onValueChange = { polyText = it }, label = "Polígono (JSON)")

                // Seção Amigável de Requisitos
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text("Requisitos de Desbloqueio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = QuestuaGold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = reqPremium,
                        onCheckedChange = { reqPremium = it },
                        colors = CheckboxDefaults.colors(checkedColor = QuestuaGold, checkmarkColor = Color.Black)
                    )
                    Text("Exigir Assinatura Premium")
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        QuestuaTextField(
                            value = reqLevel,
                            onValueChange = { reqLevel = it },
                            label = "Nível Mín."
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        QuestuaTextField(
                            value = reqCefr,
                            onValueChange = { reqCefr = it },
                            label = "CEFR (ex: B1)"
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPublished, onCheckedChange = { isPublished = it }, colors = CheckboxDefaults.colors(checkedColor = QuestuaGold, checkmarkColor = Color.Black))
                    Text("Publicado")
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = isPremium, onCheckedChange = { isPremium = it }, colors = CheckboxDefaults.colors(checkedColor = QuestuaGold, checkmarkColor = Color.Black))
                    Text("Premium")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val poly = try { BoundingPolygon(Json.decodeFromString(polyText)) } catch (e: Exception) { null }

                    // Monta o objeto UnlockRequirement a partir dos campos individuais
                    val unlock = UnlockRequirement(
                        premiumAccess = reqPremium,
                        requiredGamificationLevel = reqLevel.toIntOrNull(),
                        requiredCefrLevel = reqCefr.ifEmpty { null }
                    )

                    onConfirm(name, code, desc, langId, poly, lat.toDoubleOrNull() ?: 0.0, lon.toDoubleOrNull() ?: 0.0, selectedImageFile, selectedIconFile, isPremium, unlock, false, isPublished)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuestuaGold,
                    contentColor = Color.Black
                )
            ) { Text("Confirmar", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) { Text("Cancelar") }
        }
    )

    if (showLangPicker) {
        AlertDialog(
            onDismissRequest = { showLangPicker = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Selecione o Idioma", fontWeight = FontWeight.Bold) },
            text = {
                Box(Modifier.heightIn(max = 400.dp)) {
                    LazyColumn {
                        items(languages) { lang ->
                            ListItem(
                                modifier = Modifier.clickable {
                                    langId = lang.id
                                    showLangPicker = false
                                },
                                headlineContent = { Text(lang.name) },
                                supportingContent = { Text(lang.code) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangPicker = false }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) { Text("Fechar") }
            }
        )
    }
}