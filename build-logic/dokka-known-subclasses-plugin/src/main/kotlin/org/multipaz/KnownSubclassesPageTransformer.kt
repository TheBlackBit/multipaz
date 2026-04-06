package org.multipaz

import org.jetbrains.dokka.DokkaConfiguration.DokkaSourceSet
import org.jetbrains.dokka.base.transformers.documentables.InheritorsInfo
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DAnnotation
import org.jetbrains.dokka.model.DClass
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DInterface
import org.jetbrains.dokka.model.DModule
import org.jetbrains.dokka.model.DObject
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.model.SourceSetDependent
import org.jetbrains.dokka.model.TypeConstructorWithKind
import org.jetbrains.dokka.plugability.DokkaContext
import org.jetbrains.dokka.transformers.documentation.DocumentableTransformer

/**
 * A [DocumentableTransformer] that runs after Dokka's built-in [InheritorsExtractorTransformer]
 * and augments the [InheritorsInfo] on each documentable with **transitive** inheritors.
 *
 * If class A extends B extends C, Dokka's built-in transformer only adds B as an inheritor of C
 * and A as an inheritor of B. This transformer ensures C also lists A.
 */
class TransitiveInheritorsTransformer : DocumentableTransformer {

    override fun invoke(original: DModule, context: DokkaContext): DModule {
        val directInheritors = mutableMapOf<DRI, MutableMap<DokkaSourceSet, MutableList<DRI>>>()
        collectSupertypeRelationships(original, directInheritors)

        if (directInheritors.isEmpty()) return original

        val transitiveInheritors = computeTransitiveClosure(directInheritors)

        return original.updateInheritors(transitiveInheritors)
    }

    private fun collectSupertypeRelationships(
        documentable: Documentable,
        result: MutableMap<DRI, MutableMap<DokkaSourceSet, MutableList<DRI>>>
    ) {
        val supertypes: SourceSetDependent<List<TypeConstructorWithKind>> = when (documentable) {
            is DClass, is DInterface, is DObject, is DEnum -> documentable.supertypes
            else -> emptyMap()
        }

        for ((sourceSet, typeConstructors) in supertypes) {
            for (tc in typeConstructors) {
                val parentDri = tc.typeConstructor.dri
                if (parentDri.packageName == "kotlin" && parentDri.classNames == "Any") continue
                if (parentDri.packageName == "java.lang" && parentDri.classNames == "Object") continue

                result
                    .getOrPut(parentDri) { mutableMapOf() }
                    .getOrPut(sourceSet) { mutableListOf() }
                    .add(documentable.dri)
            }
        }

        documentable.children.forEach { collectSupertypeRelationships(it, result) }
    }

    private fun computeTransitiveClosure(
        directInheritors: Map<DRI, Map<DokkaSourceSet, List<DRI>>>
    ): Map<DRI, Map<DokkaSourceSet, List<DRI>>> {
        val allSourceSets = collectSourceSets(directInheritors)

        val result = mutableMapOf<DRI, MutableMap<DokkaSourceSet, MutableSet<DRI>>>()

        for (sourceSet in allSourceSets) {
            val adjacency = buildAdjacency(directInheritors, sourceSet)
            val closureForSourceSet = computeClosureForSourceSet(adjacency)

            for ((parent, descendants) in closureForSourceSet) {
                result
                    .getOrPut(parent) { mutableMapOf() }
                    .getOrPut(sourceSet) { mutableSetOf() }
                    .addAll(descendants)
            }
        }

        return result.mapValues { (_, ssMap) ->
            ssMap.mapValues { (_, dris) -> dris.toList() }
        }
    }

    private fun collectSourceSets(
        directInheritors: Map<DRI, Map<DokkaSourceSet, List<DRI>>>
    ): Set<DokkaSourceSet> =
        directInheritors.values.flatMap { it.keys }.toSet()

    private fun buildAdjacency(
        directInheritors: Map<DRI, Map<DokkaSourceSet, List<DRI>>>,
        sourceSet: DokkaSourceSet
    ): Map<DRI, Set<DRI>> {
        val adjacency = mutableMapOf<DRI, MutableSet<DRI>>()
        for ((parent, ssMap) in directInheritors) {
            val children = ssMap[sourceSet] ?: continue
            adjacency.getOrPut(parent) { mutableSetOf() }.addAll(children)
        }
        return adjacency
    }

    private fun computeClosureForSourceSet(
        adjacency: Map<DRI, Set<DRI>>
    ): Map<DRI, Set<DRI>> {
        val result = mutableMapOf<DRI, Set<DRI>>()

        for (parent in adjacency.keys) {
            val allDescendants = mutableSetOf<DRI>()
            val queue = ArrayDeque<DRI>()
            queue.addAll(adjacency[parent] ?: emptySet())

            while (queue.isNotEmpty()) {
                val child = queue.removeFirst()
                if (!allDescendants.add(child)) continue
                adjacency[child]?.let { queue.addAll(it) }
            }

            result[parent] = allDescendants
        }

        return result
    }

    private fun DModule.updateInheritors(
        transitiveMap: Map<DRI, Map<DokkaSourceSet, List<DRI>>>
    ): DModule = copy(
        packages = packages.map { it.updateInheritors(transitiveMap) }
    )

    private fun DPackage.updateInheritors(
        transitiveMap: Map<DRI, Map<DokkaSourceSet, List<DRI>>>
    ): DPackage = copy(
        classlikes = classlikes.map { it.updateInheritors(transitiveMap) }
    )

    @Suppress("UNCHECKED_CAST")
    private fun <T : DClasslike> T.updateInheritors(
        transitiveMap: Map<DRI, Map<DokkaSourceSet, List<DRI>>>
    ): T {
        val transitiveForThis = transitiveMap[dri]

        val updatedClasslikes = when (this) {
            is DClass, is DInterface, is DObject, is DEnum, is DAnnotation -> classlikes
            else -> emptyList()
        }.map { it.updateInheritors(transitiveMap) }

        val newInfo = transitiveForThis?.let { InheritorsInfo(it) }

        return when (this) {
            is DClass -> copy(
                extra = if (newInfo != null) extra + newInfo else extra,
                classlikes = updatedClasslikes
            )

            is DInterface -> copy(
                extra = if (newInfo != null) extra + newInfo else extra,
                classlikes = updatedClasslikes
            )

            is DEnum -> copy(
                extra = if (newInfo != null) extra + newInfo else extra,
                classlikes = updatedClasslikes
            )

            is DObject -> copy(classlikes = updatedClasslikes)
            is DAnnotation -> copy(classlikes = updatedClasslikes)
            else -> this
        } as T
    }
}
