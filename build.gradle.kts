import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.internal.HasConvention
import org.gradle.api.tasks.SourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.gradle.api.JavaVersion.VERSION_1_8
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.jvm.tasks.Jar
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path
import kotlin.concurrent.thread

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}


val CI = System.getenv("CI") != null

val channel = prop("publishChannel")

plugins {
    idea
    kotlin("jvm") version "1.2.21"
    id("org.jetbrains.intellij") version "0.2.18"
}

idea {
    module {
        // https://github.com/gradle/kotlin-dsl/issues/537/
        excludeDirs = excludeDirs + file("testData") + file("deps")
    }
}

allprojects {
    apply {
        plugin("idea")
        plugin("kotlin")
        plugin("org.jetbrains.intellij")
    }

    repositories {
        mavenCentral()
        jcenter()
    }

    idea {
        module {
            generatedSourceDirs.add(file("src/gen"))
        }
    }

    intellij {
        version = prop("ideaVersion")
        downloadSources = !CI
        updateSinceUntilBuild = false
        instrumentCode = false
        ideaDependencyCachePath = file("deps").absolutePath
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            languageVersion = "1.2"
            apiVersion = "1.2"
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = VERSION_1_8
        targetCompatibility = VERSION_1_8
    }

    java.sourceSets {
        getByName("main").java.srcDirs("src/gen")
    }
}

val versionSuffix = if (channel.isBlank()) "" else "-$channel"

project(":") {
    version = "0.1"
    intellij {
        pluginName = "intellij-lsp-server"
    }

    dependencies {
        compile("org.jetbrains.kotlin:kotlin-reflect:1.2.21")
        compile("org.eclipse.lsp4j:org.eclipse.lsp4j:0.4.0.M6")
    }

    tasks.withType<Test> {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }

        // Prevent "File access outside allowed roots" in multi module tests, because modules each have an .iml
        environment("NO_FS_ROOTS_ACCESS_CHECK", "1")
    }

    task("resolveDependencies") {
        doLast {
            rootProject.allprojects
                .map { it.configurations }
                .flatMap { listOf(it.compile, it.testCompile) }
                .forEach { it.resolve() }
        }
    }
}

fun prop(name: String): String =
    extra.properties[name] as? String
        ?: error("Property `$name` is not defined in gradle.properties")


inline operator fun <T : Task> T.invoke(a: T.() -> Unit): T = apply(a)

val SourceSet.kotlin: SourceDirectorySet
    get() =
        (this as HasConvention)
            .convention
            .getPlugin(KotlinSourceSet::class.java)
            .kotlin


fun SourceSet.kotlin(action: SourceDirectorySet.() -> Unit) =
    kotlin.action()


fun String.execute(wd: String? = null, ignoreExitCode: Boolean = false): String =
    split(" ").execute(wd, ignoreExitCode)

fun List<String>.execute(wd: String? = null, ignoreExitCode: Boolean = false): String {
    val process = ProcessBuilder(this)
        .also { pb -> wd?.let { pb.directory(File(it)) } }
        .start()
    var result = ""
    val errReader = thread { process.errorStream.bufferedReader().forEachLine { println(it) } }
    val outReader = thread {
        process.inputStream.bufferedReader().forEachLine { line ->
            println(line)
            result += line
        }
    }
    process.waitFor()
    outReader.join()
    errReader.join()
    if (process.exitValue() != 0 && !ignoreExitCode) error("Non-zero exit status for `$this`")
    return result
}
