package org.multipaz.compose.trustmanagement

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.multipaz.asn1.OID
import org.multipaz.mdoc.rical.SignedRical
import org.multipaz.mdoc.vical.SignedVical
import org.multipaz.multipaz_compose.generated.resources.Res
import org.multipaz.multipaz_compose.generated.resources.trust_entry_details_certificate
import org.multipaz.multipaz_compose.generated.resources.trust_entry_details_rical
import org.multipaz.multipaz_compose.generated.resources.trust_entry_details_vical
import org.multipaz.multipaz_compose.generated.resources.trust_entry_rical_fallback_name
import org.multipaz.multipaz_compose.generated.resources.trust_entry_vical_fallback_name
import org.multipaz.trustmanagement.TrustEntry
import org.multipaz.trustmanagement.TrustEntryBasedTrustManager
import org.multipaz.trustmanagement.TrustEntryRical
import org.multipaz.trustmanagement.TrustEntryVical
import org.multipaz.trustmanagement.TrustEntryX509Cert
import org.multipaz.trustmanagement.TrustManager

/**
 * A presentation model that bridges a [TrustEntryBasedTrustManager] with UI components.
 *
 * This class observes the underlying [TrustEntryBasedTrustManager] for changes and exposes the
 * current list of trust entries as a reactive [StateFlow] of [TrustEntryInfo] objects.
 * It handles the parsing of raw trust entries into UI-friendly data structures.
 *
 * @property trustManager The underlying [TrustManager] being observed.
 * @property coroutineScope The scope used to run background queries for the state flow.
 */
class TrustManagerModel(
    val trustManager: TrustEntryBasedTrustManager,
    coroutineScope: CoroutineScope
) {
    /**
     * A [StateFlow] containing the current, parsed list of trust entries.
     *
     * - The initial value is `null` while the first read and parsing are in progress.
     * - It automatically re-queries the [trustManager] whenever it emits a change notification.
     * - Uses [SharingStarted.WhileSubscribed] to pause observations if the UI is completely hidden.
     */
    val trustManagerInfos: StateFlow<List<TrustEntryInfo>?> = trustManager.eventFlow
        .onStart { emit(Unit) } // Trigger the initial load
        .map { fetchAndParseEntries() }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Fetches the latest entries from the [trustManager] and parses their contents
     * (such as VICALs and RICALs).
     */
    private suspend fun fetchAndParseEntries(): List<TrustEntryInfo> {
        val infos = mutableListOf<TrustEntryInfo>()
        trustManager.getEntries().forEach { entry ->
            val signedVical = if (entry is TrustEntryVical) {
                SignedVical.parse(
                    encodedSignedVical = entry.encodedSignedVical.toByteArray(),
                    disableSignatureVerification = true
                )
            } else {
                null
            }
            val signedRical = if (entry is TrustEntryRical) {
                SignedRical.parse(
                    encodedSignedRical = entry.encodedSignedRical.toByteArray(),
                    disableSignatureVerification = true
                )
            } else {
                null
            }
            infos.add(TrustEntryInfo(
                entry = entry,
                manager = trustManager,
                signedVical = signedVical,
                signedRical = signedRical
            ))
        }
        return infos
    }
}

/**
 * Generates a descriptive fallback name for a [TrustEntry] based on its specific type
 * and underlying data (e.g., Common Name for an X.509 cert, Provider for a VICAL/RICAL).
 */
@Composable
fun TrustEntry.getFallbackName(
    signedVical: SignedVical?,
    signedRical: SignedRical?
): String {
    when (this) {
        is TrustEntryX509Cert -> {
            val subject = certificate.subject
            val commonName = subject.components[OID.COMMON_NAME.oid]
            if (commonName != null) {
                return commonName.value
            }
            return subject.name
        }
        is TrustEntryVical -> {
            return stringResource(
                Res.string.trust_entry_vical_fallback_name,
                signedVical!!.vical.vicalProvider
            )
        }
        is TrustEntryRical -> {
            return stringResource(
                Res.string.trust_entry_rical_fallback_name,
                signedRical!!.rical.provider
            )
        }
    }
}

/**
 * Generates a brief detail string describing the contents of a [TrustEntry],
 * such as the number of certificates contained within a VICAL or RICAL.
 */
@Composable
fun TrustEntry.getDetails(
    signedVical: SignedVical?,
    signedRical: SignedRical?
): String {
    when (this) {
        is TrustEntryX509Cert -> {
            return stringResource(Res.string.trust_entry_details_certificate)
        }
        is TrustEntryVical -> {
            val size = signedVical!!.vical.certificateInfos.size
            return pluralStringResource(Res.plurals.trust_entry_details_vical, size, size)
        }
        is TrustEntryRical -> {
            val size = signedRical!!.rical.certificateInfos.size
            return pluralStringResource(Res.plurals.trust_entry_details_rical, size, size)
        }
    }
}