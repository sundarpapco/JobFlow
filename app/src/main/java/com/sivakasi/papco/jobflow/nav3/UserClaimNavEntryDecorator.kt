package com.sivakasi.papco.jobflow.nav3

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey

val LocalUserClaim = compositionLocalOf<String?> {null }

class UserClaimNavEntryDecorator(claim:String?): NavEntryDecorator<NavKey> (
    decorate = {entry->
        CompositionLocalProvider(LocalUserClaim provides claim) {
            entry.Content()
        }
    }
)