package org.multipaz.documenttype.knowntypes

import org.multipaz.documenttype.MultiDocumentCannedRequest
import org.multipaz.doctypes.localization.LocalizedStrings
import org.multipaz.doctypes.localization.GeneratedStringKeys

/**
 * Well-known canned requests that combine multiple document types into a single DCQL request.
 */
val wellKnownMultipleDocumentRequests = listOf(
    MultiDocumentCannedRequest(
        id = "mDL-and-PhotoID",
        displayName = LocalizedStrings.getString(GeneratedStringKeys.MULTI_REQUEST_MDL_AND_PHOTOID),
        dcqlString = """
            {
              "credentials": [
                {
                  "id": "mdl",
                  "format": "mso_mdoc",
                  "meta": { "doctype_value": "org.iso.18013.5.1.mDL" },
                  "claims": [
                    { "path": ["org.iso.18013.5.1", "given_name"] },
                    { "path": ["org.iso.18013.5.1", "family_name"] }
                  ]
                },
                {
                  "id": "photoid",
                  "format": "mso_mdoc",
                  "meta": { "doctype_value": "org.iso.23220.photoid.1" },
                  "claims": [
                    { "path": ["org.iso.23220.1", "given_name"] },
                    { "path": ["org.iso.23220.1", "family_name"] }
                  ]
                }
              ],
              "credential_sets": [
                {
                  "options": [
                    [ "mdl", "photoid" ]
                  ]
                }
              ]
            }
        """.trimIndent()
    ),
    MultiDocumentCannedRequest(
        id = "mDL-or-PhotoID",
        displayName = LocalizedStrings.getString(GeneratedStringKeys.MULTI_REQUEST_MDL_OR_PHOTOID),
        dcqlString = """
            {
              "credentials": [
                {
                  "id": "mdl",
                  "format": "mso_mdoc",
                  "meta": { "doctype_value": "org.iso.18013.5.1.mDL" },
                  "claims": [
                    { "path": ["org.iso.18013.5.1", "given_name"] },
                    { "path": ["org.iso.18013.5.1", "family_name"] }
                  ]
                },
                {
                  "id": "photoid",
                  "format": "mso_mdoc",
                  "meta": { "doctype_value": "org.iso.23220.photoid.1" },
                  "claims": [
                    { "path": ["org.iso.23220.1", "given_name"] },
                    { "path": ["org.iso.23220.1", "family_name"] }
                  ]
                }
              ],
              "credential_sets": [
                {
                  "options": [
                    [ "mdl" ],
                    [ "photoid" ]
                  ]
                }
              ]
            }
        """.trimIndent()
    ),
)
