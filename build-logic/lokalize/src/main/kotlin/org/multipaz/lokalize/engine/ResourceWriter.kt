package org.multipaz.lokalize.engine

import org.multipaz.lokalize.model.LocaleBundle
import org.multipaz.lokalize.model.ResourceEntry
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Writes and updates Android string resource XML files.
 * Handles strings, plurals, and string-arrays.
 */
class ResourceWriter {

    /**
     * Merges a partial LocaleBundle (missing translations) into an existing resource file.
     * Creates the file if it doesn't exist.
     */
    fun mergeBundle(
        file: File,
        bundle: LocaleBundle,
        preserveExisting: Boolean = true
    ) {
        file.parentFile?.mkdirs()

        val document = loadOrCreateDocument(file)
        val root = document.documentElement
            ?: throw IllegalStateException("Invalid XML: no root element")

        val existingKeys = if (preserveExisting) {
            ExistingKeys.from(root)
        } else {
            ExistingKeys.empty()
        }

        val entriesToAdd = BundleEntries.from(bundle, existingKeys)
        entriesToAdd.setExistingKeys(existingKeys)
        entriesToAdd.appendTo(document, root)

        // Merge quantities into existing plurals
        entriesToAdd.mergePluralsIntoExisting(document, root, existingKeys)

        writeDocument(document, file)
    }

    /**
     * Loads an existing XML document or creates a new one.
     */
    private fun loadOrCreateDocument(file: File): Document {
        return if (file.exists()) {
            DocumentBuilderFactory.newInstance()
                .apply { isNamespaceAware = true }
                .newDocumentBuilder()
                .parse(file)
        } else {
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument()
                .apply { appendChild(createElement("resources")) }
        }
    }

    /**
     * Container for existing resource keys to avoid duplicates.
     * For plurals, tracks which quantities are already defined.
     */
    private data class ExistingKeys(
        val strings: Set<String>,
        val plurals: Map<String, Set<String>>, // plural name -> set of quantities
        val arrays: Set<String>
    ) {
        companion object {
            fun empty() = ExistingKeys(emptySet(), emptyMap(), emptySet())

            fun from(root: Element): ExistingKeys {
                return ExistingKeys(
                    strings = extractKeysFrom(root, "string"),
                    plurals = extractPluralQuantitiesFrom(root),
                    arrays = extractKeysFrom(root, "string-array")
                )
            }

            private fun extractKeysFrom(element: Element, tagName: String): Set<String> {
                return element.getElementsByTagName(tagName)
                    .asNodeSequence()
                    .mapNotNull { it.attributes?.getNamedItem("name")?.nodeValue }
                    .toSet()
            }

            private fun extractPluralQuantitiesFrom(root: Element): Map<String, Set<String>> {
                return root.getElementsByTagName("plurals")
                    .asNodeSequence()
                    .mapNotNull { pluralNode ->
                        val name = pluralNode.attributes?.getNamedItem("name")?.nodeValue
                            ?: return@mapNotNull null
                        val quantities = pluralNode.childNodes
                            .asNodeSequence()
                            .filter { it.nodeName == "item" }
                            .mapNotNull { it.attributes?.getNamedItem("quantity")?.nodeValue }
                            .toSet()
                        name to quantities
                    }
                    .toMap()
            }

            private fun NodeList.asNodeSequence(): Sequence<org.w3c.dom.Node> {
                return (0 until length).asSequence().map { item(it) }
            }
        }

        private fun org.w3c.dom.NodeList.asNodeSequence(): Sequence<org.w3c.dom.Node> {
            return (0 until length).asSequence().map { item(it) }
        }
    }

    /**
     * Container for entries that need to be added.
     */
    private data class BundleEntries(
        val strings: List<ResourceEntry.StringEntry>,
        val plurals: List<ResourceEntry.PluralEntry>,
        val arrays: List<ResourceEntry.StringArrayEntry>
    ) {
        companion object {
            fun from(bundle: LocaleBundle, existing: ExistingKeys): BundleEntries {
                return BundleEntries(
                    strings = bundle.strings.values
                        .filter { it.key !in existing.strings },
                    // Include plurals that either don't exist OR have missing quantities
                    plurals = bundle.plurals.values
                        .filter { entry ->
                            val existingQuantities = existing.plurals[entry.key]
                            if (existingQuantities == null) {
                                // Plural doesn't exist at all - include it
                                true
                            } else {
                                // Check if there are missing quantities
                                entry.items.keys.any { it !in existingQuantities }
                            }
                        },
                    arrays = bundle.arrays.values
                        .filter { it.key !in existing.arrays }
                )
            }
        }

        fun appendTo(document: Document, root: Element) {
            strings.forEach { entry ->
                createStringElement(document, entry).appendTo(root)
            }
            // Only create new plural elements for plurals that don't exist yet
            plurals.filter { it.key !in existingKeys?.plurals?.keys ?: emptySet() }
                .forEach { entry ->
                    createPluralElement(document, entry).appendTo(root)
                }
            arrays.forEach { entry ->
                createArrayElement(document, entry).appendTo(root)
            }
        }

        /**
         * Merges quantities from bundle plurals into existing plural elements in the XML.
         * This handles the case where a plural exists but is missing some quantities.
         */
        fun mergePluralsIntoExisting(document: Document, root: Element, existing: ExistingKeys) {
            plurals.filter { it.key in existing.plurals.keys }
                .forEach { pluralEntry ->
                    val existingQuantities = existing.plurals[pluralEntry.key] ?: emptySet()
                    val missingQuantities = pluralEntry.items.keys - existingQuantities

                    if (missingQuantities.isNotEmpty()) {
                        // Find the existing plural element
                        val pluralElement = root.getElementsByTagName("plurals")
                            .asNodeSequence()
                            .firstOrNull { it.attributes?.getNamedItem("name")?.nodeValue == pluralEntry.key }
                            as? Element

                        pluralElement?.let { element ->
                            // Add missing quantities to the existing plural
                            ResourceEntry.PluralEntry.QUANTITIES
                                .filter { it in missingQuantities }
                                .forEach { quantity ->
                                    element.appendChild(document.createElement("item").apply {
                                        setAttribute("quantity", quantity)
                                        textContent = pluralEntry.items[quantity]
                                    })
                                }
                        }
                    }
                }
        }

        private var existingKeys: ExistingKeys? = null

        fun setExistingKeys(keys: ExistingKeys) {
            this.existingKeys = keys
        }

        private fun createStringElement(document: Document, entry: ResourceEntry.StringEntry): Element {
            return document.createElement("string").apply {
                setAttribute("name", entry.key)
                textContent = entry.value
            }
        }

        private fun createPluralElement(document: Document, entry: ResourceEntry.PluralEntry): Element {
            return document.createElement("plurals").apply {
                setAttribute("name", entry.key)
                ResourceEntry.PluralEntry.QUANTITIES
                    .filter { it in entry.items }
                    .forEach { quantity ->
                        appendChild(document.createElement("item").apply {
                            setAttribute("quantity", quantity)
                            textContent = entry.items[quantity]
                        })
                    }
            }
        }

        private fun createArrayElement(document: Document, entry: ResourceEntry.StringArrayEntry): Element {
            return document.createElement("string-array").apply {
                setAttribute("name", entry.key)
                entry.items.forEach { value ->
                    appendChild(document.createElement("item").apply {
                        textContent = value
                    })
                }
            }
        }

        private fun Element.appendTo(parent: Element) {
            parent.appendChild(this)
        }

        private fun NodeList.asNodeSequence(): Sequence<org.w3c.dom.Node> {
            return (0 until length).asSequence().map { item(it) }
        }
    }

    /**
     * Writes the document to file with proper formatting.
     * Normalizes whitespace to prevent accumulating blank lines on repeated saves.
     */
    private fun writeDocument(document: Document, file: File) {
        // Normalize the document to remove accumulated whitespace
        normalizeWhitespace(document.documentElement)

        TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            transform(DOMSource(document), StreamResult(file))
        }
    }

    /**
     * Normalizes whitespace in the XML document by removing text nodes
     * that contain only whitespace between elements. This prevents
     * accumulating blank lines on repeated read/write cycles.
     */
    private fun normalizeWhitespace(element: org.w3c.dom.Element) {
        val children = element.childNodes
        val toRemove = mutableListOf<org.w3c.dom.Node>()

        for (i in 0 until children.length) {
            val child = children.item(i)
            when (child.nodeType) {
                org.w3c.dom.Node.TEXT_NODE -> {
                    // Remove text nodes that are only whitespace
                    if (child.textContent?.trim()?.isEmpty() == true) {
                        toRemove.add(child)
                    }
                }
                org.w3c.dom.Node.ELEMENT_NODE -> {
                    // Recursively normalize child elements
                    normalizeWhitespace(child as org.w3c.dom.Element)
                }
            }
        }

        // Remove the whitespace-only text nodes
        toRemove.forEach { element.removeChild(it) }
    }
}

private fun NodeList.asNodeSequence(): Sequence<org.w3c.dom.Node> {
    return (0 until length).asSequence().map { item(it) }
}
