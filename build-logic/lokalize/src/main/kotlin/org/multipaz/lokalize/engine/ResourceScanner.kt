package org.multipaz.lokalize.engine

import org.multipaz.lokalize.model.LocaleBundle
import java.io.File

/**
 * Resource scanner that dispatches to the appropriate strategy based on file format.
 *
 * This class serves as a convenience wrapper that automatically detects the file format
 * and uses the correct scanning strategy (XML or JSON).
 *
 * For more control, use [ResourceScannerStrategyFactory] directly to get a specific strategy.
 *
 * Example usage:
 * ```kotlin
 * val scanner = ResourceScanner()
 * val bundle = scanner.scan(file) // Automatically detects format from file extension
 * ```
 */
class ResourceScanner {

    /**
     * Scans a resource file and returns a populated LocaleBundle.
     * Automatically detects the file format from the extension.
     *
     * @param file The resource file to scan (e.g., strings.xml or strings.json)
     * @return A LocaleBundle containing all extracted entries, or empty bundle if file doesn't exist
     */
    fun scan(file: File): LocaleBundle {
        val strategy = ResourceScannerStrategyFactory.createFromFile(file)
        return strategy.scan(file)
    }

    /**
     * Scans a resource file using a specific format strategy.
     *
     * @param file The resource file to scan
     * @param strategy The scanning strategy to use
     * @return A LocaleBundle containing all extracted entries
     */
    fun scan(file: File, strategy: ResourceScannerStrategy): LocaleBundle {
        return strategy.scan(file)
    }
}
