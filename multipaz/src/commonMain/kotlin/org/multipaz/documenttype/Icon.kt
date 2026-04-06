package org.multipaz.documenttype

/**
 * An enumeration of icons used to represent ISO mdoc data elements or JSON-based credential claims.
 *
 * @property iconName the icon name according to https://fonts.google.com/icons
 */
enum class Icon(
    val iconName: String
) {
    /** Represents a person, identity, or user profile. */
    PERSON("person"),

    /** Represents a specific day, such as an issue date or current date. */
    TODAY("today"),

    /** Represents a duration, validity period, or a start and end date. */
    DATE_RANGE("date_range"),

    /** Represents a timestamp or a combination of date and time. */
    CALENDAR_CLOCK("calendar_clock"),

    /** Represents an issuing authority, financial institution, or administrative entity. */
    ACCOUNT_BALANCE("account_balance"),

    /** Represents identification numbers, document numbers, or other numeric claims. */
    NUMBERS("numbers"),

    /** Represents an account or a detailed profile box. */
    ACCOUNT_BOX("account_box"),

    /** Represents driving privileges, a personal vehicle, or a driver's license claim. */
    DIRECTIONS_CAR("directions_car"),

    /** Represents a spoken or written language, or nationality. */
    LANGUAGE("language"),

    /** Represents emergency contact information, medical alerts, or urgent data. */
    EMERGENCY("emergency"),

    /** Represents a physical location, point of interest, or geographic coordinates. */
    PLACE("place"),

    /** Represents a physical or cryptographic signature. */
    SIGNATURE("signature"),

    /** Represents military service, veteran status, or decorations. */
    MILITARY_TECH("military_tech"),

    /** Represents special statuses, ratings, or premium features. */
    STARS("stars"),

    /** Represents a portrait, facial image, or facial biometrics. */
    FACE("face"),

    /** Represents fingerprint biometrics or touch-based authentication. */
    FINGERPRINT("fingerprint"),

    /** Represents iris biometrics or eye-related physical traits. */
    EYE_TRACKING("eye_tracking"),

    /** Represents commercial driving privileges, passenger transport, or shuttle services. */
    AIRPORT_SHUTTLE("airport_shuttle"),

    /** Represents a wide-angle view, panorama, or landscape-oriented visual data. */
    PANORAMA_WIDE_ANGLE("panorama_wide_angle"),

    /** Represents a generic image, photograph, or scanned document. */
    IMAGE("image"),

    /** Represents a city, municipality, or urban location data. */
    LOCATION_CITY("location_city"),

    /** Represents routing, directions, or travel-related information. */
    DIRECTIONS("directions"),

    /** Represents a home address, primary residence, or domestic property. */
    HOUSE("house"),

    /** Represents a country of issue, nationality, or geographic state. */
    FLAG("flag"),

    /** Represents an apartment, suite, or unit number within a larger building. */
    APARTMENT("apartment"),

    /** Represents Japanese Kana characters or specific regional language or script data. */
    LANGUAGE_JAPANESE_KANA("language_japanese_kana"),

    /** Represents a telephone number or mobile contact information. */
    PHONE("phone"),

    /** Represents an email address or electronic contact information. */
    EMAIL("alternate_email"),

    /** Represents an identification badge, credential, or official document status. */
    BADGE("badge"),

    /** Represents global, international, or worldwide attributes. */
    GLOBE("globe")
}