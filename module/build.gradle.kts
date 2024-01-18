/*
 * This file is part of LSPosed.
 *
 * LSPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2024 LSPosed Contributors
 */

import com.android.build.api.component.analytics.AnalyticsEnabledApplicationVariant
import com.android.build.api.variant.impl.ApplicationVariantImpl
import org.apache.commons.codec.binary.Hex
import org.apache.tools.ant.filters.FixCrLfFilter
import org.apache.tools.ant.filters.ReplaceTokens
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.Locale
import java.util.TreeSet

plugins {
    id("com.android.application")
}

val verCode: Int by rootProject.extra
val verName: String by rootProject.extra

android {
    namespace = "zygisk.cacerts"

    defaultConfig {
        externalNativeBuild {
            ndkBuild {
                arguments += "-j${Runtime.getRuntime().availableProcessors()}"
            }
        }
        ndk {
            debugSymbolLevel = "FULL"
            jobs = Runtime.getRuntime().availableProcessors()
        }
    }

    externalNativeBuild {
        ndkBuild {
            path("jni/Android.mk")
        }
    }
}

val zipAll = task("zipAll", Task::class) {
    group = rootProject.name
}

androidComponents.onVariants { v ->
    val variant: ApplicationVariantImpl =
        if (v is ApplicationVariantImpl) v
        else (v as AnalyticsEnabledApplicationVariant).delegate as ApplicationVariantImpl

    val variantCapped = variant.name.replaceFirstChar { it.titlecase(Locale.getDefault()) }
    val variantLowered = variant.name.lowercase(Locale.getDefault())
    val buildTypeLowered = variant.buildType!!.lowercase(Locale.getDefault())

    val magiskDir = layout.buildDirectory.dir("magisk/$variantLowered")

    val zipFileName = "zygisk_cacerts-v$verName-$verCode-$buildTypeLowered.zip"

    val prepareMagiskFilesTask = task("prepareMagiskFiles$variantCapped", Sync::class) {
        group = rootProject.name
        dependsOn("assemble$variantCapped")
        into(magiskDir)
        from("${rootProject.projectDir}/README.md")
        from("$projectDir/magisk_module") {
            exclude("module.prop")
        }
        from("$projectDir/magisk_module") {
            include("module.prop")
            expand(
                "versionName" to "v$verName",
                "versionCode" to verCode,
            )
            filteringCharset = "UTF-8"

            filter<FixCrLfFilter>("eol" to FixCrLfFilter.CrLf.newInstance("lf"))
        }
        into("lib") {
            from(layout.buildDirectory.dir("intermediates/stripped_native_libs/$variantLowered/out/lib"))
        }
        doLast {
            magiskDir.get().dir("zygisk").asFile.mkdir()
            magiskDir.get().dir("lib").asFileTree.visit {
                if (!isDirectory) return@visit
                val srcPath = Paths.get("${file.absolutePath}/libvvb2060.so")
                val dstPath = magiskDir.get().file("zygisk/$path.so").asFile.toPath()
                Files.move(srcPath, dstPath)
            }
            magiskDir.get().dir("lib").asFile.deleteRecursively()
        }
    }

    val zipTask = task("zip${variantCapped}", Zip::class) {
        group = rootProject.name
        dependsOn(prepareMagiskFilesTask)
        archiveFileName.set(zipFileName)
        destinationDirectory.set(file("$projectDir/release"))
        setMetadataCharset("UTF-8")
        isPreserveFileTimestamps = false
        from(magiskDir)
    }

    zipAll.dependsOn(zipTask)
}
