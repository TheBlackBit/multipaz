package org.multipaz.lokalize.engine

import org.multipaz.lokalize.model.LocaleBundle
import java.io.File

/**
 * Strategy interface for writing resource files in different formats.
 * Implementations handle the specifics of writing XML (Android) or JSON (web/desktop) formats.
 */
interface ResourceWriterStrategy {

    /**
     * Merges a partial LocaleBundle (missing translations) into an existing resource file.
     * Creates the file if it doesn't exist.
     *
     * @param file The output file (e.g., strings.xml or strings.json)
     * @param bundle The locale bundle containing entries to write
     * @param preserveExisting Whether to preserve existing entries (true) or overwrite (false)
     */
    fun mergeBundle(
        file: File,
        bundle: LocaleBundle,
        preserveExisting: Boolean = true
    )

    /**
     * Writes a complete locale bundle to a new file.
     * Overwrites any existing file.
     *
     * @param file The output file
     * @param bundle The locale bundle to write
     */
    fun writeBundle(file: File, bundle: LocaleBundle)
}
