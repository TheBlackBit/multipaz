package org.multipaz.verifier.server

import io.ktor.server.application.Application
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.Deferred
import org.multipaz.server.common.ServerEnvironment
import org.multipaz.server.common.serveResources
import org.multipaz.server.request.certificateAuthority
import org.multipaz.server.request.push
import org.multipaz.verifier.request.getRequest
import org.multipaz.verifier.request.getResult
import org.multipaz.verifier.request.makeRequest
import org.multipaz.verifier.request.processDirectPost
import org.multipaz.verifier.request.processResponse

/**
 * Defines server endpoints for HTTP GET and POST.
 */
fun Routing.configureVerifier(environment: Deferred<ServerEnvironment>) {
    push(environment)
    certificateAuthority()
    serveResources()
    post("/make_request") {
        makeRequest(call)
    }
    get("/get_request/{sessionId}") {
        getRequest(call, call.parameters["sessionId"]!!)
    }
    post("/direct_post/{sessionId}") {
        processDirectPost(call, call.parameters["sessionId"]!!)
    }
    get("/get_result/{sessionId}") {
        getResult(call, call.parameters["sessionId"]!!)
    }
    post("/process_response") {
        processResponse(call)
    }
}
