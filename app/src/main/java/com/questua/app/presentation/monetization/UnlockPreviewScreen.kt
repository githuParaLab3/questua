package com.questua.app.presentation.monetization

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questua.app.core.ui.components.QuestuaButton
import com.questua.app.domain.model.Product
import com.questua.app.domain.model.UnlockRequirement

@Composable
fun UnlockPreviewScreen(
    onNavigateBack: () -> Unit,
    onUnlockSuccess: () -> Unit,
    viewModel: UnlockPreviewViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.purchaseSuccess) {
        if (state.purchaseSuccess) {
            onUnlockSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Conteúdo Bloqueado",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Complete os requisitos abaixo para desbloquear este conteúdo.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    state.requirement?.let { req ->
                        if (req.premiumAccess) {
                            PremiumContentSection(
                                products = state.products,
                                isProcessing = state.isPaymentProcessing,
                                onBuyClick = { product -> viewModel.purchaseProduct(product) }
                            )
                        } else {
                            RequirementsList(
                                requirement = req,
                                userLevel = state.userLevel
                            )
                        }
                    }

                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.error ?: "Erro desconhecido",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    QuestuaButton(
                        text = "Voltar",
                        onClick = onNavigateBack,
                        isSecondary = true
                    )
                }
            }
        }
    }
}

@Composable
fun RequirementsList(requirement: UnlockRequirement, userLevel: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Requisitos de Progresso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            requirement.requiredGamificationLevel?.let { requiredLevel ->
                RequirementItem(
                    text = "Nível $requiredLevel",
                    isMet = userLevel >= requiredLevel,
                    description = "Seu nível atual: $userLevel"
                )
            }

            requirement.requiredCefrLevel?.let { cefr ->
                RequirementItem(
                    text = "Nível de Idioma $cefr",
                    isMet = false, // Implementar lógica de comparação de CEFR
                    description = "Necessário proficiência $cefr"
                )
            }

            if (requirement.requiredQuests.isNotEmpty()) {
                RequirementItem(
                    text = "${requirement.requiredQuests.size} Missões Específicas",
                    isMet = false, // Implementar verificação de missões completas
                    description = "Complete as missões anteriores"
                )
            }
        }
    }
}

@Composable
fun RequirementItem(text: String, isMet: Boolean, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.CheckCircle else Icons.Default.Lock,
            contentDescription = null,
            tint = if (isMet) Color.Green else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun PremiumContentSection(
    products: List<Product>,
    isProcessing: Boolean,
    onBuyClick: (Product) -> Unit
) {
    Text(
        text = "Acesso Premium",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    if (products.isEmpty()) {
        Text("Nenhum produto disponível para compra.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(products) { product ->
                ProductCard(product, isProcessing, onBuyClick)
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isProcessing: Boolean,
    onBuyClick: (Product) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            product.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${product.currency} ${(product.priceCents / 100.0)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                QuestuaButton(
                    text = "Comprar",
                    onClick = { onBuyClick(product) },
                    isLoading = isProcessing,
                    modifier = Modifier.width(120.dp),
                    leadingIcon = Icons.Default.ShoppingCart
                )
            }
        }
    }
}