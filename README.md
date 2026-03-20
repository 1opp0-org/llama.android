# Overview

This is a library based off the android demo in the llama.cpp repository.

Its current state is proof of concept of an android library capable of running LLM models in GGUF format on mobile android CPUs. It also has disabled KLEIDIAI, which is a kernel optimized for Arm.

# Usage

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

Check on https://jitpack.io/#1opp0-org/llama.android/ for latest version.