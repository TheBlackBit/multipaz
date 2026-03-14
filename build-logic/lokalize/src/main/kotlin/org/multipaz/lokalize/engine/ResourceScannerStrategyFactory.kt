package org.multipaz.lokalize.engine

import org.multipaz.lokalize.util.OutputFormat
import java.io.File

/**
 * Factory for creating ResourceScannerStrategy instances based on the input format.
 */
object ResourceScannerStrategyFactory {

    /**
     * Creates a ResourceScannerStrategy for the specified output format.
     *
     * @param format The desired input format (XML or JSON)
     * @return The appropriate ResourceScannerStrategy implementation
     */
    fun create(format: OutputFormat): ResourceScannerStrategy {
        return when (format) {
            OutputFormat.XML -> XmlResourceScannerStrategy()
            OutputFormat.JSON -> JsonResourceScannerStrategy()
        }
    }

    /**
     * Creates a ResourceScannerStrategy based on file extension.
     *
     * @param fileName The filename to detect format from
     * @return The appropriate ResourceScannerStrategy implementation (defaults to XML)
     */
    fun createFromFileName(fileName: String): ResourceScannerStrategy {
        return when {
            fileName.endsWith(".json", ignoreCase = true) -> JsonResourceScannerStrategy()
            fileName.endsWith(".xml", ignoreCase = true) -> XmlResourceScannerStrategy()
            else -> XmlResourceScannerStrategy() // Default to XML for backward compatibility
        }
    }

    /**
     * Detects the format from a file and returns the appropriate scanner.
     *
     * @param file The file to detect format from
     * @return The appropriate ResourceScannerStrategy implementation
     */
    fun createFromFile(file: File): ResourceScannerStrategy {
        return createFromFileName(file.name)
    }
}
