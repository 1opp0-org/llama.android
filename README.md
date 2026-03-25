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

In your settings.gradle.kts, do

```kotlin
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url = uri("https://jitpack.io") }
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
