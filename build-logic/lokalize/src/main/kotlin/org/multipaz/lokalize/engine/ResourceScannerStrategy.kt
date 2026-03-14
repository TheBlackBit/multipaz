package org.multipaz.lokalize.engine

import org.multipaz.lokalize.model.LocaleBundle
import java.io.File

/**
 * Strategy interface for scanning resource files in different formats.
 * Implementations handle the specifics of parsing XML (Android) or JSON (web/desktop) formats.
 */
interface ResourceScannerStrategy {

    /**
     * Scans a resource file and returns a populated LocaleBundle.
     *
     * @param file The resource file to scan (e.g., strings.xml or strings.json)
     * @return A LocaleBundle containing all extracted entries, or empty bundle if file doesn't exist
     */
    fun scan(file: File): LocaleBundle

    /**
     * Checks if this strategy supports the given file.
     *
     * @param file The file to check
     * @return true if this strategy can scan the file
     */
    fun supports(file: File): Boolean
}
