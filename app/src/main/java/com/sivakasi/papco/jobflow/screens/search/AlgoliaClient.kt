package com.sivakasi.papco.jobflow.screens.search

import com.algolia.search.client.ClientSearch
import com.algolia.search.dsl.customRanking
import com.algolia.search.dsl.query
import com.algolia.search.dsl.settings
import com.algolia.search.helper.deserialize
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.algolia.search.model.response.ResponseSearch
import com.algolia.search.model.search.TypoTolerance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AlgoliaClient {
    suspend fun search(queryString: String, pageNumber: Int = 0): AlgoliaLoadResult
}


class AlgoliaClientImpl : AlgoliaClient {

    private val index = ClientSearch(
        ApplicationID("72NK3L9K3Y"),
        APIKey("9e2f6cad28ac04bbd386b766b805157b")
    ).initIndex(
        IndexName("completed_jobs")
    )


    override suspend fun search(queryString: String, pageNumber: Int): AlgoliaLoadResult =
        withContext(Dispatchers.IO) {

            settings {
                customRanking {

                }
            }

            val query = query {
                query = queryString
                typoTolerance = TypoTolerance.False
                page = pageNumber
                hitsPerPage = 100
            }
            val response: ResponseSearch = index.search(query)
            val data = response.hits.deserialize(AlgoliaRecord.serializer())
            AlgoliaLoadResult(
                data,
                totalPages = response.nbPages,
                loadedPage = response.page,
                totalHits = response.nbHitsOrNull ?: 0
            )

        }
}

data class AlgoliaLoadResult(
    val data: List<AlgoliaRecord>,
    val totalPages: Int,
    val loadedPage: Int,
    val totalHits: Int
)