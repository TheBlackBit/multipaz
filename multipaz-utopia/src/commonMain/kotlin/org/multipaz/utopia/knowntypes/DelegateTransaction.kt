package org.multipaz.utopia.knowntypes

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import kotlin.time.Instant
import org.multipaz.credential.Credential
import org.multipaz.crypto.Algorithm
import org.multipaz.crypto.Crypto
import org.multipaz.documenttype.TransactionType
import org.multipaz.documenttype.TransactionUserInput
import org.multipaz.presentment.TransactionData
import org.multipaz.util.fromBase64Url
import org.multipaz.util.toBase64Url
import org.multipaz.sdjwt.credential.KeyBoundSdJwtVcCredential

/**
 * Mandate delegation, as defined by the
 * [Agent Payments Protocol](https://github.com/google-agentic-commerce/AP2)
 * (`docs/ap2/agent_authorization.md`, "Delegation using OpenID4VP").
 *
 * A user authorizes an agent to act later, when they are no longer present — "this assistant may
 * buy groceries, up to $50 a shop, until December". The verifier puts that Mandate Content in a
 * `transaction_data` entry of type `delegate`, and the wallet returns it bound into the Key
 * Binding JWT; the user's key binding is the authorization.
 *
 * The wire shape follows
 * [Delegate SD-JWT](https://datatracker.ietf.org/doc/html/draft-gco-oauth-delegate-sd-jwt), the
 * specification AP2 points at, rather than the shape AP2's own examples illustrate. The two
 * disagree: AP2 sends the mandate in the clear as `delegate_payload`, while §7.1 sends an array
 * disclosure in `delegate_payload_disclosure` and puts only its digest in the KB-JWT. The draft is
 * what a verifier written to the standard will read.
 *
 * AP2 requires the Mandate Content to be shown to the user on a Trusted Surface before it is
 * signed, and the wallet is that surface. [summarize] renders it, generically: it walks whatever
 * JSON the mandate carries rather than knowing AP2's mandate types.
 */
object DelegateTransaction : TransactionType<DelegateTransaction.Payload>(
    displayName = "Delegate authorization",
    identifier = "delegate",
    // Delegate SD-JWT §5.1.4 fixes both: `delegate_payload` is an array at the KB-JWT top level,
    // and the key binding has its own media type. Nesting would make it unreadable to a verifier.
    nestSdJwtResponseClaims = false,
    sdJwtKbType = "kb+sd-jwt",
) {
    /**
     * The KB-JWT claim carrying the digest of the Mandate Content (Delegate SD-JWT §7.1).
     *
     * No leading underscore: the draft's source writes it in Markdown italics around an escaped
     * underscore, which datatracker renders as `_delegate_payload_`. §7.1's prose spells it plainly.
     */
    const val DELEGATE_PAYLOAD_CLAIM = "delegate_payload"

    /** The `typ` a Delegate Key Binding JWT carries (Delegate SD-JWT §5.1.4). */
    const val DELEGATE_KB_TYPE = "kb+sd-jwt"

    /**
     * @property format the delegation format, `dSD-JWT` or `dSD-JWT+KB` (Delegate SD-JWT §7.1).
     * @property delegatePayloadDisclosure the Array Disclosure exactly as it arrived; the KB-JWT
     *   digest is computed over these bytes, so re-encoding it would change what was signed.
     * @property delegatePayload the Mandate Content that disclosure carries, decoded for display.
     * @property delegateDisclosures optional selective disclosures within [delegatePayload].
     */
    data class Payload(
        val format: String,
        val delegatePayloadDisclosure: String,
        val delegatePayload: JsonObject,
        val delegateDisclosures: List<JsonElement>? = null,
    )

    /**
     * One readable line on the consent screen.
     *
     * @property label what the line is — a mandate member or constraint, in words.
     * @property value what it says, formatted for a person (money, dates, lists).
     */
    data class SummaryLine(val label: String, val value: String)

    /**
     * The Mandate Content, rendered for a consent screen. One entry carries one Delegate Payload
     * (§5.1.4), so a request delegating several mandates sends several entries.
     *
     * An empty result means nothing can be shown, and such a mandate must not be signed —
     * [parseOpenId4VpRequest] refuses it.
     */
    fun summarize(payload: Payload): List<SummaryLine> =
        buildList {
            for ((key, value) in payload.delegatePayload) {
                when {
                    key == "vct" -> {} // the heading, rendered separately
                    key == "cnf" -> keyExcerpt(value)?.let { add(SummaryLine("Authorized agent key", it)) }
                    key in INSTANT_CLAIMS && value is JsonPrimitive && value.longOrNull != null ->
                        add(SummaryLine(INSTANT_CLAIMS.getValue(key), formatInstant(value.long)))
                    value is JsonArray -> value.forEach { element -> add(lineForElement(key, element)) }
                    else -> add(SummaryLine(prettyLabel(key), renderValue(value)))
                }
            }
        }

    /** The mandate type, for the heading above its lines. Blank when the mandate does not say. */
    fun mandateType(mandate: JsonObject): String =
        (mandate["vct"] as? JsonPrimitive)?.contentOrNull.orEmpty()

    /**
     * The Mandate Content carried by an Array Disclosure — `base64url(JSON([salt, value]))`,
     * RFC 9901 §4.2.4.2.
     *
     * @throws IllegalArgumentException if the disclosure is not a two-element array of a JSON object.
     */
    private fun mandateFromDisclosure(disclosure: String): JsonObject {
        val decoded = try {
            json.parseToJsonElement(disclosure.fromBase64Url().decodeToString())
        } catch (err: Exception) {
            throw IllegalArgumentException("'delegate_payload_disclosure' is not a base64url-encoded JSON array", err)
        }
        val array = decoded as? JsonArray
            ?: throw IllegalArgumentException("'delegate_payload_disclosure' does not decode to a JSON array")
        // [salt, value] is the array-element form; [salt, name, value] is the object-property one.
        if (array.size != 2) {
            throw IllegalArgumentException(
                "'delegate_payload_disclosure' has ${array.size} elements — an array disclosure is [salt, value]"
            )
        }
        return array[1] as? JsonObject
            ?: throw IllegalArgumentException("'delegate_payload_disclosure' does not disclose a JSON object")
    }

    private val INSTANT_CLAIMS = mapOf(
        "exp" to "Valid until",
        "nbf" to "Valid from",
        "iat" to "Issued",
    )

    /** An array element. An object carrying a `type` reads better with that as its label. */
    private fun lineForElement(key: String, element: JsonElement): SummaryLine {
        val tag = (element as? JsonObject)?.get("type")?.let { (it as? JsonPrimitive)?.contentOrNull }
        if (tag != null) {
            val rest = element.jsonObject.filterKeys { it != "type" }
            // A `currency` member means the amounts are ISO 4217 minor units: "13000" is $130.00,
            // and this is the screen where the person has to read the limit correctly.
            val currency = (rest["currency"] as? JsonPrimitive)?.contentOrNull
            return SummaryLine(
                prettyLabel(tag),
                rest.entries
                    .filter { (k, v) -> !isEmptyCollection(v) && !(k == "currency" && currency != null) }
                    .joinToString(", ") { (k, v) ->
                        val amount = currency?.let { c -> (v as? JsonPrimitive)?.longOrNull?.let { formatMinorUnits(it, c) } }
                        if (amount != null) "$k $amount" else "$k ${renderValue(v)}"
                    }
                    .ifEmpty { EMPTY_VALUE },
            )
        }
        return SummaryLine(prettyLabel(key), renderValue(element))
    }

    /** What an empty list reads as. NOT "any" — an empty allow-list permits nothing. */
    private const val EMPTY_VALUE = "(none)"

    private fun isEmptyCollection(value: JsonElement): Boolean =
        (value is JsonArray && value.isEmpty()) || (value is JsonObject && value.isEmpty())

    /** Minor units to a readable amount. ISO 4217 exponents that are not 2 are listed. */
    private fun formatMinorUnits(minor: Long, currency: String): String {
        val exp = MINOR_UNIT_EXPONENTS[currency.uppercase()] ?: 2
        if (exp == 0) return "$minor $currency"
        val sign = if (minor < 0) "-" else ""
        val digits = kotlin.math.abs(minor).toString().padStart(exp + 1, '0')
        val whole = digits.dropLast(exp)
        return "$sign$whole.${digits.takeLast(exp)} $currency"
    }

    private val MINOR_UNIT_EXPONENTS = mapOf(
        "BIF" to 0, "CLP" to 0, "DJF" to 0, "GNF" to 0, "ISK" to 0, "JPY" to 0, "KMF" to 0,
        "KRW" to 0, "PYG" to 0, "RWF" to 0, "UGX" to 0, "UYI" to 0, "VND" to 0, "VUV" to 0,
        "XAF" to 0, "XOF" to 0, "XPF" to 0,
        "BHD" to 3, "IQD" to 3, "JOD" to 3, "KWD" to 3, "LYD" to 3, "OMR" to 3, "TND" to 3,
    )

    /** `checkout.allowed_merchants` → `Checkout allowed merchants`. Punctuation only. */
    private fun prettyLabel(raw: String): String =
        raw.replace('_', ' ').replace('.', ' ').trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    private fun renderValue(value: JsonElement): String = when (value) {
        is JsonPrimitive -> value.contentOrNull ?: value.toString()
        is JsonArray -> value.joinToString(", ") { renderValue(it) }.ifEmpty { EMPTY_VALUE }
        is JsonObject -> value.entries.joinToString(", ") { (k, v) -> "$k ${renderValue(v)}" }.ifEmpty { EMPTY_VALUE }
    }

    /**
     * A short, stable excerpt of the `cnf` key (RFC 7800). Not an RFC 7638 thumbprint: that needs
     * a hash, and `Crypto.digest` suspends while this renders in a composable. An excerpt still
     * lets a person tell one agent from another.
     */
    private fun keyExcerpt(cnf: JsonElement): String? {
        val jwk = (cnf as? JsonObject)?.get("jwk") as? JsonObject ?: return null
        val x = (jwk["x"] as? JsonPrimitive)?.contentOrNull ?: return null
        if (x.length <= 16) return x
        return "${x.take(8)}…${x.takeLast(8)}"
    }

    private fun formatInstant(epochSeconds: Long): String =
        Instant.fromEpochSeconds(epochSeconds).toString().substringBefore('.').replace('T', ' ') + " UTC"

    private val json = Json { ignoreUnknownKeys = true }

    override fun serializeOpenId4VpRequest(
        payload: Payload,
        credentialIds: List<String>,
        hashAlgorithms: List<Algorithm>?
    ): String {
        val obj = buildMap<String, JsonElement> {
            put("type", JsonPrimitive(identifier))
            put("format", JsonPrimitive(payload.format))
            put("credential_ids", JsonArray(credentialIds.map { JsonPrimitive(it) }))
            joseHashAlgorithms(hashAlgorithms)?.let {
                put("transaction_data_hashes_alg", JsonArray(it.map { alg -> JsonPrimitive(alg) }))
            }
            put("delegate_payload_disclosure", JsonPrimitive(payload.delegatePayloadDisclosure))
            payload.delegateDisclosures?.let { put("delegate_disclosures", JsonArray(it)) }
        }
        return json.encodeToString(JsonObject.serializer(), JsonObject(obj))
    }

    /**
     * @throws IllegalArgumentException if a required member is missing, or the mandate has nothing
     *   that can be shown to the user.
     */
    override fun parseOpenId4VpRequest(jsonString: String): Payload {
        val obj = json.parseToJsonElement(jsonString).jsonObject
        // REQUIRED by §7.1. A request carrying AP2's plain `delegate_payload` lands here and is
        // refused, rather than signed into a key binding that conforms to neither document.
        val disclosure = (obj["delegate_payload_disclosure"]
            ?: throw IllegalArgumentException("Missing 'delegate_payload_disclosure' in delegate transaction data"))
            .jsonPrimitive.content
        if (disclosure.isEmpty()) {
            throw IllegalArgumentException("'delegate_payload_disclosure' is empty — nothing to authorize")
        }
        val format = (obj["format"]
            ?: throw IllegalArgumentException("Missing 'format' in delegate transaction data"))
            .jsonPrimitive.content
        val payload = Payload(
            format = format,
            delegatePayloadDisclosure = disclosure,
            delegatePayload = mandateFromDisclosure(disclosure),
            delegateDisclosures = obj["delegate_disclosures"]?.jsonArray?.toList(),
        )
        // A mandate the consent screen cannot show must not be signable: the signature is the
        // authorization, so it cannot cover terms the person was never shown.
        if (summarize(payload).isEmpty()) {
            throw IllegalArgumentException(
                "the mandate in 'delegate_payload_disclosure' has nothing that can be shown to the user — refusing to request a signature over terms they cannot read"
            )
        }
        return payload
    }

    /** Delegation is the holder's signature over the mandate, so only a key-bound SD-JWT VC can
     *  carry it; Delegate SD-JWT is SD-JWT syntax and has no mdoc equivalent. */
    override suspend fun isApplicable(
        transactionData: TransactionData<Payload>,
        credential: Credential
    ): Boolean {
        if (credential !is KeyBoundSdJwtVcCredential) return false
        return super.isApplicable(transactionData, credential)
    }

    /** Puts the digest of the Mandate Content into the KB-JWT (Delegate SD-JWT §7.1). */
    override suspend fun generateSdJwtResponseClaims(
        transactionData: TransactionData<Payload>,
        credential: Credential,
        userInput: TransactionUserInput?,
        docRequestId: Int?
    ): Map<String, JsonElement> = buildMap {
        putAll(super.generateSdJwtResponseClaims(transactionData, credential, userInput, docRequestId))
        put(DELEGATE_PAYLOAD_CLAIM, delegatePayloadClaim(transactionData))
    }

    /**
     * The `delegate_payload` claim for one transaction data item: the digest of its disclosure, in
     * RFC 9901's replaced-array-element form, under the algorithm the verifier asked for.
     *
     * The digest covers the disclosure's own bytes, never a re-encoding of the JSON inside it.
     * Separate from [generateSdJwtResponseClaims] so it can be exercised without a credential.
     */
    suspend fun delegatePayloadClaim(transactionData: TransactionData<Payload>): JsonArray {
        val algorithm = transactionData.hashAlgorithms?.firstOrNull() ?: Algorithm.SHA256
        val digest = Crypto.digest(
            algorithm,
            transactionData.payload.delegatePayloadDisclosure.encodeToByteArray()
        ).toBase64Url()
        return JsonArray(listOf(JsonObject(mapOf(ARRAY_ELEMENT_DIGEST_KEY to JsonPrimitive(digest)))))
    }

    /** RFC 9901 §4.2.4.2: an array element replaced by a disclosure is `{"...": "<digest>"}`. */
    private const val ARRAY_ELEMENT_DIGEST_KEY = "..."
}
