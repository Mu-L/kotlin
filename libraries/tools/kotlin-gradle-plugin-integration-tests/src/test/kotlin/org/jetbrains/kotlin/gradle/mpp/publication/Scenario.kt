/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.mpp.publication

import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.testbase.TestVersions
import org.jetbrains.kotlin.tooling.core.KotlinToolingVersion

private val agpSupportedGradleVersions = TestVersions.AgpCompatibilityMatrix.entries.associateBy { it.version }

private infix operator fun TestVersions.AgpCompatibilityMatrix.contains(gradleVersion: GradleVersion): Boolean =
    gradleVersion in minSupportedGradleVersion..maxSupportedGradleVersion


data class Scenario(
    val consumer: Project,
    val producer: Project,
) {
    data class Project(
        val variant: ProjectVariant,
        val gradleVersionString: String,
        val kotlinVersionString: String?,
        val agpVersionString: String?,
    ) {
        val gradleVersion = GradleVersion.version(gradleVersionString)
        val kotlinVersion = if (kotlinVersionString != null) KotlinToolingVersion(kotlinVersionString) else null

        val sanitizedKotlinVersionString = if (kotlinVersionString == TestVersions.Kotlin.CURRENT) "snapshot" else kotlinVersionString
        val sanitizedGradleVersionString = if (gradleVersionString == TestVersions.Gradle.MAX_SUPPORTED) "max" else gradleVersionString
        val sanitizedAndroidVersionString = if (agpVersionString == TestVersions.AGP.MAX_SUPPORTED) "max" else agpVersionString

        val isKmp get() = variant is ProjectVariant.Kmp
        val isMasterKmp get() = variant is ProjectVariant.Kmp && kotlinVersionString == TestVersions.Kotlin.CURRENT
        val hasJvm get() = (variant is ProjectVariant.Kmp && variant.withJvm) || (variant === ProjectVariant.JavaOnly)
        val hasAndroid get() = (variant is ProjectVariant.Kmp && variant.withAndroid) || (variant == ProjectVariant.AndroidOnly)

        val hasValidVersionCombo
            get() = when {
                hasAndroid -> gradleVersion in agpSupportedGradleVersions[agpVersionString]!!
                else -> true
            }

        val resolvedConfigurationsNames
            get() = when (variant) {
                ProjectVariant.AndroidOnly -> listOf(
                    "flavor1ReleaseCompileClasspath",
                    "flavor1DebugCompileClasspath"
                )
                ProjectVariant.JavaOnly -> listOf("compileClasspath")
                is ProjectVariant.Kmp -> listOfNotNull(
                    "linuxX64CompileKlibraries",
                    "linuxArm64CompileKlibraries",
                    "jvmCompileClasspath".takeIf { variant.withJvm },
                ) + if (variant.withAndroid) listOf(
                    "androidFlavor1ReleaseCompileClasspath",
                    "androidFlavor1DebugCompileClasspath"
                ) else emptyList()
            }
    }

    val hasMasterKmp get() = producer.isMasterKmp || consumer.isMasterKmp

    val isConsumable: Boolean
        get() = consumer.hasJvm && producer.hasJvm ||
                consumer.hasAndroid && producer.hasAndroid ||
                consumer.isKmp && producer.isKmp

    override fun toString(): String = consumer.id + " consumes " + producer.id
}

fun ScenarioProject(
    variant: ProjectVariant,
    gradleVersion: String,
    kotlinVersion: String,
    agpVersion: String
): Scenario.Project = when (variant) {
    ProjectVariant.AndroidOnly -> Scenario.Project(variant, gradleVersion, TestVersions.Kotlin.CURRENT, agpVersion)
    ProjectVariant.JavaOnly -> Scenario.Project(variant, gradleVersion, null, null)
    is ProjectVariant.Kmp -> Scenario.Project(variant, gradleVersion, kotlinVersion, agpVersion.takeIf { variant.withAndroid })
}

val Scenario.Project.id
    get() = listOfNotNull(
        "gradle_${sanitizedGradleVersionString.lettersDigitsUnderscores}",
        variant.toString(),
        sanitizedKotlinVersionString?.let { "kgp_$it" },
        sanitizedAndroidVersionString?.let { "agp_$it" },
    ).joinToString("_") { it.lettersDigitsUnderscores }

val Scenario.Project.packageName get() = "sample.gradle_${sanitizedGradleVersionString.lettersDigitsUnderscores}"

val Scenario.Project.artifactName get() = listOfNotNull(
        variant.toString(),
        sanitizedKotlinVersionString?.let { "kgp_$it" },
        sanitizedAndroidVersionString?.let { "agp_$it" },
    ).joinToString("_") { it.lettersDigitsUnderscores }

private val String.lettersDigitsUnderscores get() = map { if (!it.isLetterOrDigit()) "_" else it }.joinToString("")
