/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.multipaz.documenttype.knowntypes

import kotlinx.datetime.LocalDate
import org.multipaz.cbor.Simple
import org.multipaz.cbor.Tagged
import org.multipaz.cbor.Tstr
import org.multipaz.cbor.addCborMap
import org.multipaz.cbor.buildCborArray
import org.multipaz.cbor.toDataItem
import org.multipaz.cbor.toDataItemFullDate
import org.multipaz.doctypes.localization.LocalizedStrings
import org.multipaz.doctypes.localization.GeneratedStringKeys
import org.multipaz.documenttype.DocumentAttributeType
import org.multipaz.documenttype.DocumentType
import org.multipaz.documenttype.Icon
import org.multipaz.documenttype.IntegerOption
import org.multipaz.documenttype.StringOption
import org.multipaz.util.fromBase64Url

/**
 * Object containing the metadata of the Driving License
 * Document Type.
 */
object DrivingLicense {
    const val MDL_DOCTYPE = "org.iso.18013.5.1.mDL"
    const val MDL_NAMESPACE = "org.iso.18013.5.1"
    const val AAMVA_NAMESPACE = "org.iso.18013.5.1.aamva"

    /**
     * Build the Driving License Document Type. This is ISO mdoc only.
     */
    fun getDocumentType(locale: String = LocalizedStrings.getCurrentLocale()): DocumentType {
        fun getLocalizedString(key: String) = LocalizedStrings.getString(key, locale)

        return DocumentType.Builder(getLocalizedString(GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_DRIVING_LICENSE))
            .addMdocDocumentType(MDL_DOCTYPE)
            /*
             * First the attributes that the mDL and VC Credential Type have in common
             */
            .addMdocAttribute(
                DocumentAttributeType.String,
                "family_name",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_FAMILY_NAME),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_FAMILY_NAME),
                true,
                MDL_NAMESPACE,
                Icon.PERSON,
                SampleData.FAMILY_NAME.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "given_name",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_GIVEN_NAMES),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_GIVEN_NAMES),
                true,
                MDL_NAMESPACE,
                Icon.PERSON,
                SampleData.GIVEN_NAME.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "birth_date",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_DATE_OF_BIRTH),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_DATE_OF_BIRTH),
                true,
                MDL_NAMESPACE,
                Icon.TODAY,
                LocalDate.parse(SampleData.BIRTH_DATE).toDataItemFullDate()
            )
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "issue_date",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_DATE_OF_ISSUE),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_DATE_OF_ISSUE),
                true,
                MDL_NAMESPACE,
                Icon.DATE_RANGE,
                LocalDate.parse(SampleData.ISSUE_DATE).toDataItemFullDate()
            )
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "expiry_date",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_DATE_OF_EXPIRY),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_DATE_OF_EXPIRY),
                true,
                MDL_NAMESPACE,
                Icon.CALENDAR_CLOCK,
                LocalDate.parse(SampleData.EXPIRY_DATE).toDataItemFullDate()
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(Options.COUNTRY_ISO_3166_1_ALPHA_2),
                "issuing_country",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_ISSUING_COUNTRY),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_ISSUING_COUNTRY),
                true,
                MDL_NAMESPACE,
                Icon.ACCOUNT_BALANCE,
                SampleData.ISSUING_COUNTRY.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "issuing_authority",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_ISSUING_AUTHORITY),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_ISSUING_AUTHORITY),
                true,
                MDL_NAMESPACE,
                Icon.ACCOUNT_BALANCE,
                SampleData.ISSUING_AUTHORITY_MDL.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "document_number",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_LICENSE_NUMBER),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_LICENSE_NUMBER),
                true,
                MDL_NAMESPACE,
                Icon.NUMBERS,
                SampleData.DOCUMENT_NUMBER.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Picture,
                "portrait",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_PHOTO_OF_HOLDER),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_PHOTO_OF_HOLDER),
                true,
                MDL_NAMESPACE,
                Icon.ACCOUNT_BOX,
                SampleData.PORTRAIT_BASE64URL.fromBase64Url().toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "driving_privileges",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_DRIVING_PRIVILEGES),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_DRIVING_PRIVILEGES),
                true,
                MDL_NAMESPACE,
                Icon.DIRECTIONS_CAR,
                buildCborArray {
                    addCborMap {
                        put("vehicle_category_code", "A")
                        put("issue_date", Tagged(Tagged.FULL_DATE_STRING, Tstr("2018-08-09")))
                        put("expiry_date", Tagged(Tagged.FULL_DATE_STRING, Tstr("2028-09-01")))
                    }
                    addCborMap {
                        put("vehicle_category_code", "B")
                        put("issue_date", Tagged(Tagged.FULL_DATE_STRING, Tstr("2017-02-23")))
                        put("expiry_date", Tagged(Tagged.FULL_DATE_STRING, Tstr("2028-09-01")))
                    }
                }
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(Options.DISTINGUISHING_SIGN_ISO_IEC_18013_1_ANNEX_F),
                "un_distinguishing_sign",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_UN_DISTINGUISHING_SIGN),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_UN_DISTINGUISHING_SIGN),
                true,
                MDL_NAMESPACE,
                Icon.LANGUAGE,
                SampleData.UN_DISTINGUISHING_SIGN.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "administrative_number",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_ADMINISTRATIVE_NUMBER),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_ADMINISTRATIVE_NUMBER),
                false,
                MDL_NAMESPACE,
                Icon.NUMBERS,
                SampleData.ADMINISTRATIVE_NUMBER.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.IntegerOptions(Options.SEX_ISO_IEC_5218),
                "sex",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_SEX),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_SEX),
                false,
                MDL_NAMESPACE,
                Icon.EMERGENCY,
                SampleData.SEX_ISO_5218.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Number,
                "height",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_HEIGHT),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_HEIGHT),
                false,
                MDL_NAMESPACE,
                Icon.EMERGENCY,
                SampleData.HEIGHT_CM.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Number,
                "weight",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_WEIGHT),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_WEIGHT),
                false,
                MDL_NAMESPACE,
                Icon.EMERGENCY,
                SampleData.WEIGHT_KG.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(
                    listOf(
                        StringOption(null, "(not set)"),
                        StringOption("black", "Black"),
                        StringOption("blue", "Blue"),
                        StringOption("brown", "Brown"),
                        StringOption("dichromatic", "Dichromatic"),
                        StringOption("grey", "Grey"),
                        StringOption("green", "Green"),
                        StringOption("hazel", "Hazel"),
                        StringOption("maroon", "Maroon"),
                        StringOption("pink", "Pink"),
                        StringOption("unknown", "Unknown")
                    )
                ),
                "eye_colour",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_EYE_COLOR),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_EYE_COLOR),
                false,
                MDL_NAMESPACE,
                Icon.PERSON,
                "blue".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(
                    listOf(
                        StringOption(null, "(not set)"),
                        StringOption("bald", "Bald"),
                        StringOption("black", "Black"),
                        StringOption("blond", "Blond"),
                        StringOption("brown", "Brown"),
                        StringOption("grey", "Grey"),
                        StringOption("red", "Red"),
                        StringOption("auburn", "Auburn"),
                        StringOption("sandy", "Sandy"),
                        StringOption("white", "White"),
                        StringOption("unknown", "Unknown"),
                    )
                ),
                "hair_colour",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_HAIR_COLOR),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_HAIR_COLOR),
                false,
                MDL_NAMESPACE,
                Icon.PERSON,
                "blond".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "birth_place",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_PLACE_OF_BIRTH),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_PLACE_OF_BIRTH),
                false,
                MDL_NAMESPACE,
                Icon.PLACE,
                SampleData.BIRTH_PLACE.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "resident_address",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_RESIDENT_ADDRESS),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_RESIDENT_ADDRESS),
                false,
                MDL_NAMESPACE,
                Icon.PLACE,
                SampleData.RESIDENT_ADDRESS.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "portrait_capture_date",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_PORTRAIT_IMAGE_TIMESTAMP),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_PORTRAIT_IMAGE_TIMESTAMP),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                LocalDate.parse(SampleData.PORTRAIT_CAPTURE_DATE).toDataItemFullDate()
            )
            .addMdocAttribute(
                DocumentAttributeType.Number,
                "age_in_years",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_AGE_IN_YEARS),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_AGE_IN_YEARS),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_IN_YEARS.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Number,
                "age_birth_year",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_YEAR_OF_BIRTH),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_YEAR_OF_BIRTH),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_BIRTH_YEAR.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_over_13",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_OLDER_THAN_13),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_OLDER_THAN_13),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_13.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_over_16",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_OLDER_THAN_16),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_OLDER_THAN_16),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_16.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_over_18",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_OLDER_THAN_18),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_OLDER_THAN_18),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_18.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_over_21",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_OLDER_THAN_21),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_OLDER_THAN_21),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_21.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_over_25",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_OLDER_THAN_25),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_OLDER_THAN_25),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_25.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_over_60",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_OLDER_THAN_60),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_OLDER_THAN_60),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_60.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_over_62",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_OLDER_THAN_62),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_OLDER_THAN_62),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_62.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_over_65",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_OLDER_THAN_65),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_OLDER_THAN_65),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_65.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "age_over_68",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_OLDER_THAN_68),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_OLDER_THAN_68),
                false,
                MDL_NAMESPACE,
                Icon.TODAY,
                SampleData.AGE_OVER_68.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "issuing_jurisdiction",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_ISSUING_JURISDICTION),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_ISSUING_JURISDICTION),
                false,
                MDL_NAMESPACE,
                Icon.ACCOUNT_BALANCE,
                SampleData.ISSUING_JURISDICTION.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(Options.COUNTRY_ISO_3166_1_ALPHA_2),
                "nationality",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_NATIONALITY),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_NATIONALITY),
                false,
                MDL_NAMESPACE,
                Icon.LANGUAGE,
                SampleData.NATIONALITY.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "resident_city",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_RESIDENT_CITY),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_RESIDENT_CITY),
                false,
                MDL_NAMESPACE,
                Icon.PLACE,
                SampleData.RESIDENT_CITY.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "resident_state",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_RESIDENT_STATE),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_RESIDENT_STATE),
                false,
                MDL_NAMESPACE,
                Icon.PLACE,
                SampleData.RESIDENT_STATE.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "resident_postal_code",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_RESIDENT_POSTAL_CODE),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_RESIDENT_POSTAL_CODE),
                false,
                MDL_NAMESPACE,
                Icon.PLACE,
                SampleData.RESIDENT_POSTAL_CODE.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(Options.COUNTRY_ISO_3166_1_ALPHA_2),
                "resident_country",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_RESIDENT_COUNTRY),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_RESIDENT_COUNTRY),
                false,
                MDL_NAMESPACE,
                Icon.PLACE,
                SampleData.RESIDENT_COUNTRY.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "family_name_national_character",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_FAMILY_NAME_NATIONAL_CHARACTERS),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_FAMILY_NAME_NATIONAL_CHARACTERS),
                false,
                MDL_NAMESPACE,
                Icon.PERSON,
                SampleData.FAMILY_NAME_NATIONAL_CHARACTER.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "given_name_national_character",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_GIVEN_NAME_NATIONAL_CHARACTERS),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_GIVEN_NAME_NATIONAL_CHARACTERS),
                false,
                MDL_NAMESPACE,
                Icon.PERSON,
                SampleData.GIVEN_NAMES_NATIONAL_CHARACTER.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Picture,
                "signature_usual_mark",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_SIGNATURE_USUAL_MARK),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_SIGNATURE_USUAL_MARK),
                false,
                MDL_NAMESPACE,
                Icon.SIGNATURE,
                SampleData.SIGNATURE_OR_USUAL_MARK_BASE64URL.fromBase64Url().toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "domestic_driving_privileges",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_DOMESTIC_DRIVING_PRIVILEGES),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_DOMESTIC_DRIVING_PRIVILEGES),
                true,
                AAMVA_NAMESPACE,
                Icon.DIRECTIONS_CAR,
                buildCborArray {}
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(Options.AAMVA_NAME_SUFFIX),
                "name_suffix",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_NAME_SUFFIX),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_NAME_SUFFIX),
                false,
                AAMVA_NAMESPACE,
                Icon.PERSON,
                "Jr III".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.IntegerOptions(
                    listOf(
                        IntegerOption(null, "(not set)"),
                        IntegerOption(1, "Donor")
                    )
                ),
                "organ_donor",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_ORGAN_DONOR),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_ORGAN_DONOR),
                false,
                AAMVA_NAMESPACE,
                Icon.EMERGENCY,
                1.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.IntegerOptions(
                    listOf(
                        IntegerOption(null, "(not set)"),
                        IntegerOption(1, "Veteran")
                    )
                ),
                "veteran",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_VETERAN),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_VETERAN),
                false,
                AAMVA_NAMESPACE,
                Icon.MILITARY_TECH,
                1.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(
                    listOf(
                        StringOption(null, "(not set)"),
                        StringOption("T", "Truncated"),
                        StringOption("N", "Not truncated"),
                        StringOption("U", "Unknown whether truncated"),
                    )
                ),
                "family_name_truncation",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_FAMILY_NAME_TRUNCATION),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_FAMILY_NAME_TRUNCATION),
                true,
                AAMVA_NAMESPACE,
                Icon.PERSON,
                "N".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(
                    listOf(
                        StringOption(null, "(not set)"),
                        StringOption("T", "Truncated"),
                        StringOption("N", "Not truncated"),
                        StringOption("U", "Unknown whether truncated"),
                    )
                ),
                "given_name_truncation",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_GIVEN_NAME_TRUNCATION),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_GIVEN_NAME_TRUNCATION),
                true,
                AAMVA_NAMESPACE,
                Icon.PERSON,
                "N".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "aka_family_name",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_ALIAS_FAMILY_NAME),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_ALIAS_FAMILY_NAME),
                false,
                AAMVA_NAMESPACE,
                Icon.PERSON,
                "Musstermensch".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "aka_given_name",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_ALIAS_GIVEN_NAME),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_ALIAS_GIVEN_NAME),
                false,
                AAMVA_NAMESPACE,
                Icon.PERSON,
                "Erica".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(Options.AAMVA_NAME_SUFFIX),
                "aka_suffix",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_ALIAS_SUFFIX),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_ALIAS_SUFFIX),
                false,
                AAMVA_NAMESPACE,
                Icon.PERSON,
                "Ica".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.IntegerOptions(
                    listOf(
                        IntegerOption(null, "(not set)"),
                        IntegerOption(0, "Up to 31 kg (up to 70 lbs.)"),
                        IntegerOption(1, "32 – 45 kg (71 – 100 lbs.)"),
                        IntegerOption(2, "46 - 59 kg (101 – 130 lbs.)"),
                        IntegerOption(3, "60 - 70 kg (131 – 160 lbs.)"),
                        IntegerOption(4, "71 - 86 kg (161 – 190 lbs.)"),
                        IntegerOption(5, "87 - 100 kg (191 – 220 lbs.)"),
                        IntegerOption(6, "101 - 113 kg (221 – 250 lbs.)"),
                        IntegerOption(7, "114 - 127 kg (251 – 280 lbs.)"),
                        IntegerOption(8, "128 – 145 kg (281 – 320 lbs.)"),
                        IntegerOption(9, "146+ kg (321+ lbs.)"),
                    )
                ),
                "weight_range",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_WEIGHT_RANGE),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_WEIGHT_RANGE),
                false,
                AAMVA_NAMESPACE,
                Icon.EMERGENCY,
                3.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(
                    listOf(
                        StringOption(null, "(not set)"),
                        StringOption("AI", "Alaskan or American Indian"),
                        StringOption("AP", "Asian or Pacific Islander"),
                        StringOption("BK", "Black"),
                        StringOption("H", "Hispanic Origin"),
                        StringOption("O", "Non-hispanic"),
                        StringOption("U", "Unknown"),
                        StringOption("W", "White")
                    )
                ),
                "race_ethnicity",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_RACE_ETHNICITY),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_RACE_ETHNICITY),
                false,
                AAMVA_NAMESPACE,
                Icon.EMERGENCY,
                "W".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.StringOptions(
                    listOf(
                        StringOption(null, "(not set)"),
                        StringOption("F", "Fully compliant"),
                        StringOption("N", "Non-compliant"),
                    )
                ),
                "DHS_compliance",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_COMPLIANCE_TYPE),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_COMPLIANCE_TYPE),
                false,
                AAMVA_NAMESPACE,
                Icon.STARS,
                "F".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.IntegerOptions(
                    listOf(
                        IntegerOption(null, "(not set)"),
                        IntegerOption(1, "Temporary lawful status")
                    )
                ),
                "DHS_temporary_lawful_status",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_LIMITED_DURATION_DOCUMENT_INDICATOR),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_LIMITED_DURATION_DOCUMENT_INDICATOR),
                false,
                AAMVA_NAMESPACE,
                Icon.STARS,
                1.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.IntegerOptions(
                    listOf(
                        IntegerOption(null, "(not set)"),
                        IntegerOption(1, "Driver's license"),
                        IntegerOption(2, "Identification card")
                    )
                ),
                "EDL_credential",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_EDL_INDICATOR),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_EDL_INDICATOR),
                false,
                AAMVA_NAMESPACE,
                Icon.DIRECTIONS_CAR,
                1.toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "resident_county",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_RESIDENT_COUNTY),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_RESIDENT_COUNTY),
                false,
                AAMVA_NAMESPACE,
                Icon.PLACE,
                "037".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "hazmat_endorsement_expiration_date",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_HAZMAT_ENDORSEMENT_EXPIRATION_DATE),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_HAZMAT_ENDORSEMENT_EXPIRATION_DATE),
                true,
                AAMVA_NAMESPACE,
                Icon.CALENDAR_CLOCK,
                LocalDate.parse(SampleData.EXPIRY_DATE).toDataItemFullDate()
            )
            .addMdocAttribute(
                DocumentAttributeType.IntegerOptions(Options.SEX_ISO_IEC_5218),
                "sex",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_SEX),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_SEX),
                true,
                AAMVA_NAMESPACE,
                Icon.EMERGENCY,
                SampleData.SEX_ISO_5218.toDataItem()
            )
            /*
             * Then the attributes that exist only in the mDL Credential Type and not in the VC Credential Type
             */
            .addMdocAttribute(
                DocumentAttributeType.Picture,
                "biometric_template_face",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_BIOMETRIC_TEMPLATE_FACE),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_BIOMETRIC_TEMPLATE_FACE),
                false,
                MDL_NAMESPACE,
                Icon.FACE,
                Simple.NULL
            )
            .addMdocAttribute(
                DocumentAttributeType.Picture,
                "biometric_template_finger",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_BIOMETRIC_TEMPLATE_FINGERPRINT),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_BIOMETRIC_TEMPLATE_FINGERPRINT),
                false,
                MDL_NAMESPACE,
                Icon.FINGERPRINT,
                Simple.NULL
            )
            .addMdocAttribute(
                DocumentAttributeType.Picture,
                "biometric_template_signature_sign",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_BIOMETRIC_TEMPLATE_SIGNATURE_SIGN),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_BIOMETRIC_TEMPLATE_SIGNATURE_SIGN),
                false,
                MDL_NAMESPACE,
                Icon.SIGNATURE,
                Simple.NULL
            )
            .addMdocAttribute(
                DocumentAttributeType.Picture,
                "biometric_template_iris",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_BIOMETRIC_TEMPLATE_IRIS),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_BIOMETRIC_TEMPLATE_IRIS),
                false,
                MDL_NAMESPACE,
                Icon.EYE_TRACKING,
                Simple.NULL
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "audit_information",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_AUDIT_INFORMATION),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_AUDIT_INFORMATION),
                false,
                AAMVA_NAMESPACE,
                Icon.STARS,
                "".toDataItem()
            )
            .addMdocAttribute(
                DocumentAttributeType.Number,
                "aamva_version",
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_ATTRIBUTE_AAMVA_VERSION_NUMBER),
                getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_DESCRIPTION_AAMVA_VERSION_NUMBER),
                true,
                AAMVA_NAMESPACE,
                Icon.NUMBERS,
                1.toDataItem()
            )
            .addSampleRequest(
                id = "us-transportation",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_US_TRANSPORTATION),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(
                        "sex" to false,
                        "portrait" to false,
                        "given_name" to false,
                        "issue_date" to false,
                        "expiry_date" to false,
                        "family_name" to false,
                        "document_number" to false,
                        "issuing_authority" to false
                    ),
                    AAMVA_NAMESPACE to mapOf(
                        "DHS_compliance" to false,
                        "EDL_credential" to false
                    ),
                )
            )
            .addSampleRequest(
                id = "age_over_18",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_AGE_OVER_18),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(
                        "age_over_18" to false,
                    )
                ),
            )
            .addSampleRequest(
                id = "age_over_21",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_AGE_OVER_21),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(
                        "age_over_21" to false,
                    )
                ),
            )
            .addSampleRequest(
                id = "age_over_18_zkp",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_AGE_OVER_18_ZKP),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(
                        "age_over_18" to false,
                    )
                ),
                mdocUseZkp = true
            )
            .addSampleRequest(
                id = "age_over_21_zkp",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_AGE_OVER_21_ZKP),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(
                        "age_over_21" to false,
                    )
                ),
                mdocUseZkp = true
            )
            .addSampleRequest(
                id = "age_over_18_and_portrait",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_AGE_OVER_18_AND_PORTRAIT),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(
                        "age_over_18" to false,
                        "portrait" to false
                    )
                ),
            )
            .addSampleRequest(
                id = "age_over_21_and_portrait",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_AGE_OVER_21_AND_PORTRAIT),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(
                        "age_over_21" to false,
                        "portrait" to false
                    )
                ),
            )
            .addSampleRequest(
                id = "mandatory",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_MANDATORY_DATA_ELEMENTS),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(
                        "family_name" to false,
                        "given_name" to false,
                        "birth_date" to false,
                        "issue_date" to false,
                        "expiry_date" to false,
                        "issuing_country" to false,
                        "issuing_authority" to false,
                        "document_number" to false,
                        "portrait" to false,
                        "driving_privileges" to false,
                        "un_distinguishing_sign" to false,
                    )
                )
            )
            .addSampleRequest(
                id = "full",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_ALL_DATA_ELEMENTS),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(),
                    AAMVA_NAMESPACE to mapOf()
                )
            )
            .addSampleRequest(
                id = "name-and-address-partially-stored",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_NAME_AND_ADDRESS_PARTIALLY_STORED),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(
                        "family_name" to true,
                        "given_name" to true,
                        "issuing_authority" to false,
                        "portrait" to false,
                        "resident_address" to true,
                        "resident_city" to true,
                        "resident_state" to true,
                        "resident_postal_code" to true,
                        "resident_country" to true,
                    ),
                    AAMVA_NAMESPACE to mapOf(
                        "resident_county" to true,
                    )
                )
            )
            .addSampleRequest(
                id = "name-and-address-all-stored",
                displayName = getLocalizedString(GeneratedStringKeys.DRIVING_LICENSE_REQUEST_NAME_AND_ADDRESS_ALL_STORED),
                mdocDataElements = mapOf(
                    MDL_NAMESPACE to mapOf(
                        "family_name" to true,
                        "given_name" to true,
                        "issuing_authority" to true,
                        "portrait" to true,
                        "resident_address" to true,
                        "resident_city" to true,
                        "resident_state" to true,
                        "resident_postal_code" to true,
                        "resident_country" to true,
                    ),
                    AAMVA_NAMESPACE to mapOf(
                        "resident_county" to true,
                    )
                )
            )
            .build()
    }
}
