package org.multipaz.documenttype

/**
 * A well-known request for a multiple documents.
 *
 * @property id an identifier for the well-known document request (unique only for the document type).
 * @property displayName a short string with the name of the request, short enough to be used
 *   for a button. For example "Age Over 21 and Portrait" or "Full mDL".
 * @property dcqlString a text string with the DCQL for the request.
 * @property transactionData transaction data list (if any) for this request as JSON array
 */
data class MultiDocumentCannedRequest(
    override val id: String,
    override val displayName: String,
    val dcqlString: String,
    val transactionData: String? = null
): DocumentCannedRequest(id, displayName)
