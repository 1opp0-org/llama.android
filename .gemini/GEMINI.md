# Llama Android Project Mandates

This project aims to provide a high-quality, local-only LLM inference library for Android.

## Ground rules for AI assistants

- No vibe coding as a general rule. 
- No removing files or running command lines as a general rule.
- No editing kotlin and gradle files as a general rule (AI only files such as .gemini/conductor are ok).
- Exceptions to these rules are made on a granular basis, with clear user consent in writing, and revoked automatically soon after granted. If unsure whether they're revoked, ask the user.
- Android Studio allows for many gradle commands to be invoked through its MCP plugin, without direct command line. If it's not active, ask the user whether they are using Android Studio and whether they installed the MCP plugin by jetbrains.

## Engineering Standards
- **Compose Only:** The `sample-app` must remain a 100% Jetpack Compose application. No XML layouts should be introduced.
- **Dependency Management:** All dependencies and version numbers must be managed via `gradle/libs.versions.toml`.
- **Gradle:** Always use the provided Gradle wrapper (`./gradlew`).
- **Roadmaps & Graphs:** All project roadmaps, dependency graphs, or visual process flows MUST use plain ASCII text. Mermaid or other graphic formats are strictly prohibited for these purposes.

## Project Structure & Maintenance
In order of relevance:

- **`llama.cpp`** Git Submodule from https://github.com/ggml-org/llama.cpp. Treated as READ-ONLY, since edits must be pushed to upstream repository. This is the repository used as a basis for Ollama: https://ollama.com/.
- **`llama-android-core`:** This contains the core library logic and public API in charge of loading models, submitting assistant/user prompts and getting text back. It contains glue code (JNI) between C++ and Java/Kotlin which can greatly benefit from AI assistance for edits. It must NOT have internet access whatsoever, as a pillar to pure LLM inference that values privacy. It must be as lean as possible, with abstractions moved to other modules.
- **`llama.android-models`:** (future) Helps with downloading models from internet (hugging face) and finding modules already downloaded to app `cache` folder or available by other means, such as embedded in `apk/aab` assets and or raw folder at build time.
- **`sample-app`:** A sample android app in compose showcasing the library usage.

# AI assistant prompt (prepared by an AI assistant ;) )

## MISSION BRIEFING:

You are inheriting a wrapper for llama.cpp that is currently in a "functional but brittle" state. We are fighting a two-front war: trying to enable modern ARM kernels (KLEIDIAI) while supporting a CI graveyard (JitPack) that runs on GLIBC 2.23.
  
## CRITICAL ARCHITECTURE NOTES:
  1. The JitPack Hack: Do NOT touch the before_install block in jitpack.yml unless you understand that AGP defaults to a CMake version that requires GLIBC 2.28. We manually inject a portable CMake 3.31 + Ninja 1.12 and force the path via cmake.dir. It’s ugly, but it’s the only way to get KLEIDIAI compiling on their outdated boxes.
  2. The Version Tightrope: libs.versions.toml is the source of truth. Keep cmakeVersion synced with the manual
  download in jitpack.yml or the build will fail silently by falling back to a broken SDK version.

## YOUR STANDARDS:
  - No flattering. No "looks good to me."
  - If you see documentation drift (like in DEVELOPMENT.md and CHANGELOG.md), warn the user at the appropriate time - probably when a new PR is being prepared and specially when a new version is being baked.
  - Build it like it's going to run on a billion devices, even if right now it's only running on your desk and a dusty server in JitPack's basement.
    - user's note: this is what AI thinks it needs to remind itself to take code seriously 

