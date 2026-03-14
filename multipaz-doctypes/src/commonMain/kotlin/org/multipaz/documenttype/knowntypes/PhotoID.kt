package org.multipaz.documenttype.knowntypes

import org.multipaz.cbor.toDataItem
import org.multipaz.cbor.toDataItemFullDate
import org.multipaz.documenttype.DocumentAttributeType
import org.multipaz.documenttype.DocumentType
import org.multipaz.documenttype.Icon
import org.multipaz.util.fromBase64Url
import kotlinx.datetime.LocalDate
import org.multipaz.cbor.buildCborMap
import org.multipaz.documenttype.knowntypes.DrivingLicense.MDL_NAMESPACE
import org.multipaz.doctypes.localization.LocalizedStrings
import org.multipaz.doctypes.localization.GeneratedStringKeys

/**
 * PhotoID according to ISO/IEC 23220-4 Annex C.
 *
 * (This is based on ISO/IEC JTC 1/SC 17/WG 4 N 4862 from 2025-12-04)
 */
object PhotoID {
    const val PHOTO_ID_DOCTYPE = "org.iso.23220.photoid.1"
    const val ISO_23220_2_NAMESPACE = "org.iso.23220.1"
    const val PHOTO_ID_NAMESPACE = "org.iso.23220.photoid.1"
    const val DTC_NAMESPACE = "org.iso.23220.dtc.1"

    /**
     * Build the PhotoID Document Type.
     */
    fun getDocumentType(locale: String = LocalizedStrings.getCurrentLocale()): DocumentType {
        fun getLocalizedString(key: String) = LocalizedStrings.getString(key, locale)

        return with(DocumentType.Builder(getLocalizedString(GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_PHOTO_ID))) {
        addMdocDocumentType(PHOTO_ID_DOCTYPE)

        // Data elements from ISO/IEC 23220-4 Table C.1 — PhotoID data elements defined by ISO/IEC TS 23220-2
        //
        addMdocAttribute(
            DocumentAttributeType.String,
            "family_name",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_FAMILY_NAME),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_FAMILY_NAME),
            true,
            ISO_23220_2_NAMESPACE,
            Icon.PERSON,
            SampleData.FAMILY_NAME.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "family_name_viz",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_FAMILY_NAME_VIZ),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_FAMILY_NAME_VIZ),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PERSON,
            SampleData.FAMILY_NAME.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "given_name",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_GIVEN_NAMES),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_GIVEN_NAMES),
            true,
            ISO_23220_2_NAMESPACE,
            Icon.PERSON,
            SampleData.GIVEN_NAME.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "given_name_viz",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_GIVEN_NAME_VIZ),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_GIVEN_NAME_VIZ),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PERSON,
            SampleData.GIVEN_NAME.toDataItem()
        )
        // Note, this is more complicated than mDL and EU PID, according to ISO/IEC 23220-2
        // clause "6.3.1.1.3 Date of birth as either uncertain or approximate, or both"
        //
        // If date of birth includes an unknown part, the following birth_date structure may be used.
        // birth date = {
        //   "birth_date" : full-date,
        //   ? "approximate_mask": tstr
        // }
        // Approximate_mask is an 8 digit flag to denote the location of the mask in YYYYMMDD
        // format. 1 denotes mask.
        //
        // NOTE "approximate mask" is not intended to be used for calculation.
        //
        addMdocAttribute(
            DocumentAttributeType.Date,   // TODO: this is a more complex type
            "birth_date",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_DATE_OF_BIRTH),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_DATE_OF_BIRTH),
            true,
            ISO_23220_2_NAMESPACE,
            Icon.TODAY,
            buildCborMap {
                put("birth_date", LocalDate.parse(SampleData.BIRTH_DATE).toDataItemFullDate())
            }
        )
        addMdocAttribute(
            DocumentAttributeType.Picture,
            "portrait",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_PHOTO_OF_HOLDER),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_PHOTO_OF_HOLDER),
            true,
            ISO_23220_2_NAMESPACE,
            Icon.ACCOUNT_BOX,
            SampleData.PORTRAIT_BASE64URL.fromBase64Url().toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.Date,
            "issue_date",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_DATE_OF_ISSUE),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_DATE_OF_ISSUE),
            true,
            ISO_23220_2_NAMESPACE,
            Icon.DATE_RANGE,
            LocalDate.parse(SampleData.ISSUE_DATE).toDataItemFullDate()
        )
        addMdocAttribute(
            DocumentAttributeType.Date,
            "expiry_date",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_DATE_OF_EXPIRY),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_DATE_OF_EXPIRY),
            true,
            ISO_23220_2_NAMESPACE,
            Icon.CALENDAR_CLOCK,
            LocalDate.parse(SampleData.EXPIRY_DATE).toDataItemFullDate()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "issuing_authority_unicode",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_ISSUING_AUTHORITY),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_ISSUING_AUTHORITY),
            true,
            ISO_23220_2_NAMESPACE,
            Icon.ACCOUNT_BALANCE,
            SampleData.ISSUING_AUTHORITY_PHOTO_ID.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.StringOptions(Options.COUNTRY_ISO_3166_1_ALPHA_2),
            "issuing_country",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_ISSUING_COUNTRY),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_ISSUING_COUNTRY),
            true,
            ISO_23220_2_NAMESPACE,
            Icon.ACCOUNT_BALANCE,
            SampleData.ISSUING_COUNTRY.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.Number,
            "age_in_years",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_AGE_IN_YEARS),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_AGE_IN_YEARS),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.TODAY,
            SampleData.AGE_IN_YEARS.toDataItem()
        )
        // If we provision all 99 age_over_NN claims the MSO will be 3886 bytes which exceeds the Longfellow-ZK
        // MSO size limit of ~ 2200 bytes. With these 13 claims, the MSO is 764 bytes which is more manageable.
        val ageThresholdsToProvision = listOf(13, 15, 16, 18, 21, 23, 25, 27, 28, 40, 60, 65, 67)
        for (age in IntRange(1, 99)) {
            addMdocAttribute(
                type = DocumentAttributeType.Boolean,
                identifier = "age_over_${if (age < 10) "0$age" else "$age"}",
                displayName = "Older than $age years",
                description = "Indication whether the document holder is as old or older than $age",
                mandatory = (age == 18),
                mdocNamespace = ISO_23220_2_NAMESPACE,
                icon = Icon.TODAY,
                sampleValue = if (age in ageThresholdsToProvision) {
                    (SampleData.AGE_IN_YEARS >= age).toDataItem()
                } else {
                    null
                }
            )
        }
        addMdocAttribute(
            DocumentAttributeType.Number,
            "age_birth_year",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_YEAR_OF_BIRTH),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_YEAR_OF_BIRTH),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.TODAY,
            SampleData.AGE_BIRTH_YEAR.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.Date,
            "portrait_capture_date",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_PORTRAIT_CAPTURE_DATE),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_PORTRAIT_CAPTURE_DATE),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.TODAY,
            LocalDate.parse(SampleData.PORTRAIT_CAPTURE_DATE).toDataItemFullDate()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "birthplace",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_PLACE_OF_BIRTH),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_PLACE_OF_BIRTH),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PLACE,
            SampleData.BIRTH_PLACE.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "name_at_birth",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_NAME_AT_BIRTH),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_NAME_AT_BIRTH),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PERSON,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "resident_address",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_RESIDENT_ADDRESS),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_RESIDENT_ADDRESS),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PLACE,
            SampleData.RESIDENT_ADDRESS.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "resident_city",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_RESIDENT_CITY),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_RESIDENT_CITY),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PLACE,
            SampleData.RESIDENT_CITY.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "resident_postal_code",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_RESIDENT_POSTAL_CODE),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_RESIDENT_POSTAL_CODE),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PLACE,
            SampleData.RESIDENT_POSTAL_CODE.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.StringOptions(Options.COUNTRY_ISO_3166_1_ALPHA_2),
            "resident_country",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_RESIDENT_COUNTRY),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_RESIDENT_COUNTRY),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PLACE,
            SampleData.RESIDENT_COUNTRY.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "resident_city_latin1",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_RESIDENT_CITY_LATIN1),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_RESIDENT_CITY_LATIN1),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PLACE,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.IntegerOptions(Options.SEX_ISO_IEC_5218),
            "sex",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_SEX),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_SEX),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.EMERGENCY,
            SampleData.SEX_ISO_5218.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.StringOptions(Options.COUNTRY_ISO_3166_1_ALPHA_2),
            "nationality",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_NATIONALITY),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_NATIONALITY),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.LANGUAGE,
            SampleData.NATIONALITY.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "document_number",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_DOCUMENT_NUMBER),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_DOCUMENT_NUMBER),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.NUMBERS,
            SampleData.DOCUMENT_NUMBER.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "issuing_subdivision",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_ISSUING_SUBDIVISION),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_ISSUING_SUBDIVISION),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.ACCOUNT_BALANCE,
            SampleData.ISSUING_JURISDICTION.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "family_name_latin1",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_FAMILY_NAME_LATIN1),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_FAMILY_NAME_LATIN1),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PERSON,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "given_name_latin1",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_GIVEN_NAMES_LATIN1),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_GIVEN_NAMES_LATIN1),
            false,
            ISO_23220_2_NAMESPACE,
            Icon.PERSON,
            null
        )

        // Data elements from ISO/IEC 23220-4 Table C.2 — Data elements specifically defined for PhotoID
        //
        addMdocAttribute(
            DocumentAttributeType.String,
            "person_id",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_PERSON_ID),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_PERSON_ID),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.NUMBERS,
            SampleData.PERSON_ID.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.StringOptions(Options.COUNTRY_ISO_3166_1_ALPHA_2),
            "birth_country",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_BIRTH_COUNTRY),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_BIRTH_COUNTRY),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.PLACE,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "birth_state",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_BIRTH_STATE),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_BIRTH_STATE),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.PLACE,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "birth_city",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_BIRTH_CITY),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_BIRTH_CITY),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.PLACE,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "administrative_number",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_ADMINISTRATIVE_NUMBER),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_ADMINISTRATIVE_NUMBER),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.NUMBERS,
            SampleData.ADMINISTRATIVE_NUMBER.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "resident_street",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_RESIDENT_STREET),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_RESIDENT_STREET),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.PLACE,
            SampleData.RESIDENT_STREET.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "resident_house_number",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_RESIDENT_HOUSE_NUMBER),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_RESIDENT_HOUSE_NUMBER),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.PLACE,
            SampleData.RESIDENT_HOUSE_NUMBER.toDataItem()
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "travel_document_type",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_TRAVEL_DOCUMENT_TYPE),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_TRAVEL_DOCUMENT_TYPE),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "travel_document_number",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_TRAVEL_DOCUMENT_NUMBER),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_TRAVEL_DOCUMENT_NUMBER),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "travel_document_mrz",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_TRAVEL_DOCUMENT_MRZ),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_TRAVEL_DOCUMENT_MRZ),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.String,
            "resident_state",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_RESIDENT_STATE),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_RESIDENT_STATE),
            false,
            PHOTO_ID_NAMESPACE,
            Icon.PLACE,
            SampleData.RESIDENT_STATE.toDataItem()
        )


        // Data elements from ISO/IEC 23220-4 Table C.3 — Data elements defined by ICAO 9303 part 10
        //
        addMdocAttribute(
            DocumentAttributeType.String,
            "version",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_DTC_VC_VERSION),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_DTC_VC_VERSION),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "sod",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_SOD),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_SOD),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg1",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG1),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG1),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg2",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG2),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG2),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg3",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG3),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG3),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg4",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG4),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG4),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg5",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG5),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG5),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg6",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG6),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG6),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg7",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG7),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG7),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg8",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG8),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG8),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg9",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG9),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG9),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg10",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG10),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG10),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg11",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG11),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG11),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg12",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG12),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG12),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg13",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG13),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG13),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg14",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG14),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG14),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg15",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG15),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG15),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )
        addMdocAttribute(
            DocumentAttributeType.Blob,
            "dg16",
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_ATTRIBUTE_EMRTD_DG16),
            getLocalizedString(GeneratedStringKeys.PHOTO_ID_DESCRIPTION_EMRTD_DG16),
            false,
            DTC_NAMESPACE,
            Icon.NUMBERS,
            null
        )

        // Finally for the sample requests.
        //
        addSampleRequest(
            id = "age_over_18",
            displayName = getLocalizedString(GeneratedStringKeys.PHOTO_ID_REQUEST_AGE_OVER_18),
            mdocDataElements = mapOf(
                ISO_23220_2_NAMESPACE to mapOf(
                    "age_over_18" to false,
                )
            ),
        )
        addSampleRequest(
            id = "age_over_18_zkp",
            displayName = getLocalizedString(GeneratedStringKeys.PHOTO_ID_REQUEST_AGE_OVER_18_ZKP),
            mdocDataElements = mapOf(
                ISO_23220_2_NAMESPACE to mapOf(
                    "age_over_18" to false,
                )
            ),
            mdocUseZkp = true
        )
        addSampleRequest(
            id = "age_over_18_and_portrait",
            displayName = getLocalizedString(GeneratedStringKeys.PHOTO_ID_REQUEST_AGE_OVER_18_AND_PORTRAIT),
            mdocDataElements = mapOf(
                ISO_23220_2_NAMESPACE to mapOf(
                    "age_over_18" to false,
                    "portrait" to false
                )
            ),
        )
        addSampleRequest(
            id = "mandatory",
            displayName = getLocalizedString(GeneratedStringKeys.PHOTO_ID_REQUEST_MANDATORY_DATA_ELEMENTS),
            mdocDataElements = mapOf(
                ISO_23220_2_NAMESPACE to mapOf(
                    "family_name" to false,
                    "given_name" to false,
                    "birth_date" to false,
                    "portrait" to false,
                    "issue_date" to false,
                    "expiry_date" to false,
                    "issuing_authority_unicode" to false,
                    "issuing_country" to false,
                    "age_over_18" to false,
                )
            )
        )
        addSampleRequest(
            id = "full",
            displayName = getLocalizedString(GeneratedStringKeys.PHOTO_ID_REQUEST_ALL_DATA_ELEMENTS),
            mdocDataElements = mapOf(
                ISO_23220_2_NAMESPACE to mapOf(),
                PHOTO_ID_NAMESPACE to mapOf(),
                DTC_NAMESPACE to mapOf()
            )
        )
        }.build()
    }
}
