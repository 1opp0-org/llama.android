# Publishing library

## Publish to local maven repository

```bash
./gradlew publishToMavenLocal
```


```bash
./gradlew publishReleaseToLocalRepoRepository 
```

# Internals

## Folder structure

- The `llama.cpp` folder is a git submodule of the llama.cpp github repository.

- llama-android-core contains 
  - JNI code as well as a thin kotlin abstraction layer 
  - ndk/ninja/cmake configuration that builds llama.cpp c++ files, and produces .so files packaged into its own maven artifacts
- sample-app is a demo of how to consume `llama-android-core`

## Note on instrumented tests

`llama-android-core` has instrumented tests that run llm text generation. 

- it has a gradle task that downloads a gguf file into `src/androidTest/assets` (and pif the file exists, it skips downloading)
- this tasks is a dependency of the `connectedAndroidTest` task, so it runs before the tests
- the test .apk contains the gguf file
- when the test starts running, it copies the gguf file into the app's `cache/model` folder
- the test loads the gguf from `cache/model` folder

This stopgap solution is used to avoid having binary files in the repository, which is a hassle because of the size but also a bad practice from security point of view.

Unfortunately it's not possible to load AI models directly from an .apk asset folder, or from its `raw` folder either, so we copy it first into the app's cache. This is a limitation of the existing API for llama.cpp, which requires a file path that can be used with `fopen`.

TODO: validate that the file is authentic and not corrupted, by using a CRC test.
