package org.multipaz.documenttype.knowntypes

import org.multipaz.cbor.toDataItem
import org.multipaz.cbor.toDataItemFullDate
import org.multipaz.documenttype.DocumentAttributeType
import org.multipaz.documenttype.DocumentType
import org.multipaz.documenttype.Icon
import org.multipaz.util.fromBase64Url
import kotlinx.datetime.LocalDate

/**
 * Object containing the metadata of the Aadhaar Document Type.
 * Reference: https://docs.uidai.gov.in/readme/verifiable-credential-specifications/iso-18013-5-aadhaar-mdoc-specs
 */
object Aadhaar {
    /** The ISO mdoc docType used for Aadhaar. */
    const val AADHAAR_DOCTYPE = "in.gov.uidai.aadhaar.1"

    /** The ISO mdoc namespace used for Aadhaar. */
    const val AADHAAR_NAMESPACE = "in.gov.uidai.aadhaar.1"

    /**
     * Build the Aadhaar Document Type.
     */
    fun getDocumentType(): DocumentType {
        return DocumentType.Builder("Aadhaar")
            .addMdocDocumentType(AADHAAR_DOCTYPE)
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "credential_issuing_date",
                "Credential issuing date",
                "Date of credential issuance",
                false,
                AADHAAR_NAMESPACE,
                Icon.CALENDAR_CLOCK,
                LocalDate.parse("2023-01-01").toDataItemFullDate()
            )
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "enrolment_date",
                "Enrollment date",
                "Date of enrollment",
                false,
                AADHAAR_NAMESPACE,
                Icon.TODAY,
                LocalDate.parse("2023-01-01").toDataItemFullDate()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "enrolment_number",
                "Enrollment number",
                "Enrollment number",
                false,
                AADHAAR_NAMESPACE,
                Icon.NUMBERS,
                "1234567890".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "is_nri",
                "Is NRI",
                "Resident is NRI",
                false,
                AADHAAR_NAMESPACE,
                Icon.GLOBE,
                false.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Picture,
                "resident_image",
                "Photo",
                "Photo of the resident",
                false,
                AADHAAR_NAMESPACE,
                Icon.ACCOUNT_BOX,
                SampleData.PORTRAIT_BASE64URL.fromBase64Url().toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "resident_name",
                "Name",
                "Resident name",
                false,
                AADHAAR_NAMESPACE,
                Icon.PERSON,
                SampleData.GIVEN_NAME.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_resident_name",
                "Local name",
                "Resident name in local language",
                false,
                AADHAAR_NAMESPACE,
                Icon.PERSON,
                SampleData.GIVEN_NAME.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_above18",
                "Age above 18",
                "Age above 18",
                false,
                AADHAAR_NAMESPACE,
                Icon.TODAY,
                true.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_above50",
                "Age above 50",
                "Age above 50",
                false,
                AADHAAR_NAMESPACE,
                Icon.TODAY,
                true.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_above60",
                "Age above 60",
                "Age above 60",
                false,
                AADHAAR_NAMESPACE,
                Icon.TODAY,
                true.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_above75",
                "Age above 75",
                "Age above 75",
                false,
                AADHAAR_NAMESPACE,
                Icon.TODAY,
                true.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "dob",
                "Date of birth",
                "Date of birth",
                false,
                AADHAAR_NAMESPACE,
                Icon.TODAY,
                LocalDate.parse("1990-01-01").toDataItemFullDate()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "gender",
                "Gender",
                "Gender",
                false,
                AADHAAR_NAMESPACE,
                Icon.PERSON,
                "M".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "building",
                "Building",
                "Building",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "Building 1".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_building",
                "Local building",
                "Local building",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "Building 1".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "locality",
                "Locality",
                "Locality",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "Locality 1".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_locality",
                "Local locality",
                "Local locality",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "Locality 1".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "street",
                "Street",
                "Street",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "Street 1".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_street",
                "Local street",
                "Local street",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "Street 1".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "landmark",
                "Landmark",
                "Landmark",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "Landmark 1".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_landmark",
                "Local landmark",
                "Local landmark",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "Landmark 1".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "vtc",
                "VTC",
                "Village/town/city",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "VTC".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_vtc",
                "Local VTC",
                "Local village/town/city",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "VTC".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "sub_district",
                "Sub-district",
                "Sub-district",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "Sub-District".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_sub_district",
                "Local Sub-district",
                "Local Sub-district",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "Sub-District".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "district",
                "District",
                "District",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "District".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_district",
                "Local district",
                "Local district",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "District".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "state",
                "State",
                "State",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                SampleData.RESIDENT_STATE.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_state",
                "Local state",
                "Local state",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                SampleData.RESIDENT_STATE.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "po_name",
                "PO name",
                "Post office name",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "PO Name".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_po_name",
                "Local PO name",
                "Local post office name",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                "PO Name".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "pincode",
                "Pincode",
                "Pincode",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                SampleData.RESIDENT_POSTAL_CODE.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "address",
                "Address",
                "Address",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                SampleData.RESIDENT_ADDRESS.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "local_address",
                "Local address",
                "Local address",
                false,
                AADHAAR_NAMESPACE,
                Icon.PLACE,
                SampleData.RESIDENT_ADDRESS.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "mobile",
                "Mobile",
                "Mobile",
                false,
                AADHAAR_NAMESPACE,
                Icon.PHONE,
                "1234567890".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "masked_mobile",
                "Masked mobile",
                "Masked mobile",
                false,
                AADHAAR_NAMESPACE,
                Icon.PHONE,
                "XXXXXX7890".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "email",
                "Email",
                "Email",
                false,
                AADHAAR_NAMESPACE,
                Icon.EMAIL,
                SampleData.EMAIL_ADDRESS.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "masked_email",
                "Masked email",
                "Masked email",
                false,
                AADHAAR_NAMESPACE,
                Icon.EMAIL,
                "a***a@example.com".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "masked_uid",
                "Masked UID",
                "Masked UID",
                false,
                AADHAAR_NAMESPACE,
                Icon.NUMBERS,
                "XXXXXXXX1234".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "aadhaar_type",
                "Type",
                "Type",
                false,
                AADHAAR_NAMESPACE,
                Icon.BADGE,
                "Resident".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "expires_on",
                "Expires on",
                "Expires on",
                false,
                AADHAAR_NAMESPACE,
                Icon.CALENDAR_CLOCK,
                LocalDate.parse(SampleData.EXPIRY_DATE).toDataItemFullDate()
            )
            .addSampleRequest(
                id = "age_over_18",
                displayName = "Age over 18",
                mdocDataElements = mapOf(
                    AADHAAR_NAMESPACE to mapOf(
                        "age_above18" to false,
                    )
                )
            )
            .addSampleRequest(
                id = "age_over_18_zkp",
                displayName ="Age over 18 (ZKP)",
                mdocDataElements = mapOf(
                    AADHAAR_NAMESPACE to mapOf(
                        "age_above18" to false,
                    )
                ),
                mdocUseZkp = true
            )
            .addSampleRequest(
                id = "age_over_18_and_portrait",
                displayName = "Age over 18 + portrait",
                mdocDataElements = mapOf(
                    AADHAAR_NAMESPACE to mapOf(
                        "age_above18" to false,
                        "resident_image" to false,
                    )
                )
            )
            .addSampleRequest(
                id = "full",
                displayName = "All data elements",
                mdocDataElements = mapOf(
                    AADHAAR_NAMESPACE to mapOf()
                )
            )
            .build()
    }
}
