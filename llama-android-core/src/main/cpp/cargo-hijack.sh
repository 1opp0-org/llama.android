#!/bin/sh
# cargo-hijack.sh
# This script is called by CMake to cross-compile Rust components for Android.
# It expects the following environment variables to be set by CMake:
# - RUST_NDK_TRIPLE
# - ANDROID_NDK_CLANG_PATH
# - NDK_BIN_DIR
# It must also get the following ones somehow (usually from gradle's parent process environment):
# - CARGO_HOME
# - RUSTUP_HOME


##############################
# PREP AND CHECK ENVIRONMENT
##############################

echo "Rust Build: Hijack script running for Triple='${RUST_NDK_TRIPLE}'"

# Calculate RUST_TARGET_UPPER (e.g., aarch64-linux-android -> AARCH64_LINUX_ANDROID)
RUST_TARGET_UPPER=$(echo "${RUST_NDK_TRIPLE}" | tr '-' '_' | tr '[:lower:]' '[:upper:]')

export CARGO_TARGET_${RUST_TARGET_UPPER}_LINKER="${ANDROID_NDK_CLANG_PATH}"
export CARGO_TARGET_${RUST_TARGET_UPPER}_AR="${NDK_BIN_DIR}/llvm-ar"

if [ -z "${CARGO_HOME}" ] ; then
  echo "Rust Build: CARGO_HOME is not set, fail build."
  exit 1
fi

if [ -z "${RUSTUP_HOME}" ] ; then
  echo "Rust Build: RUSTUP_HOME is not set, fail build."
  exit 1
fi

# Check if a default toolchain is configured. If not, try to use 'stable'.
if ! rustup default > /dev/null 2>&1; then
    echo "Rust Build: No default toolchain found, fail build."
    echo "CARGO_HOME  = ${CARGO_HOME}"
    echo "RUSTUP_HOME = ${RUSTUP_HOME}"
    exit 1
else
    CARGO_CMD="cargo"
fi

##########
# BUILD
##########

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
