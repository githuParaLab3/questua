package com.questua.app.core.common

import android.util.Patterns

// Validação de Email simples
fun String.isValidEmail(): Boolean {
    return this.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

// Formatação ou Capitalização
fun String.capitalizeFirstLetter(): String {
    return this.lowercase().replaceFirstChar { it.uppercase() }
}

// Extensão para tratar erros de API genéricos se necessário
fun Throwable.userFriendlyMessage(): String {
    return this.localizedMessage ?: "Ocorreu um erro inesperado"
}