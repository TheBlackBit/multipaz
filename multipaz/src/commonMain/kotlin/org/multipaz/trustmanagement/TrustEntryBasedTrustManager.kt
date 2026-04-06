package org.multipaz.trustmanagement

import kotlinx.coroutines.flow.SharedFlow

/**
 * Common interface for [TrustManagerInterface] implementations built on top of [TrustEntry].
 */
interface TrustEntryBasedTrustManager: TrustManagerInterface {
    /**
     * A reactive stream of events emitted whenever the underlying trust data changes.
     * Observers can collect this flow to know when to refresh their cached UI states.
     */
    val eventFlow: SharedFlow<Unit>

    /**
     * Retrieves all [TrustEntry] items currently managed.
     *
     * @return A list of [TrustEntry] objects (e.g., [TrustEntryX509Cert], [TrustEntryVical]).
     */
    suspend fun getEntries(): List<TrustEntry>
}