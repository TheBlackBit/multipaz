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

import org.multipaz.documenttype.DocumentAttributeType
import org.multipaz.documenttype.DocumentType
import org.multipaz.doctypes.localization.LocalizedStrings
import org.multipaz.doctypes.localization.GeneratedStringKeys

/**
 * Object containing the metadata of the Vaccination
 * Document Type.
 */
object VaccinationDocument {
    const val MICOV_ATT_NAMESPACE = "org.micov.attestation.1"
    const val MICOV_VTR_NAMESPACE = "org.micov.vtr.1"

    /**
     * Build the Vaccination Document Type.
     */
    fun getDocumentType(locale: String = LocalizedStrings.getCurrentLocale()): DocumentType {
        fun getLocalizedString(key: String) = LocalizedStrings.getString(key, locale)

        return DocumentType.Builder(getLocalizedString(GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_VACCINATION_DOCUMENT))
            .addMdocDocumentType("org.micov.1")
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "1D47_vaccinated",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_YELLOW_FEVER_VACCINATED),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_YELLOW_FEVER_VACCINATED),
                false,
                MICOV_ATT_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.Boolean,
                "RA01_vaccinated",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_COVID19_VACCINATED),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_COVID19_VACCINATED),
                false,
                MICOV_ATT_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "RA01_test",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_COVID19_TEST),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_COVID19_TEST),
                false,
                MICOV_ATT_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "safeEntry_Leisure",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_SAFE_ENTRY_LEISURE),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_SAFE_ENTRY_LEISURE),
                false,
                MICOV_ATT_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.Picture,
                "fac",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_FACIAL_IMAGE),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_FACIAL_IMAGE),
                false,
                MICOV_ATT_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "fni",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_FAMILY_NAME_INITIAL),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_FAMILY_NAME_INITIAL),
                false,
                MICOV_ATT_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "gni",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_GIVEN_NAME_INITIAL),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_GIVEN_NAME_INITIAL),
                false,
                MICOV_ATT_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.Number,
                "by",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_BIRTH_YEAR),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_BIRTH_YEAR),
                false,
                MICOV_ATT_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.Number,
                "bm",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_BIRTH_MONTH),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_BIRTH_MONTH),
                false,
                MICOV_ATT_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.Number,
                "bd",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_BIRTH_DAY),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_BIRTH_DAY),
                false,
                MICOV_ATT_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "fn",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_FAMILY_NAME),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_FAMILY_NAME),
                true,
                MICOV_VTR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "gn",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_GIVEN_NAME),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_GIVEN_NAME),
                true,
                MICOV_VTR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "dob",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_DATE_OF_BIRTH),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_DATE_OF_BIRTH),
                true,
                MICOV_VTR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.IntegerOptions(Options.SEX_ISO_IEC_5218),
                "sex",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_SEX),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_SEX),
                false,
                MICOV_VTR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "v_RA01_1",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_COVID19_FIRST_VACCINATION),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_COVID19_FIRST_VACCINATION),
                false,
                MICOV_VTR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "v_RA01_2",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_COVID19_SECOND_VACCINATION),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_COVID19_SECOND_VACCINATION),
                false,
                MICOV_VTR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "pid_PPN",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_ID_WITH_PASSPORT_NUMBER),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_ID_WITH_PASSPORT_NUMBER),
                false,
                MICOV_VTR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "pid_DL",
                getLocalizedString(GeneratedStringKeys.VACCINATION_ATTRIBUTE_ID_WITH_DRIVERS_LICENSE_NUMBER),
                getLocalizedString(GeneratedStringKeys.VACCINATION_DESCRIPTION_ID_WITH_DRIVERS_LICENSE_NUMBER),
                false,
                MICOV_VTR_NAMESPACE
            )
            .build()
    }
}
