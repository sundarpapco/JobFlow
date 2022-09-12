package com.sivakasi.papco.jobflow.screens.processinghistory

import com.sivakasi.papco.jobflow.data.ProcessingHistory
import com.sivakasi.papco.jobflow.models.SearchModel

data class PreviousHistoryScreenState(
    val printOrder: SearchModel,
    val history: List<ProcessingHistory>
)
