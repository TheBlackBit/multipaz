package org.multipaz.doctypes.localization

import java.util.Locale

internal actual object NativeLocale {
    actual fun currentLocale(): String {
        return Locale.getDefault().language
    }
}