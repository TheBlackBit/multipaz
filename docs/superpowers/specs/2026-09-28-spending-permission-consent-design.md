# Spending permission consent — readable layout

Branch: `feat/consent-authorization-mode`

## Problem

The AP2 delegate block on the consent sheet renders the mandate JSON generically: labels are
raw constraint types with punctuation removed and values are raw members. A person sees
"Payment amount range: max 250.00 USD" next to "Payment budget: max 400.00 USD" and cannot tell
that one is a per-purchase cap and the other a cumulative cap. "Checkout line items: allowed
aurora-headphones" and "2027-09-21 03:08:05Z UTC" read as machine output. SwiftUI lags further:
one block per mandate and a "Share" button.

## Goals

- The limits read as plain-language caps, and are the most prominent thing in the block.
- Known AP2 constraints map to short label/value rows.
- Nothing the signature covers becomes unreachable; unknown constraints still render.
- Compose and SwiftUI render the same wording from one shared Kotlin model.

Non-goals: remembering approved agent keys ("approved before"), changes to parsing, signing or
digest computation.

## 1. Shared model (`multipaz-utopia` `DelegateTransaction`)

`describePermission(payloads: List<Payload>): PermissionSummary` (device time zone; an `internal` overload takes a `TimeZone` for tests)

```
PermissionSummary(
    limits: List<Limit>,          // Limit(label, amount, currency)
    rows: List<SummaryLine>,      // At, For, Until, Agent, then unrecognised conditions
    details: List<SummaryLine>,   // every line from summarizeGroup, for "Show all"
)
```

Mapping (deduplicated across mandates, as today):

| Row | Source | Output |
|---|---|---|
| Limit "Per purchase, up to" | `payment.amount_range` with `max` only | `250.00` / `USD` |
| Limit "Per purchase, between" | `payment.amount_range` with `min` and `max` | `10.00 – 250.00` / `USD` |
| Limit "Per purchase, at least" | `payment.amount_range` with `min` only | `10.00` / `USD` |
| Limit "In total, up to" | `payment.budget.max` | `400.00` / `USD` |
| At | `checkout.allowed_merchants` / `payment.allowed_payees` (any type ending in `merchants` or `payees`): `allowed` AP2 merchants `{id, name, website?, origin?}`, deduplicated by `id`. One merchant: its `name`, with the host of `website`/`origin` (else `id`) as a secondary line. Several: one `name (domain)` per line | `utopia` / `shop.example.com` |
| For | `checkout.line_items.items`: one line per requirement `{id, acceptable_items: [{id, title}], quantity}` — distinct titles joined with " or ", prefixed `N × ` when quantity is not 1 | `aurora-headphones` |
| Until | `exp`, local date in `timeZone`, "d MMMM yyyy" in English | `21 September 2027` |
| Agent | `cnf.jwk` | `Key only · wu7CCOUo…ac733T-o` |
| (other) | any constraint not listed above, via the existing generic path | `label: value` |

The "At" row is taken only from the mandate, never from the requester: the page asking is not
necessarily the merchant. Amounts use the existing ISO 4217 minor-unit formatting. Opaque lines
(digests, nonces) never appear in `rows`, only in `details`. `summarize()` and `summarizeGroup()`
stay as they are; `details` is `summarizeGroup()` flattened, so the "Show all N fields" count is
unchanged.

## 2. Layout (Compose `Consent.kt`, SwiftUI `Consent.swift`)

Order inside the credential container for an authorization request:

0. Header: "Approve this request?" above the requester name, shown smaller and muted.
1. Card, then "Also shared with this site:" (or "this app") and the claims — the requester is
   already named in the header. Card leads again.
2. "Spending permission" section:
   - title "Spending permission";
   - one line: "An agent will be able to spend without asking you again, until these limits
     are reached.";
   - a bordered, rounded box, one row per limit, divider between rows; label on the left
     (body), amount on the right (large, bold) with the currency smaller after it; omitted when
     `limits` is empty;
   - label/value rows: label column muted on the left, value on the right, divider between;
   - outlined full-width "Show all N fields" / "Show less" button, expanding `details` as
     `label: value` lines.
3. Buttons: Cancel / Approve (unchanged in Compose; SwiftUI switches to "Approve" when any
   transaction type sets `grantsStandingAuthorization`).

SwiftUI groups delegate items by type and renders the block once per group, as Compose does.

## 3. Testing

`DelegateTransactionTest` additions:
- amount_range max → "Per purchase, up to"; min+max → "between"; budget → "In total, up to";
- a missing budget yields one limit;
- line items → "For"; merchant constraint → "At"; no merchant → no "At" row;
- `exp` formats as "21 September 2027" in UTC;
- agent row reads "Key only · …";
- an unknown constraint lands in `rows` with its generic label;
- opaque lines appear only in `details`, and `details.size == summarizeGroup(...).size`.

Verification: `./gradlew :multipaz-utopia:jvmTest :multipaz-compose:assemble detekt` and
`xcodebuild -project samples/SwiftTestApp/SwiftTestApp.xcodeproj -scheme SwiftTestApp -sdk iphonesimulator build`.
