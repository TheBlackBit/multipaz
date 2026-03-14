package org.multipaz.doctypes.localization

import kotlin.js.js

internal actual object NativeLocale {
    actual fun currentLocale(): String {
        return try {
            js("navigator.language || 'en'").toString().substringBefore("-")
        } catch (e: Throwable) {
            "en"
        }
    }
}