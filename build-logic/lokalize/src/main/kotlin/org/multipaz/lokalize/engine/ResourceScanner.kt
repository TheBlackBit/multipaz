package org.multipaz.lokalize.engine

import org.multipaz.lokalize.model.LocaleBundle
import org.multipaz.lokalize.model.ResourceEntry
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Scans Android strings.xml files and extracts all resource types.
 *
 * Supports:
 * - Simple strings (<string>)
 * - Plurals (<plurals> with <item quantity="...">)
 * - String arrays (<string-array> with <item>)
 */
class ResourceScanner {

    /**
     * Scans a strings.xml file and returns a populated LocaleBundle.
     */
    fun scan(file: File): LocaleBundle {
        if (!file.exists()) {
            return LocaleBundle.empty("unknown")
        }

        val document = parseXmlSecurely(file)
        val entries = extractEntries(document)
        val locale = extractLocaleFromPath(file)

        return LocaleBundle(
            locale = locale,
            strings = entries.strings,
            plurals = entries.plurals,
            arrays = entries.arrays
        )
    }

    /**
     * Parses XML with security features enabled (XXE protection).
     */
    private fun parseXmlSecurely(file: File): Document {
        return DocumentBuilderFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            isNamespaceAware = true
        }.newDocumentBuilder().parse(file)
    }

    /**
     * Container for all extracted resource entries.
     */
    private data class ResourceEntries(
        val strings: Map<String, ResourceEntry.StringEntry>,
        val plurals: Map<String, ResourceEntry.PluralEntry>,
        val arrays: Map<String, ResourceEntry.StringArrayEntry>
    )

    /**
     * Extracts all resource entries from the XML document.
     */
    private fun extractEntries(document: Document): ResourceEntries {
        val root = document.documentElement ?: return ResourceEntries(emptyMap(), emptyMap(), emptyMap())

        return ResourceEntries(
            strings = extractStrings(root),
            plurals = extractPlurals(root),
            arrays = extractArrays(root)
        )
    }

    /**
     * Extracts all <string> entries.
     */
    private fun extractStrings(root: Element): Map<String, ResourceEntry.StringEntry> {
        return root.getElementsByTagName("string")
            .asSequence()
            .mapNotNull { node ->
                val name = node.getAttribute("name") ?: return@mapNotNull null
                val value = node.textContent ?: ""
                name to ResourceEntry.StringEntry(name, value)
            }
            .toMap()
    }

    /**
     * Extracts all <plurals> entries with their quantity variants.
     */
    private fun extractPlurals(root: Element): Map<String, ResourceEntry.PluralEntry> {
        return root.getElementsByTagName("plurals")
            .asSequence()
            .mapNotNull { pluralNode ->
                val name = pluralNode.getAttribute("name") ?: return@mapNotNull null
                val items = extractPluralItems(pluralNode)
                name to ResourceEntry.PluralEntry(name, items)
            }
            .toMap()
    }

    /**
     * Extracts quantity items from a plurals node.
     */
    private fun extractPluralItems(pluralNode: Node): Map<String, String> {
        return pluralNode.childNodes
            .asSequence()
            .filter { it.nodeName == "item" }
            .mapNotNull { itemNode ->
                val quantity = itemNode.getAttribute("quantity") ?: return@mapNotNull null
                val value = itemNode.textContent ?: ""
                quantity to value
            }
            .toMap()
    }

    /**
     * Extracts all <string-array> entries.
     */
    private fun extractArrays(root: Element): Map<String, ResourceEntry.StringArrayEntry> {
        return root.getElementsByTagName("string-array")
            .asSequence()
            .mapNotNull { arrayNode ->
                val name = arrayNode.getAttribute("name") ?: return@mapNotNull null
                val items = extractArrayItems(arrayNode)
                name to ResourceEntry.StringArrayEntry(name, items)
            }
            .toMap()
    }

    /**
     * Extracts items from a string-array node.
     */
    private fun extractArrayItems(arrayNode: Node): List<String> {
        return arrayNode.childNodes
            .asSequence()
            .filter { it.nodeName == "item" }
            .map { it.textContent ?: "" }
            .toList()
    }

    /**
     * Extracts locale from file path (e.g., values-es → es).
     */
    private fun extractLocaleFromPath(file: File): String {
        val parentName = file.parentFile?.name ?: "values"
        return when {
            parentName == "values" -> "default"
            parentName.startsWith("values-") -> parentName.removePrefix("values-")
            else -> "unknown"
        }
    }

    /**
     * Extension to convert NodeList to Sequence for functional operations.
     */
    private fun NodeList.asSequence(): Sequence<Node> {
        return (0 until length).asSequence().map { item(it) }
    }

    /**
     * Extension to safely get named attribute from a node.
     */
    private fun Node.getAttribute(name: String): String? {
        return attributes?.getNamedItem(name)?.nodeValue
    }
}
