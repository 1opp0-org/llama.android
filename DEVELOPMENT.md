# Publishing library

## Publish to local maven repository

```bash
./gradlew publishToMavenLocal
```


```bash
./gradlew publishReleaseToLocalRepoRepository 
```

## Jitpack

- Jitpack uses Ubuntu 16.04
- Ubuntu 16.04 has GLIBC version 2.23
- android sdkmanager skips several versions of cmake
  - it currently has versions 3.22.1, 3.30.3 and 3.31.6
  - version 3.30.3 is not supported on ubuntu 16.04, but 3.22.1 is
- KLEIDIAI needs cmake 3.24, so we need to disable it. It's a kernel specific for Arm.

### Workaround 1 (TODO)

    1 install:
    2   # 1. Download official CMake 3.31.6 (Compatible with glibc 2.17+)
    3   - wget -q https://github.com/Kitware/CMake/releases/download/v3.31.6/cmake-3.31.6-linux-x86_64.tar.gz
    4   - tar -xzf cmake-3.31.6-linux-x86_64.tar.gz
    5
    6   # 2. Download Ninja (Android SDK CMake usually includes this, but official does not)
    7   - wget -q https://github.com/ninja-build/ninja/releases/download/v1.11.1/ninja-linux.zip
    8   - unzip -q ninja-linux.zip
    9
   10   # 3. Create the SDK directory structure
   11   - mkdir -p /opt/android-sdk-linux/cmake/3.31.6
   12
   13   # 4. Move CMake and Ninja into place
   14   - mv cmake-3.31.6-linux-x86_64/* /opt/android-sdk-linux/cmake/3.31.6/
   15   - mv ninja /opt/android-sdk-linux/cmake/3.31.6/bin/
   16
   17   # 5. Create source.properties so Gradle recognizes the version
   18   - echo "Pkg.Revision=3.31.6" > /opt/android-sdk-linux/cmake/3.31.6/source.properties
   19
   20   # (Optional) Verify it works
   21   - /opt/android-sdk-linux/cmake/3.31.6/bin/cmake --version


