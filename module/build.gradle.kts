import org.apache.tools.ant.filters.FixCrLfFilter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.Locale

plugins {
    id("com.android.application")
}

val verCode = providers.exec {
    commandLine("git", "rev-list", "--count", "HEAD")
}.standardOutput.asText.get().trim().toInt()

val verName = providers.exec {
    commandLine("git", "describe", "--tags", "--always", "--dirty")
}.standardOutput.asText.get().trim()

android {
    namespace = "zygisk.cacerts"
    defaultConfig {
        versionCode = verCode
        versionName = verName
        externalNativeBuild {
            ndkBuild {
                arguments += "-j${Runtime.getRuntime().availableProcessors()}"
            }
        }
        ndk {
            abiFilters += listOf("x86", "x86_64", "armeabi-v7a", "arm64-v8a", "riscv64")
            debugSymbolLevel = "FULL"
        }
    }
    externalNativeBuild {
        ndkBuild {
            path("jni/Android.mk")
        }
    }
    lint {
        checkReleaseBuilds = false
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

val zipAll = tasks.register<Task>("zipAll") {
    group = rootProject.name
}

androidComponents.onVariants { variant ->
    val variantCapped = variant.name.replaceFirstChar { it.titlecase(Locale.ROOT) }
    val variantLowered = variant.name.lowercase(Locale.ROOT)

    val magiskDir = layout.buildDirectory.dir("magisk/$variantLowered")

    val prepareMagiskFilesTask = tasks.register<Sync>("prepareMagiskFiles$variantCapped") {
        group = rootProject.name
        inputs.property("versionName", verName)
        inputs.property("versionCode", verCode)
        dependsOn("assemble$variantCapped")
        into(magiskDir)
        from("${rootProject.projectDir}/README.md")
        from("$projectDir/magisk_module") {
            exclude("module.prop")
        }
        from("$projectDir/magisk_module") {
            include("module.prop")
            expand(
                "versionName" to verName,
                "versionCode" to verCode,
            )
            filteringCharset = "UTF-8"

            filter<FixCrLfFilter>("eol" to FixCrLfFilter.CrLf.newInstance("lf"))
        }
        into("lib") {
            val path = "intermediates/stripped_native_libs/$variantLowered/" +
                    "strip${variantCapped}DebugSymbols/out/lib"
            from(layout.buildDirectory.dir(path))
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

    val zipTask = tasks.register<Zip>("zip${variantCapped}") {
        group = rootProject.name
        dependsOn(prepareMagiskFilesTask)
        archiveBaseName.set(rootProject.name)
        archiveVersion.set(verName)
        archiveClassifier.set(variantLowered)
        archiveExtension.set("zip")
        destinationDirectory.set(file("$projectDir/release"))
        setMetadataCharset("UTF-8")
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        from(magiskDir)
    }

    zipAll.configure {
        dependsOn(zipTask)
    }
}
