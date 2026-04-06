package org.multipaz.verifier.server

import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.Deferred
import org.multipaz.server.common.ServerEnvironment
import org.multipaz.verifier.request.cannedRequests
import org.multipaz.verifier.request.verifierGet
import org.multipaz.verifier.request.verifierPost

/**
 * Defines server endpoints for HTTP GET and POST.
 */
fun Application.configureRouting(environment: Deferred<ServerEnvironment>) {
    routing {
        configureVerifier(environment)
        get("/canned_requests") {
            cannedRequests(call)
        }

        // legacy verifier back-end
        get("/verifier/{command}") {
            verifierGet(call, call.parameters["command"]!!)
        }
        post("/verifier/{command}") {
            verifierPost(call, call.parameters["command"]!!)
        }
    }
}

