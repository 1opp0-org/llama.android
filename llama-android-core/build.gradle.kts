import java.net.URI

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id(libs.plugins.maven.publish.get().pluginId)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = libs.versions.project.group.get()
            artifactId = "core"
            version = libs.versions.project.version.get()

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "LocalRepo"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

android {
    namespace = libs.versions.project.group.get() + ".core"
    compileSdk = libs.versions.compileSdk.get().toInt()
    ndkVersion = libs.versions.ndkVersion.get()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
        externalNativeBuild {
            cmake {
                val llGuidanceEnabled = project.findProperty("llama.android.llguidance")
                    ?.toString()?.toBoolean() ?: false

                arguments += "-DBUILD_SHARED_LIBS=ON"
                arguments += "-DLLAMA_BUILD_COMMON=ON"
                arguments += "-DLLAMA_OPENSSL=OFF"
                arguments += "-DGGML_NATIVE=OFF"

                arguments += "-DGGML_BACKEND_DL=ON"

                if (llGuidanceEnabled) {
                    arguments += "-DLLAMA_LLGUIDANCE=ON"

                    val rustupHome = System.getenv("RUSTUP_HOME")
                    val cargoHome = System.getenv("CARGO_HOME")
                    if ( rustupHome == null || cargoHome == null ) {
                        throw RuntimeException("RUSTUP_HOME and/or CARGO_HOME environment variable are not set. These are required to build LLGuidance support with Rust. You can also disable LLGuidance by editing gradle.properties and setting llama.android.llguidance=false.")
                    }
                }
            }
        }
        aarMetadata {
            minCompileSdk = libs.versions.minSdk.get().toInt()
        }
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = libs.versions.cmakeVersion.get()
        }
    }
    buildTypes {
        release {
            externalNativeBuild {
                cmake {
                    arguments += "-DCMAKE_BUILD_TYPE=Release"

                    arguments += "-DCMAKE_MESSAGE_LOG_LEVEL=INFO"
                    arguments += "-DCMAKE_VERBOSE_MAKEFILE=OFF"

                    arguments += "-DGGML_CPU_ALL_VARIANTS=ON"
                    arguments += "-DGGML_LLAMAFILE=ON"
                    arguments += "-DGGML_CPU_KLEIDIAI=ON"
                }
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }
        debug {

            externalNativeBuild {
                cmake {
                    // compile time for Release vs Debug is only a bit more,
                    // but run time is MUCH faster
                    arguments += "-DCMAKE_BUILD_TYPE=Release"

                    arguments += "-DCMAKE_MESSAGE_LOG_LEVEL=DEBUG"
                    arguments += "-DCMAKE_VERBOSE_MAKEFILE=ON"

                    arguments += "-DGGML_CPU_ALL_VARIANTS=OFF"
                    arguments += "-DGGML_LLAMAFILE=OFF"
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)

        compileOptions {
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    packaging {
        jniLibs {
            // this has impact on instrumented tests but not on modules that depend on this
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.timber)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)

    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}

tasks.register("downloadTestModel") {

    val modelQwen_0_5B_Url = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"

    val modelUrl = modelQwen_0_5B_Url

    val outputDir = file("src/androidTest/assets/")
    val outputFile = File(outputDir, "test_model.gguf")

    outputs.file(outputFile)

    doLast {
        if (!outputDir.exists()) outputDir.mkdirs()
        if (!outputFile.exists()) {
            println("Downloading test model from $modelUrl...")
            URI(modelUrl).toURL().openStream().use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            println("Download complete: ${outputFile.absolutePath}")
        } else {
            println("Test model already exists, skipping download: ${outputFile.path} - ${outputFile.length() / 1024 / 1024} MB")
        }
    }
}

// Ensure the model is downloaded before building the instrumented test
tasks.matching {
    it.name.contains("connectedDebugAndroidTest") ||
            it.name.contains("packageDebugAndroidTest") ||
            (it.name.startsWith("merge") && it.name.endsWith("AndroidTestAssets")) ||
            (it.name.startsWith("process") && it.name.endsWith("Resources")) ||
            (it.name.startsWith("generate") && it.name.endsWith("TestResources"))

}.all {
    dependsOn("downloadTestModel")
}

tasks.named<Delete>("clean") {
    delete(file("src/androidTest/assets/test_model.gguf"))

    delete(".cxx")
}
