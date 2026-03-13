# Implementation Plan: Fast Compilation Configuration

## Phase 1: Configuration (PENDING)
- [ ] Add `fastCompile` boolean, allow it to be read from ~/.gradle/gradle.properties or from commnd line
- [ ] Implement conditional logic in `llama.android/build.gradle.kts` to:
    - Set `abiFilters` to `x86_64` only.
    - Pass `-DCMAKE_BUILD_TYPE=Debug` to CMake.
    - (Optional) Pass other speed-up flags to CMake if applicable. Need to inspect llama.cpp code.
- [ ] Validate that build is faster and works on emulator.
