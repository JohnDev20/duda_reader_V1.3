package com.duda.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DictionaryResponseDto(
    @SerializedName("word") val word: String,
    @SerializedName("phonetic") val phonetic: String?,
    @SerializedName("meanings") val meanings: List<MeaningDto>
)

data class MeaningDto(
    @SerializedName("partOfSpeech") val partOfSpeech: String,
    @SerializedName("definitions") val definitions: List<DefinitionDto>
)

data class DefinitionDto(
    @SerializedName("definition") val definition: String,
    @SerializedName("example") val example: String?
)

/**
 * Converte a resposta da API em uma string formatada legível.
 */
fun List<DictionaryResponseDto>.toFormattedDefinition(): String {
    val sb = StringBuilder()
    this.firstOrNull()?.meanings?.forEachIndexed { index, meaning ->
        if (index > 0) sb.append("\n\n")
        sb.append("[${meaning.partOfSpeech}]\n")
        meaning.definitions.take(3).forEachIndexed { defIndex, def ->
            sb.append("${defIndex + 1}. ${def.definition}")
            def.example?.let { sb.append("\n   Ex: \"$it\"") }
            if (defIndex < meaning.definitions.take(3).size - 1) sb.append("\n")
        }
    }
    return sb.toString().ifEmpty { "Definição não encontrada." }
}
