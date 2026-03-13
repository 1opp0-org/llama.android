# Tech Stack

- **Language:** Kotlin (Core/API), C++ (JNI only)
  - original C++ code from llama.cpp is a submodule, but none of it is written as part of this project; although it's visible throug gradle/ndk/cmake which compiles `.so` files out of llama.cpp
- Sample app in kotlin and Jetpack compose
- **Build System:** Gradle (Kotlin DSL)
- **Native Build:** CMake
  - **Inference Engine:** `llama.cpp` (Submodule, treated as read-only)
- **Dependency Management:** Gradle Version Catalog (`libs.versions.toml`)
- published to jitpack
- CI on github actions

# Project overview

- llama.cpp folder is a git submodule pointing to https://github.com/ggml-org/llama.cpp
- `llama.android-core` (will) contain the core library logic. 
  - 100% kotlin 
  - no internet use and no ui code
- `llama.android-jni` (current `llama.android`) contains the JNI glue code between the core library and `llama.cpp`. No actual logic in it.
- `sample-app` is a demonstration of how to use the library.
  - jetpack compose
  - ktor for networking (downloading models)