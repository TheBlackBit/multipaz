package org.multipaz.lokalize.engine

import org.multipaz.lokalize.util.OutputFormat

/**
 * Factory for creating ResourceWriterStrategy instances based on the output format.
 */
object ResourceWriterStrategyFactory {

    /**
     * Creates a ResourceWriterStrategy for the specified output format.
     *
     * @param format The desired output format (XML or JSON)
     * @return The appropriate ResourceWriterStrategy implementation
     */
    fun create(format: OutputFormat): ResourceWriterStrategy {
        return when (format) {
            OutputFormat.XML -> XmlResourceWriterStrategy()
            OutputFormat.JSON -> JsonResourceWriterStrategy()
        }
    }
}
