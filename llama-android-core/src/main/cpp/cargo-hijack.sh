#!/bin/sh
# cargo-hijack.sh
# This script is called by CMake to cross-compile Rust components for Android.
# It expects the following environment variables to be set by CMake:
# - RUST_NDK_TRIPLE
# - ANDROID_NDK_CLANG_PATH
# - NDK_BIN_DIR

echo "Rust Build: Hijack script running for ${RUST_NDK_TRIPLE}"

# Calculate RUST_TARGET_UPPER (e.g., aarch64-linux-android -> AARCH64_LINUX_ANDROID)
RUST_TARGET_UPPER=$(echo "${RUST_NDK_TRIPLE}" | tr '-' '_' | tr '[:lower:]' '[:upper:]')

export CARGO_TARGET_${RUST_TARGET_UPPER}_LINKER="${ANDROID_NDK_CLANG_PATH}"
export CARGO_TARGET_${RUST_TARGET_UPPER}_AR="${NDK_BIN_DIR}/llvm-ar"

# Check if a default toolchain is configured. If not, try to use 'stable'.
if ! rustup default > /dev/null 2>&1; then
    echo "Rust Build: No default toolchain found, attempting to use '+stable'"
    CARGO_CMD="cargo +stable"
else
    CARGO_CMD="cargo"
fi

# Run the real build
$CARGO_CMD build --release --package llguidance --target="${RUST_NDK_TRIPLE}"
RES=$?

if [ $RES -eq 0 ]; then
    echo "Rust Build: Success, relocating artifacts"
    mkdir -p target/release
    cp -f target/"${RUST_NDK_TRIPLE}"/release/libllguidance.a target/release/
    cp -f target/"${RUST_NDK_TRIPLE}"/release/llguidance.h target/release/
else
    echo "Rust Build: FAILED with exit code $RES"
fi

exit $RES
