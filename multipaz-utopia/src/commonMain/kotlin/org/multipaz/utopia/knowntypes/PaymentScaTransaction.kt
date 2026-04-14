package org.multipaz.utopia.knowntypes

import org.multipaz.cbor.DataItem
import org.multipaz.cbor.toDataItem
import org.multipaz.credential.Credential
import org.multipaz.documenttype.DocumentAttribute
import org.multipaz.documenttype.DocumentAttributeType
import org.multipaz.documenttype.MdocDataElement
import org.multipaz.documenttype.TransactionType
import org.multipaz.mdoc.credential.MdocCredential
import org.multipaz.presentment.TransactionData

/**
 * Transaction type for Payment SCA (Strong Customer Authentication) flows.
 *
 * This transaction type is used when a verifier requests payment authorization
 * alongside a [DigitalPaymentCredential] presentation.
 */
object PaymentScaTransaction : TransactionType(
    displayName = "Payment SCA",
    identifier = "payment_sca",
    attributes = listOf(
        MdocDataElement(
            attribute = DocumentAttribute(
                identifier = "amount",
                type = DocumentAttributeType.String,
                displayName = "Amount",
                description = "Payment amount"
            ),
            mandatory = false
        ),
        MdocDataElement(
            attribute = DocumentAttribute(
                identifier = "currency",
                type = DocumentAttributeType.String,
                displayName = "Currency",
                description = "Currency code (e.g. USD, EUR)"
            ),
            mandatory = false
        ),
        MdocDataElement(
            attribute = DocumentAttribute(
                identifier = "merchant",
                type = DocumentAttributeType.String,
                displayName = "Merchant",
                description = "Merchant name or identifier"
            ),
            mandatory = false
        ),
    )
) {
    override suspend fun isApplicable(
        transactionData: TransactionData,
        credential: Credential
    ): Boolean {
        return credential is MdocCredential &&
                credential.docType == DigitalPaymentCredential.CARD_DOCTYPE
    }

    override suspend fun applyCbor(
        transactionData: TransactionData,
        credential: Credential
    ): Map<String, DataItem>? {
        val result = buildMap {
            transactionData.getString("amount")?.let { put("amount", it.toDataItem()) }
            transactionData.getString("currency")?.let { put("currency", it.toDataItem()) }
            transactionData.getString("merchant")?.let { put("merchant", it.toDataItem()) }
        }
        return result.ifEmpty { null }
    }
}
