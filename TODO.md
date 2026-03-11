# TODO List

- [ ] **Upgrade libraries:** Review and update all dependencies in `libs.versions.toml` to their latest stable versions.
- [ ] **Update app icon:** Replace the default Android icon with a project-specific Llama icon.
- [ ] **Change coordinates:** Update Maven coordinates (GroupId, ArtifactId) to their final production values for Maven Central.
- [ ] **Move version number to TOML:** Ensure the library version and other hardcoded versions are moved from `build.gradle.kts` files to `libs.versions.toml`.
- [ ] **Create CI files:** Set up GitHub Actions or a similar CI/CD pipeline for automated builds and tests.
- [ ] **Establish Namespace:** Determine and implement the correct project-wide namespace.
- [ ] **Refactor Library Modules:**
    - [ ] Create `llama.android-core` (Developer maintained).
    - [ ] Rename current `llama.android` to `llama.android-jni` (AI maintained).
- [ ] **Library Implementation:** Begin implementing the core C++/Kotlin bridge for llama.cpp.
- [ ] bring dependencies necessary for unit and android tests.
