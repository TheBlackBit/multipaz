package org.multipaz.utopia.knowntypes

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
    // Approving a mandate hands an agent money to spend later, unattended. That is an
    // authorization, not a disclosure, and the sheet says so.
    grantsStandingAuthorization = true,
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
     * @property shape what the value looks like, which is how a screen decides its prominence.
     * @property detail a secondary line under [value] — the domain under a merchant's name.
     */
    data class SummaryLine(
        val label: String,
        val value: String,
        val shape: Shape = Shape.TEXT,
        val detail: String? = null,
    )

    /**
     * What a value looks like. The renderer walks arbitrary JSON and cannot know which field
     * names matter, so prominence is decided by shape: a constraint type nobody has seen still
     * lands in the right place.
     */
    enum class Shape {
        /** An amount and a currency. The limit being granted. */
        MONEY,

        /** A point in time. */
        INSTANT,

        /** A long unreadable identifier — a digest, a nonce. */
        OPAQUE,

        /** Who the permission empowers. Unreadable like an identifier, but it is the "who". */
        PARTY,

        /** Anything else a person can read. */
        TEXT,
    }

    /**
     * The lines for a whole group of mandates that are signed together.
     *
     * Deduplicated on (label, value): a merchant named by both the checkout and the payment
     * mandate is one fact about the permission, not two, and printing it twice crowds out
     * something the person has not seen.
     *
     * @property money the amounts — the limits being granted.
     * @property rest what a person can read, in the order it arrived.
     * @property opaque digests and nonces: kept, but never given a summary row. They change
     *   nobody's mind, and at full prominence they push the amount off the screen.
     */
    data class GroupSummary(
        val money: List<SummaryLine>,
        val rest: List<SummaryLine>,
        val opaque: List<SummaryLine>,
    ) {
        /** How many lines the group holds in total. */
        val size: Int get() = money.size + rest.size + opaque.size
    }

    /**
     * A spending limit, worded for a person: [label] says which kind of limit it is ("Per
     * purchase, up to", "In total, up to"), because two bare "max" amounts are indistinguishable.
     *
     * @property amount the figure alone, so a screen can set it larger than [currency].
     */
    data class Limit(val label: String, val amount: String, val currency: String)

    /**
     * A group of mandates as a person reads them.
     *
     * @property limits the spending limits, in the order they arrived.
     * @property rows short label/value rows: At, For, Until and Agent first, then every
     *   constraint this wallet does not recognise, under the generic label [summarize] gives it.
     * @property details every line [summarizeGroup] produces, opaque ones included, for the
     *   disclosure. Its size is the true field count.
     */
    data class PermissionSummary(
        val limits: List<Limit>,
        val rows: List<SummaryLine>,
        val details: List<SummaryLine>,
    )

    /**
     * The mandates signed together, described in words.
     *
     * Known AP2 constraints map to plain-language rows; a constraint is only mapped when every
     * member it carries is understood, so nothing is summarised away. Anything else keeps the
     * generic rendering, and [PermissionSummary.details] holds all of it regardless.
     *
     * The merchant ("At") comes only from the mandate, never from the requester: the page asking
     * for the signature is not necessarily the merchant the agent may buy from.
     *
     * The expiry is shown in the device's time zone.
     */
    fun describePermission(payloads: List<Payload>): PermissionSummary =
        describePermission(payloads, TimeZone.currentSystemDefault())

    /** [describePermission] in a given [timeZone]; separate so a test can pin the zone. */
    internal fun describePermission(payloads: List<Payload>, timeZone: TimeZone): PermissionSummary {
        val limits = LinkedHashSet<Limit>()
        val merchants = LinkedHashMap<String, Merchant>()
        val items = LinkedHashSet<String>()
        val until = LinkedHashSet<String>()
        val agents = LinkedHashSet<String>()
        val other = LinkedHashSet<SummaryLine>()
        for (payload in payloads) {
            for ((key, value) in payload.delegatePayload) {
                when {
                    key == "vct" -> {}
                    key == "cnf" -> keyExcerpt(value)?.let { agents += "Key only · $it" }
                    key == "exp" && value is JsonPrimitive && value.longOrNull != null ->
                        until += formatDate(value.long, timeZone)
                    value is JsonArray -> value.forEach { element ->
                        val known = (element as? JsonObject)?.let {
                            describeConstraint(it, limits, merchants, items)
                        } ?: false
                        if (!known) other += lineForElement(key, element)
                    }
                    else -> other += linesFor(key, value)
                }
            }
        }
        val rows = buildList {
            when (merchants.size) {
                0 -> {}
                1 -> merchants.values.single().let { add(SummaryLine("At", it.name, detail = it.domain)) }
                else -> add(SummaryLine("At", merchants.values.joinToString("\n") { m ->
                    m.domain?.let { "${m.name} ($it)" } ?: m.name
                }))
            }
            if (items.isNotEmpty()) add(SummaryLine("For", items.joinToString("\n")))
            if (until.isNotEmpty()) add(SummaryLine("Until", until.joinToString(", "), Shape.INSTANT))
            if (agents.isNotEmpty()) add(SummaryLine("Agent", agents.joinToString(", "), Shape.PARTY))
            addAll(other.filter { it.shape != Shape.OPAQUE })
        }
        val group = summarizeGroup(payloads)
        return PermissionSummary(
            limits = limits.toList(),
            rows = rows,
            details = group.money + group.rest + group.opaque,
        )
    }

    /**
     * Folds one constraint into the readable summary when this wallet understands all of it.
     *
     * @return false when the constraint is not recognised, or carries a member that is not, and
     *   must be rendered generically instead.
     */
    private fun describeConstraint(
        constraint: JsonObject,
        limits: MutableSet<Limit>,
        merchants: MutableMap<String, Merchant>,
        items: MutableSet<String>,
    ): Boolean {
        val type = (constraint["type"] as? JsonPrimitive)?.contentOrNull ?: return false
        fun understands(vararg members: String) = constraint.keys.all { it == "type" || it in members }
        fun minor(member: String) = (constraint[member] as? JsonPrimitive)?.longOrNull
        return when {
            type == "payment.amount_range" && understands("currency", "min", "max") -> {
                val currency = (constraint["currency"] as? JsonPrimitive)?.contentOrNull ?: return false
                val min = minor("min")
                val max = minor("max")
                limits += when {
                    min != null && max != null -> Limit(
                        "Per purchase, between",
                        "${formatMinorAmount(min, currency)} – ${formatMinorAmount(max, currency)}",
                        currency
                    )
                    max != null -> Limit("Per purchase, up to", formatMinorAmount(max, currency), currency)
                    min != null -> Limit("Per purchase, at least", formatMinorAmount(min, currency), currency)
                    else -> return false
                }
                true
            }
            type == "payment.budget" && understands("currency", "max") -> {
                val currency = (constraint["currency"] as? JsonPrimitive)?.contentOrNull ?: return false
                val max = minor("max") ?: return false
                limits += Limit("In total, up to", formatMinorAmount(max, currency), currency)
                true
            }
            type == "checkout.line_items" && understands("items") -> {
                val requirements = (constraint["items"] as? JsonArray)
                    ?.map { (it as? JsonObject)?.let(::describeRequirement) ?: return false }
                    ?: return false
                if (requirements.isEmpty()) return false
                items += requirements
                true
            }
            (type.endsWith("merchants") || type.endsWith("payees")) && understands("allowed") -> {
                val allowed = (constraint["allowed"] as? JsonArray)
                    ?.map { (it as? JsonObject)?.let(::merchantOf) ?: return false }
                    ?: return false
                if (allowed.isEmpty()) return false
                // The checkout and the payment mandate name the same merchant: one fact, once.
                allowed.forEach { merchants.getOrPut(it.key) { it } }
                true
            }
            else -> false
        }
    }

    /**
     * An AP2 merchant (`types/merchant.json`), as a person reads it.
     *
     * @property key what makes two mentions the same merchant.
     * @property domain where it trades, shown under [name]; null when it would only repeat it.
     */
    private data class Merchant(val key: String, val name: String, val domain: String?)

    private val MERCHANT_MEMBERS = setOf("id", "name", "website", "origin")

    private fun merchantOf(obj: JsonObject): Merchant? {
        if (!obj.keys.all { it in MERCHANT_MEMBERS }) return null
        fun member(name: String) = (obj[name] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotEmpty() }
        val id = member("id")
        val name = member("name") ?: id ?: return null
        val domain = (member("website") ?: member("origin"))?.let(::hostOf) ?: id
        return Merchant(key = id ?: name, name = name, domain = domain?.takeIf { it != name })
    }

    /** `https://shop.example.com/path` → `shop.example.com`. */
    private fun hostOf(url: String): String =
        url.substringAfter("://").substringBefore('/').substringBefore('?').substringBefore('#')

    private val REQUIREMENT_MEMBERS = setOf("id", "acceptable_items", "quantity")
    private val ITEM_MEMBERS = setOf("id", "title")

    /**
     * One `checkout.line_items` requirement: `quantity` of any one of `acceptable_items`.
     * Reads "2 × Latte (small) or Latte (large)"; the quantity is left out when it is one.
     *
     * @return null when the requirement carries anything this wallet does not understand.
     */
    private fun describeRequirement(requirement: JsonObject): String? {
        if (!requirement.keys.all { it in REQUIREMENT_MEMBERS }) return null
        val titles = (requirement["acceptable_items"] as? JsonArray)?.map { element ->
            val item = element as? JsonObject ?: return null
            if (!item.keys.all { it in ITEM_MEMBERS }) return null
            ((item["title"] ?: item["id"]) as? JsonPrimitive)?.contentOrNull ?: return null
        }?.distinct()?.takeIf { it.isNotEmpty() } ?: return null
        val quantity = requirement["quantity"]?.let { (it as? JsonPrimitive)?.longOrNull ?: return null }
        val what = titles.joinToString(" or ")
        return if (quantity != null && quantity != 1L) "$quantity × $what" else what
    }

    /** [summarize] over several mandates at once, deduplicated and with the amounts separated. */
    fun summarizeGroup(payloads: List<Payload>): GroupSummary {
        val seen = LinkedHashMap<Pair<String, String>, SummaryLine>()
        for (payload in payloads) {
            for (line in summarize(payload)) {
                val key = line.label to line.value
                if (!seen.containsKey(key)) seen[key] = line
            }
        }
        val lines = seen.values.toList()
        return GroupSummary(
            money = lines.filter { it.shape == Shape.MONEY },
            rest = lines.filter { it.shape != Shape.MONEY && it.shape != Shape.OPAQUE },
            opaque = lines.filter { it.shape == Shape.OPAQUE },
        )
    }

    /**
     * The Mandate Content, rendered for a consent screen. One entry carries one Delegate Payload
     * (§5.1.4), so a request delegating several mandates sends several entries.
     *
     * An empty result means nothing can be shown, and such a mandate must not be signed —
     * [parseOpenId4VpRequest] refuses it.
     */
    fun summarize(payload: Payload): List<SummaryLine> =
        payload.delegatePayload.flatMap { (key, value) -> linesFor(key, value) }

    /** The generic lines for one mandate member. */
    private fun linesFor(key: String, value: JsonElement): List<SummaryLine> = when {
        key == "vct" -> emptyList() // the heading, rendered separately
        key == "cnf" -> listOfNotNull(keyExcerpt(value)?.let { SummaryLine("Authorized agent key", it, Shape.PARTY) })
        key in INSTANT_CLAIMS && value is JsonPrimitive && value.longOrNull != null ->
            listOf(SummaryLine(INSTANT_CLAIMS.getValue(key), formatInstant(value.long), Shape.INSTANT))
        value is JsonArray -> value.map { element -> lineForElement(key, element) }
        else -> renderValue(value).let { listOf(SummaryLine(prettyLabel(key), it, shapeOf(it))) }
    }

    /** The mandate type, for the heading above its lines. Blank when the mandate does not say. */
    fun mandateType(mandate: JsonObject): String =
        (mandate["vct"] as? JsonPrimitive)?.contentOrNull.orEmpty()

    /**
     * A heading for a mandate type, in words.
     *
     * A type identifier is a machine name and means nothing to a person. An unrecognised one is
     * said to be unrecognised rather than guessed at — the raw identifier is still printed, so a
     * wallet never claims to understand a condition it has not seen.
     */
    fun headingFor(mandateType: String): String = when (mandateType) {
        "mandate.checkout.open.1", "mandate.checkout.1" -> "What can be bought"
        "mandate.payment.open.1", "mandate.payment.1" -> "How much can be spent"
        else -> "A condition your wallet doesn't recognise"
    }

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
            val shown = rest.entries
                .filter { (k, v) -> !isEmptyCollection(v) && !(k == "currency" && currency != null) }
            val amounts = shown.associate { (k, v) ->
                k to currency?.let { c -> (v as? JsonPrimitive)?.longOrNull?.let { formatMinorUnits(it, c) } }
            }
            val text = shown
                .joinToString(", ") { (k, v) -> "$k ${amounts[k] ?: renderValue(v)}" }
                .ifEmpty { EMPTY_VALUE }
            return SummaryLine(
                label = prettyLabel(tag),
                value = text,
                shape = if (amounts.values.any { it != null }) Shape.MONEY else shapeOf(text),
            )
        }
        return renderValue(element).let { SummaryLine(prettyLabel(key), it, shapeOf(it)) }
    }

    /**
     * A value is opaque when it carries a long run of identifier characters and no spaces to
     * break it up — a digest, a key, a nonce. Nobody reads one, and at full prominence it pushes
     * the amount off the screen.
     */
    private fun shapeOf(value: String): Shape =
        if (OPAQUE_RUN.containsMatchIn(value)) Shape.OPAQUE else Shape.TEXT

    private val OPAQUE_RUN = Regex("[A-Za-z0-9_-]{20,}")

    /** What an empty list reads as. NOT "any" — an empty allow-list permits nothing. */
    private const val EMPTY_VALUE = "(none)"

    private fun isEmptyCollection(value: JsonElement): Boolean =
        (value is JsonArray && value.isEmpty()) || (value is JsonObject && value.isEmpty())

    /** Minor units to a readable amount. ISO 4217 exponents that are not 2 are listed. */
    private fun formatMinorUnits(minor: Long, currency: String): String =
        "${formatMinorAmount(minor, currency)} $currency"

    /** [formatMinorUnits] without the currency: `25000` USD is `250.00`. */
    private fun formatMinorAmount(minor: Long, currency: String): String {
        val exp = MINOR_UNIT_EXPONENTS[currency.uppercase()] ?: 2
        if (exp == 0) return minor.toString()
        val sign = if (minor < 0) "-" else ""
        val digits = kotlin.math.abs(minor).toString().padStart(exp + 1, '0')
        val whole = digits.dropLast(exp)
        return "$sign$whole.${digits.takeLast(exp)}"
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

    /** `21 September 2027`: a date a person reads at a glance, in their own time zone. */
    private fun formatDate(epochSeconds: Long, timeZone: TimeZone): String {
        val date = Instant.fromEpochSeconds(epochSeconds).toLocalDateTime(timeZone).date
        val month = date.month.name.lowercase().replaceFirstChar { it.titlecase() }
        return "${date.day} $month ${date.year}"
    }

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
