#!/bin/bash

# This script is optimized to install rust, cargo and rustup on jitpack (ubuntu 18) so it cross compiles to android.

INSTALL_FOLDER=~/bin/rust/rustup
export RUSTUP_HOME=$INSTALL_FOLDER/rustup
export  CARGO_HOME=$INSTALL_FOLDER/cargo
export ANDROID_NDK_CLANG_PATH=$ANDROID_HOME/ndk/29.0.13113456

export PATH=$CARGO_HOME/bin:$PATH

###############################################
# These variables are for script internal use
RUST_INSTALL_VERSION="1.94.1"
RUSTUP_INSTALL_SCRIPT_SHA="f8acbf60ac32fcd84ce45e7555a4411b0d9dcbea"
###############################################

# Verifies that RUSTUP_HOME, CARGO_HOME and ANDROID_NDK_CLANG_PATH are set properly
function check_environment() {

  rustVersion=$(rustc --version 2> /dev/null| cut -f2 -d' ')

  if [ "$rustVersion" != "$RUST_INSTALL_VERSION" ]; then
    echo "Expected rustc version '$RUST_INSTALL_VERSION' but got '$rustVersion'."
    echo "You can run 'install_rustup' to install rust and cargo at $INSTALL_FOLDER"
    return
  fi

  if [ "$ANDROID_HOME" == "" ] ; then
    echo "You must set \$ANDROID_HOME"
    return
  fi

  if ! [ -d "$ANDROID_NDK_CLANG_PATH" ]; then
    echo "The folder specified for \$$ANDROID_NDK_CLANG_PATH is '$ANDROID_NDK_CLANG_PATH' but it does not exist "
    return
  fi

  echo "Rust environment is ready for android cross compilation with version $RUST_INSTALL_VERSION"
}

function install_rustup() {
  curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs > rustup_install.sh
  sha=$(sha1sum rustup_install.sh | cut -f1 -d' ')

  if [ "$sha" != "$RUSTUP_INSTALL_SCRIPT_SHA" ]
  then
    echo "We downloaded a script that is not known - either corrupt, malicious or newer version of rust up installer. Stopping install."
    return 1
  else

    chmod a+x rustup_install.sh
    export RUSTUP_INIT_SKIP_PATH_CHECK=yes
    echo "install rustup"

    ./rustup_install.sh -q -y --no-modify-path --profile minimal --default-toolchain none

    rustup -q toolchain install $RUST_INSTALL_VERSION
    echo "rust host toolchain installed"
    rustup -q target add x86_64-linux-android
    rustup -q target add aarch64-linux-android
    echo "rust cross compiler toolchain installed"

  fi

}

# Can be used to validate your typical rust environment
function verify_compilation_host() {

  echo 'fn main() { println!("Hello from host Rust!"); }' > test_hello.rs

  rustc --target x86_64-unknown-linux-gnu \
        test_hello.rs -o test_host

}

# Can be used to validate that your rust environment supports cross compilation to android x86_64 and aarm64
function verify_compilation_android() {

  echo 'fn main() { println!("Hello from Android Rust!"); }' > test_hello.rs

  local linker_path=$ANDROID_NDK_CLANG_PATH/toolchains/llvm/prebuilt/linux-x86_64/bin/x86_64-linux-android30-clang
  local linker_args=$ANDROID_NDK_CLANG_PATH/toolchains/llvm/prebuilt/linux-x86_64/sysroot

  rustc --target x86_64-linux-android \
        -C linker=$linker_path \
        -C link-arg=--sysroot=$linker_args \
        test_hello.rs -o test_android_x86_64

  local linker_path=$ANDROID_NDK_CLANG_PATH/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android30-clang

  rustc --target aarch64-linux-android \
        -C linker=$linker_path \
        -C link-arg=--sysroot=$linker_args \
        test_hello.rs -o test_android_aarch64

}

# ==============================================================================
# USAGE
# ==============================================================================
# To use this script in JitPack/Ubuntu 18.04:
# 1. source jitpack_rust.sh
# 2. install_rustup
# 3. verify_compilation_host
# 4. verify_compilation_android

check_environment

