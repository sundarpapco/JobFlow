package com.sivakasi.papco.jobflow.screens.search

import com.algolia.client.api.SearchClient
import com.algolia.client.model.search.Hit
import com.algolia.client.model.search.SearchParamsObject
import com.algolia.client.model.search.SearchResponse
import com.algolia.client.model.search.TypoTolerance
import com.algolia.client.model.search.TypoToleranceEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

interface AlgoliaClient {
    suspend fun search(queryString: String, pageNumber: Int = 0): AlgoliaLoadResult
}

class AlgoliaClientImpl : AlgoliaClient {

    private val indexName = "completed_jobs"

    private val client = SearchClient(
        appId = "72NK3L9K3Y",
        apiKey = "9e2f6cad28ac04bbd386b766b805157b"
    )

    // Using a configuration of JSON that ignores unknown keys to prevent crashes
    // if Algolia returns extra metadata fields (like _highlightResult)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun search(queryString: String, pageNumber: Int): AlgoliaLoadResult =
        withContext(Dispatchers.IO) {

            val searchParams = SearchParamsObject(
                query = queryString,
                typoTolerance = TypoTolerance.TypoToleranceEnumValue(TypoToleranceEnum.False),
                page = pageNumber,
                hitsPerPage = 100
            )

            val response: SearchResponse = client.searchSingleIndex(
                indexName = indexName,
                searchParams = searchParams
            )

            // Fix for unresolved deserialize:
            // Encode the V3 Hit model to a JsonElement, then decode it into your custom AlgoliaRecord
            val data = response.hits.map { hit ->
                val hitJsonElement = json.encodeToJsonElement(Hit.serializer(), hit)
                json.decodeFromJsonElement(AlgoliaRecord.serializer(), hitJsonElement)
            }

            AlgoliaLoadResult(
                data = data,
                totalPages = response.nbPages ?: 0,
                loadedPage = response.page ?: 0,
                totalHits = response.nbHits ?: 0
            )
        }
}

data class AlgoliaLoadResult(
    val data: List<AlgoliaRecord>,
    val totalPages: Int,
    val loadedPage: Int,
    val totalHits: Int
)