# Version 0.0

## 0.0.3
- Downgrade minimum sdk from 33 to 30 (covers 83% of all devices)
- Compile CPP code with release optimizations (it's an order of magnitude faster than debug)
- Change package for classes from `com.arm.aichat` to `net.amazingapps.llama.android.core` 

## 0.0.2

- Upgrades from cmake 3.22.1 back to previous value 3.31.6
- On Jitpack: Manually download and install cmake and ninja-build
- This allows us to re-enable KLEIDI on ARM
- Downgrades android compileSdk from 36 to 34
## 0.0.1

Proof of concept. 

- Grabs the llama.cpp/examples/llama.cpp folder and promotes it to its own first class gradle module
- Modernizes the application module to compose
- Downgrades cmake and disables KLEIDI to make it compile on jitpack with its VERY outdated Ubuntu 16.04