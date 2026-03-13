# Tracks Registry

## Project Roadmap

- [x] **Track: Project Initialization** (ID: `init`)
- [ ] **Track: Pick a License** (ID: `license`)
  - *Depends on:* `init`
- [x] **Track: TOML Versioning** (ID: `toml`)
  - *Depends on:* `init`
- [ ] **Track: Fast Compilation Configuration** (ID: `fast-compile`)
  - *Depends on:* `init`
- [/] **Track: Core Library Refactoring** (ID: `refactor`)
  - *Depends on:* `toml`
- [x] **Track: Refactor sample activity into Compose** (ID: `sample-refactor`)
  - *Depends on:* `init`
- [/] **Track: CI Setup** (ID: `ci`)
  - *Depends on:* `init`
- [ ] **Track: Instrumented Test Hello World (CI)** (ID: `test-hw`)
  - *Depends on:* `ci`
- [ ] **Track: Core Module Instrumented Tests** (ID: `test-core`)
  - *Depends on:* `refactor`, `test-hw`
- [ ] **Track: JNI Bridge Implementation** (ID: `jni`)
  - *Depends on:* `refactor`
- [ ] **Track: Sample App Enhancement** (ID: `sample`)
  - *Depends on:* `jni`
- [ ] **Track: Text Classification Support** (ID: `feature-classify`)
  - *Depends on:* `jni`
- [ ] **Track: Structured Outputs Support** (ID: `feature-structured`)
  - *Depends on:* `jni`
- [ ] **Track: Publish to JitPack** (ID: `publish`)
  - *Depends on:* `jni`, `ci`
- [ ] **Track: Upstream Refactors** (ID: `upstream`)
  - *Depends on:* `jni`

## Visual Roadmap (ASCII)

```
       [init]
      /  |   \_________________
     / [license]               \
    /    |                      \
 [toml]--+-------[ci]      [fast-compile]
   |     |         |
   |     |     [test-hw]
   |     |         |
 [refactor]--------+
   |     |         |
   |     +----[test-core]
   |     |
 [jni]---+
  / | \   \
 /  |  \   +---[upstream]
 |  |   \
 |  |    +---[publish] <--- (ci)
 |  |
 |  +---[feature-structured]
 |
 +---[feature-classify]
 |
 +---[sample]
```
