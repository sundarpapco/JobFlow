package com.sivakasi.papco.jobflow.screens.search

import android.content.Context
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.algolia.search.exception.UnreachableHostsException
import com.sivakasi.papco.jobflow.R
import com.sivakasi.papco.jobflow.models.SearchModel

class AlgoliaDataSource(
    private val context: Context,
    private val algoliaClient: AlgoliaClient,
    val query: String?
) : PagingSource<Int, SearchModel>() {

    init {
        Log.d("SUNDAR", "Data source created with $query")
    }

    override fun getRefreshKey(state: PagingState<Int, SearchModel>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchModel> {

        if (query == null || query.isBlank())
            return LoadResult.Page(emptyList(), null, null)

        val pageToLoad = params.key ?: 0

        return try {
            val loadResult = algoliaClient.search(query, pageToLoad)
            val data = loadResult.data.map { it.toSearchModel(context) }
            val nextPage = if (loadResult.loadedPage < loadResult.totalPages - 1)
                pageToLoad + 1
            else
                null
            LoadResult.Page(data, null, nextPage)
        } catch (e: UnreachableHostsException) {
            LoadResult.Error(Throwable(context.getString(R.string.check_internet_connection)))
        } catch (e:Exception){
            LoadResult.Error(e)
        }
    }
}