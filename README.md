# Overview

This is a library based off the android demo in the llama.cpp repository.

Its current state is proof of concept of an android library capable of running LLM models in GGUF format on mobile android CPUs. 

Although Llama.cpp suppports vulkan, this version of this library does not compile against vulkan. It means that there's no GPU suppport for Android.


## Attribution

This library is an abstraction layer on top of llama.cpp https://github.com/ggml-org/llama.cpp, which is the library behind ollama https://github.com/ollama/ollama.

When you import its `llama.android-core` you are bringing in llama.cpp code, which is a git submodule of this repository.

## Alternative repos:

This repository is available at:

- https://codeberg.org/fmatos/llama.android

It is also available at:

- https://github.com/1opp0-org/llama.android

# Usage

This library is published on Jitpack: https://jitpack.io/#1opp0-org/llama.android

## Set up your application manifest

To use this library you must enable the following in your application module's manifest:

```xml
<application
...
    android:extractNativeLibs="true"
...
    >
```

If you are writing instrumented tests in a library, the solution above may not work. Use this block:

```kotlin
android {
    packaging {
        jniLibs {
            useLegacyPackaging = true
    ...
```

## Add dependency
In your settings.gradle.kts, do

```kotlin
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url = uri("https://jitpack.io") } // be nice to jitpack and keep this last
		}
	}
```

Then in the module where you want to use the library, add the dependency:

```kotlin
dependencies {
    implementation("com.github.1opp0-org:llama.android:<version>")
}

```

Latest version is [![Release](https://jitpack.io/v/1opp0-org/llama.android.svg)](https://jitpack.io/#1opp0-org/llama.android/)

# Local build

By default it builds Llama.cpp with the flag LLAMA_LLGUIDANCE on. This requires Rust. 

## How to set up rust

You can also read `jitpack_rustup.sh` in this repo and adapt something similar to your needs.

Once Rust and Cargo are installed with all details set up, Gradle needs only 2 variables set:
- RUSTUP_HOME
- CARGO_HOME

Verified with Rust 1.94.1.

## How to disable LLGuidance

`gradle.properties` has `llama.android.llguidance=true`. You can override that value with command line parameter `-Pllama.android.llguidance=false`
