package com.duda.app.data.remote.api

import com.duda.app.data.remote.dto.DictionaryResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Free Dictionary API — https://api.dictionaryapi.dev
 * Não requer autenticação. Suporta inglês e outros idiomas.
 *
 * Exemplo de chamada: GET /api/v2/entries/en/hello
 */
interface DictionaryApiService {

    @GET("api/v2/entries/en/{word}")
    suspend fun getDefinition(@Path("word") word: String): List<DictionaryResponseDto>
}
