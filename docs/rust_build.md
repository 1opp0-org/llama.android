# Android-Rust Build Orchestration: Multi-Layer Technical Reference

This document details the flow of parameters across the four layers of our build pipeline. We use a "Clean Hijack" pattern to cross-compilation Rust components for Android without modifying the `llama.cpp` submodule.

## Build Layer Parameter Flow

| Layer | Source File(s) | Parameters Set (Output) | Parameters Consumed (Input) | Role / Mechanism |
| :--- | :--- | :--- | :--- | :--- |
| **Gradle** | `llama-android-core/build.gradle.kts` | `ANDROID_ABI`, `ANDROID_PLATFORM`, `LLAMA_LLGUIDANCE=[ON\|OFF]` | `libs.versions.toml` | **Orchestrator:** Passes Android environment and project flags to CMake via DSL. |
| **CMake** | `llama-android-core/src/main/cpp/CMakeLists.txt` | `RUST_NDK_TRIPLE`, `ANDROID_NDK_CLANG_PATH`, `HIJACK_SCRIPT` | `ANDROID_ABI`, `CMAKE_C_COMPILER` | **Translator:** Detects ABI, locates NDK toolchain, and overrides `ExternalProject_Add` to intercept submodule calls. |
| **Ninja** | `.cxx/.../build.ninja` | Shell command execution of `${HIJACK_SCRIPT}` | `BUILD_COMMAND` (Intercepted) | **Executor:** Generates specific build rules. Our interceptor ensures Ninja executes the wrapper script instead of raw `cargo`. |
| **Rust/Cargo** | `cargo-hijack.sh` (Generated) | `CARGO_TARGET_<TRIPLE>_LINKER`, `AR`, `--target` | `RUST_NDK_TRIPLE`, `NDK_BIN_DIR` | **Compiler:** Uses NDK-specific environment variables to link Rust artifacts correctly for the Android target. |

## Concrete Path Example (ABI: `arm64-v8a`)

When building for ARM64, the process follows these exact filesystem paths (relative to project root):

1.  **CMake Binary Directory:**
    `llama-android-core/.cxx/Release/3x6t3th6/arm64-v8a/`
2.  **Rust Source Directory (cloned by ExternalProject):**
    `llama-android-core/.cxx/Release/3x6t3th6/arm64-v8a/llguidance/source/`
3.  **Step 1: Cargo Generation Path:**
    `llama-android-core/.cxx/Release/3x6t3th6/arm64-v8a/llguidance/source/target/aarch64-linux-android/release/libllguidance.a`
4.  **Step 2: Relocation (Target) Path:**
    `llama-android-core/.cxx/Release/3x6t3th6/arm64-v8a/llguidance/source/target/release/libllguidance.a`

**Why this works:** The `llama.cpp` C++ linker is hardcoded to look in `target/release/`. By manually moving the file from the triple-specific folder (`aarch64-linux-android`) to the generic `release` folder, we satisfy the submodule's expectations without changing its code.

## Detailed Layer Analysis

### 1. Gradle Layer
Gradle initializes the `externalNativeBuild` which triggers CMake. It passes the current `ANDROID_ABI` (e.g., `arm64-v8a`) and ensures project-wide flags like `LLAMA_LLGUIDANCE` (values `[ON|OFF]`) are defined as CMake cache variables.

### 2. CMake Layer (The Interceptor)
This is the core of our "Clean Hijack" strategy. 
*   **Variable Translation:** Maps `arm64-v8a` to the Rust target triple `aarch64-linux-android`.
*   **Toolchain Discovery:** Uses the path of `CMAKE_C_COMPILER` to find the exact NDK `clang` binary needed by Rust for linking.
*   **Function Overriding:** Before `llama.cpp` is included via `add_subdirectory`, we redefine `ExternalProject_Add`. This allows us to surgically strip the submodule's original `BUILD_COMMAND` and replace it with a call to our generated wrapper script.

### 3. Ninja Layer: Command Interception
To track future changes in `llama.cpp`, developers should compare these two command signatures:

*   **Original Command (Submodule):**
    `cargo build --release --package llguidance`
*   **Modified Command (Hijack):**
    `cargo build --release --package llguidance --target=${RUST_NDK_TRIPLE}`

Our interceptor logic in `CMakeLists.txt` uses a loop to remove the original command and any following arguments until the next CMake keyword is found, ensuring a clean replacement even if the submodule command gains new flags.

### 4. Rust/Cargo Layer: ABI Isolation
A common point of confusion is how different ABIs (e.g., `x86_64` and `arm64-v8a`) avoid overwriting each other's artifacts in the `target/release` folder.

*   **Separate Binary Trees:** CMake is invoked **once per ABI** by Android Studio. Each invocation has a unique `${CMAKE_BINARY_DIR}` (e.g., `.cxx/.../x86_64` vs `.cxx/.../arm64-v8a`).
*   **Isolated Sources:** The `ExternalProject_Add` in `llama.cpp` clones/references the source into `${CMAKE_BINARY_DIR}/llguidance/source`.
*   **Result:** Because the root source folder is unique for every ABI, the `target/release` folder is also unique. There is **zero overlap** on disk between the artifacts of different architectures.

## Key Files & Locations
*   **Interceptor Definition:** `llama-android-core/src/main/cpp/CMakeLists.txt`
*   **Generated Wrapper:** `${CMAKE_BINARY_DIR}/cargo-hijack.sh`
*   **Submodule Logic:** `llama.cpp/common/CMakeLists.txt` (READ-ONLY)
*   **Rust Source:** `llama.cpp/llguidance/source/`
