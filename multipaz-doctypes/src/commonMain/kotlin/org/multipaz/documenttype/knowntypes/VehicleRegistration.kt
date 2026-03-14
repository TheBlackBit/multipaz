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
 * Object containing the metadata of the Vehicle Registration
 * Document Type.
 */

object VehicleRegistration {
    const val MVR_NAMESPACE = "nl.rdw.mekb.1"

    /**
     * Build the Vehicle Registration Document Type.
     */
    fun getDocumentType(locale: String = LocalizedStrings.getCurrentLocale()): DocumentType {
        fun getLocalizedString(key: String) = LocalizedStrings.getString(key, locale)

        return DocumentType.Builder(getLocalizedString(GeneratedStringKeys.DOCUMENT_DISPLAY_NAME_VEHICLE_REGISTRATION))
            .addMdocDocumentType("nl.rdw.mekb.1")
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "registration_info",
                getLocalizedString(GeneratedStringKeys.VEHICLE_REGISTRATION_ATTRIBUTE_REGISTRATION_INFO),
                getLocalizedString(GeneratedStringKeys.VEHICLE_REGISTRATION_DESCRIPTION_REGISTRATION_INFO),
                true,
                MVR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.Date,
                "issue_date",
                getLocalizedString(GeneratedStringKeys.VEHICLE_REGISTRATION_ATTRIBUTE_ISSUE_DATE),
                getLocalizedString(GeneratedStringKeys.VEHICLE_REGISTRATION_DESCRIPTION_ISSUE_DATE),
                true,
                MVR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "registration_holder",
                getLocalizedString(GeneratedStringKeys.VEHICLE_REGISTRATION_ATTRIBUTE_REGISTRATION_HOLDER),
                getLocalizedString(GeneratedStringKeys.VEHICLE_REGISTRATION_DESCRIPTION_REGISTRATION_HOLDER),
                true,
                MVR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.ComplexType,
                "basic_vehicle_info",
                getLocalizedString(GeneratedStringKeys.VEHICLE_REGISTRATION_ATTRIBUTE_BASIC_VEHICLE_INFO),
                getLocalizedString(GeneratedStringKeys.VEHICLE_REGISTRATION_DESCRIPTION_BASIC_VEHICLE_INFO),
                true,
                MVR_NAMESPACE
            )
            .addMdocAttribute(
                DocumentAttributeType.String,
                "vin",
                getLocalizedString(GeneratedStringKeys.VEHICLE_REGISTRATION_ATTRIBUTE_VIN),
                getLocalizedString(GeneratedStringKeys.VEHICLE_REGISTRATION_DESCRIPTION_VIN),
                true,
                MVR_NAMESPACE
            )
            .build()
    }
}
