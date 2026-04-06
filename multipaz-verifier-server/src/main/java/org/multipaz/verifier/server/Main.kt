package org.multipaz.verifier.server

import kotlinx.serialization.json.JsonObject
import org.multipaz.documenttype.DocumentTypeRepository
import org.multipaz.server.common.runServer
import org.multipaz.trustmanagement.TrustManagerInterface
import org.multipaz.util.Logger
import org.multipaz.verifier.customization.VerifierAssistant
import org.multipaz.verifier.customization.VerifierPresentment
import org.multipaz.verifier.request.documentTypeRepo
import org.multipaz.verifier.request.getIssuerTrustManager

/**
 * Main entry point to launch the server.
 *
 * Build and start the server using
 *
 * ```./gradlew multipaz-verifier-server:run```
 */
class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runServer(args, environmentInitializer = {
                add(DocumentTypeRepository::class, documentTypeRepo)
                add(TrustManagerInterface::class, getIssuerTrustManager())
                add(VerifierAssistant::class, object: VerifierAssistant {
                    // accept all transactions for testing
                    override suspend fun processRequest(request: JsonObject): JsonObject? = null

                    override suspend fun processResponse(
                        presentment: VerifierPresentment
                    ): JsonObject? {
                        // no actual processing, just print
                        Logger.i("VerifierAssistant", "Result: ${presentment.response}")
                        return null
                    }
                })
            }) { environment ->
                configureRouting(environment)
            }
        }
    }
}