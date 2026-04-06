package org.multipaz

import org.jetbrains.dokka.base.testApi.testRunner.BaseAbstractTest

class KnownSubclassesPluginTest : BaseAbstractTest() {
    private val configuration = dokkaConfiguration {
        sourceSets {
            sourceSet {
                sourceRoots = listOf("src/main/kotlin")
            }
        }
    }
}