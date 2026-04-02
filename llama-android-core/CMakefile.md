# CMake Architecture: `llama-android-core`

This document explains how the Android-specific build system manages the C++ core and its interaction with the `llama.cpp` and `llguidance` submodules.

## 1. Standard CMake Components

These are the standard "building blocks" used to configure a modern C++ project for Android.

### Project and Standards
*   `cmake_minimum_required`: Ensures the build environment uses a compatible version of CMake.
*   `project("ai-chat" ...)`: Defines the name of the project and the languages used (C and CXX).
*   `set(CMAKE_C_STANDARD 11)` and `set(CMAKE_CXX_STANDARD 17)`: Enforces the C++ language standards required for compilation.

### Library Definition
*   `add_library(ai-chat SHARED ...)`: Defines the main JNI library for the Android app. It's built as a `SHARED` library so the JVM can load it.
*   `target_include_directories`: Tells the compiler where to find header files (`.h`, `.hpp`).
*   `target_link_libraries`: Tells the linker which other libraries to merge into our final binary (e.g., `llama`, `common`, `android`, `log`).

### Sub-projects
*   `add_subdirectory(${LLAMA_SRC} build-llama)`: This is the standard way to include an external project like `llama.cpp`. CMake enters that directory, processes its `CMakeLists.txt`, and makes its targets (like the `llama` library) available to us.

---

## 2. Non-Standard (Advanced) Components

To support modern features like `llguidance` (Rust) on Android without modifying the upstream `llama.cpp` code, we use several advanced "Hijacking" techniques.

### The "Interceptor" Pattern (Function Overriding)
We need to change how `llama.cpp` builds its Rust components, but we are not allowed to edit the submodule files. We achieve this by **overriding** the built-in CMake function `ExternalProject_Add`.

1.  **Macro Rename:** We use `include(ExternalProject)` to load the real function, but then we define our own `function(ExternalProject_Add)`.
2.  **Intercept:** When the submodule calls `ExternalProject_Add(llguidance_ext ...)`, our version of the function runs instead of the real one.
3.  **Argument Filtering:** We loop through the arguments provided by the submodule and surgically remove their original `BUILD_COMMAND`, `CONFIGURE_COMMAND`, and `INSTALL_COMMAND`.
4.  **Injection:** We call the real internal function (`_ExternalProject_Add`) but pass our own Android-specific commands instead of the originals.

### Verbatim Build Wrapper (`cargo-hijack.sh`)
Rust's `cargo` doesn't automatically know how to use the Android NDK (Network Development Kit) linkers.
*   We use CMake's `file(WRITE ...)` to generate a shell script during the configuration phase.
*   This script exports the `CARGO_TARGET_..._LINKER` and `AR` environment variables pointing to the specific Android NDK binaries.
*   It then runs `cargo build --target=${RUST_NDK_TRIPLE}`.
*   Finally, it relocates the compiled `.a` and `.h` files to the `target/release/` folder where the rest of the build system expects to find them.

### Cross-Compilation logic
*   **ABI Detection:** We detect if the target is `arm64-v8a` or `x86_64` and map it to the correct "Rust Triple" (e.g., `aarch64-linux-android`).
*   **Linker Resolution:** We use `get_filename_component` on the C compiler to find the exact NDK folder and resolve the path to the target-specific `clang` used for linking the Rust code.

## 3. Diagnostic Logging
Because Android builds involve many layers (Gradle -> CMake -> Ninja -> Cargo), we use a sequential logging system (`[Step 01]` to `[Step 100]`) that writes directly to `cargo_hijack.log` in the project root. This allows developers to see exactly how far the configuration and build phases progressed.
