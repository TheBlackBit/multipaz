package org.multipaz.verifier.request

import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import org.multipaz.documenttype.SingleDocumentCannedRequest
import org.multipaz.documenttype.knowntypes.wellKnownMultipleDocumentRequests

suspend fun cannedRequests(call: ApplicationCall) {
    call.respondText(
        contentType = ContentType.Application.Json,
        text = buildJsonArray {
            for (dt in documentTypeRepo.documentTypes) {
                val jsonRequests = dt.cannedRequests.filter { it.jsonRequest != null }
                if (jsonRequests.isNotEmpty()) {
                    addJsonObject {
                        put("display_name", dt.displayName + " (VC)")
                        putJsonArray("requests") {
                            for (singleDocumentRequest in jsonRequests) {
                                addSingleDocumentRequest(singleDocumentRequest, false)
                            }
                        }
                    }
                }
                // Zero-knowledge is not yet supported
                val mdocRequests = dt.cannedRequests.filter { it.mdocRequest?.useZkp == false }
                if (mdocRequests.isNotEmpty()) {
                    addJsonObject {
                        put("display_name", dt.displayName + " (mdoc)")
                        putJsonArray("requests") {
                            for (singleDocumentRequest in mdocRequests) {
                                addSingleDocumentRequest(singleDocumentRequest, true)
                            }
                        }
                    }
                }
            }
            addJsonObject {
                put("display_name", "Multi-document requests")
                putJsonArray("requests") {
                    for (mdr in wellKnownMultipleDocumentRequests) {
                        addJsonObject {
                            put("display_name", mdr.displayName)
                            put("dcql", Json.parseToJsonElement(mdr.dcqlString))
                            mdr.transactionData?.let {
                                put("transaction_data", Json.parseToJsonElement(it))
                            }
                        }
                    }
                }
            }
        }.toString()
    )
}

fun JsonArrayBuilder.addSingleDocumentRequest(
    singleDocumentRequest: SingleDocumentCannedRequest,
    mdoc: Boolean
) {
    addJsonObject {
        put("display_name", singleDocumentRequest.displayName)
        if (mdoc) {
            put("dcql", singleDocumentRequest.mdocRequest!!.toDcql())
        } else {
            put("dcql", singleDocumentRequest.jsonRequest!!.toDcql())
        }
        if (singleDocumentRequest.transactionData.isNotEmpty()) {
            putJsonArray("transaction_data") {
                for (transactionData in singleDocumentRequest.transactionData) {
                    addJsonObject {
                        put("type", transactionData.transactionType.identifier)
                        putJsonArray("credential_ids") {
                            add("cred1")  // toDcql() above uses this id
                        }
                        for ((name, value) in transactionData.attributes.asMap) {
                            put(name.asTstr, value.toJson())
                        }
                    }
                }
            }
        }
    }
}