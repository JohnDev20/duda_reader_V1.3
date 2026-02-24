package com.duda.app.core.util

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Float.toProgressString(): String = "${(this * 100).toInt()}%"

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

/**
 * Limita o texto a [maxLength] caracteres, adicionando "..." se necessário.
 */
fun String.ellipsize(maxLength: Int): String =
    if (length <= maxLength) this else "${take(maxLength - 3)}..."
