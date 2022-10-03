package com.sivakasi.papco.jobflow.screens.search

import android.content.Context
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.algolia.search.exception.UnreachableHostsException
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.SearchModel
import com.sivakasi.papco.jobflow.util.ResourceNotFoundException

class AlgoliaDataSource(
    private val context: Context,
    private val algoliaClient: AlgoliaClient,
    val query: String?
) : PagingSource<Int, SearchModel>() {

    override fun getRefreshKey(state: PagingState<Int, SearchModel>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchModel> {

        if (query == null || query.isBlank())
            return LoadResult.Page(emptyList(), null, null)

        val pageToLoad = params.key ?: 0

        return try {
            val loadResult = algoliaClient.search(query, pageToLoad)
            val data = loadResult.data.map { it.toSearchModel(context,query) }
            val nextPage = if (loadResult.loadedPage < loadResult.totalPages - 1)
                pageToLoad + 1
            else
                null

            if (data.isNotEmpty())
                LoadResult.Page(data, null, nextPage)
            else
                LoadResult.Error(ResourceNotFoundException(context.getString(R.string.no_results_found)))

        } catch (e: UnreachableHostsException) {
            LoadResult.Error(Throwable(context.getString(R.string.check_internet_connection)))
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}