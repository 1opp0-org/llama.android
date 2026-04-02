cmake_minimum_required(VERSION 3.31.6)

# Use an absolute path for the log to prevent fragmentation
set(GLOBAL_HIJACK_LOG "${CMAKE_CURRENT_SOURCE_DIR}/../../../cargo_hijack.log")
file(APPEND "${GLOBAL_HIJACK_LOG}" "[Step 01] CMake start\n")

project("ai-chat" VERSION 1.0.0 LANGUAGES C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED true)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED true)

set(CMAKE_C_FLAGS   "${CMAKE_C_FLAGS}"   CACHE STRING "" FORCE)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS}" CACHE STRING "" FORCE)

# --------------------------------------------------------------------------
# AI Chat library
# --------------------------------------------------------------------------

if(DEFINED ANDROID_ABI)
    file(APPEND "${GLOBAL_HIJACK_LOG}" "[Step 10] ABI block: ${ANDROID_ABI}\n")
    message(STATUS "Detected Android ABI: ${ANDROID_ABI}")
    if(ANDROID_ABI STREQUAL "arm64-v8a")
        set(GGML_SYSTEM_ARCH "ARM")
        set(GGML_CPU_KLEIDIAI ON)
        set(GGML_OPENMP ON)
        set(RUST_NDK_TRIPLE "aarch64-linux-android")
    elseif(ANDROID_ABI STREQUAL "x86_64")
        set(GGML_SYSTEM_ARCH "x86")
        set(GGML_CPU_KLEIDIAI OFF)
        set(GGML_OPENMP OFF)
        set(RUST_NDK_TRIPLE "x86_64-linux-android")
    else()
        message(FATAL_ERROR "Unsupported ABI: ${ANDROID_ABI}")
    endif()

    # Locate the target-specific clang from NDK to use as the Rust linker
    get_filename_component(NDK_BIN_DIR ${CMAKE_C_COMPILER} DIRECTORY)
    set(ANDROID_NDK_CLANG_PATH "${NDK_BIN_DIR}/${RUST_NDK_TRIPLE}${ANDROID_NATIVE_API_LEVEL}-clang")
    message(STATUS "Using Rust linker: ${ANDROID_NDK_CLANG_PATH}")
    
    # --- ExternalProject Hijacker ---
    file(APPEND "${GLOBAL_HIJACK_LOG}" "[Step 20] Defining interceptor\n")
    include(ExternalProject)

    function(ExternalProject_Add name)
        file(APPEND "${GLOBAL_HIJACK_LOG}" "[Step 30] ExternalProject_Add(${name}) called\n")
        if (name STREQUAL "llguidance_ext")
            file(APPEND "${GLOBAL_HIJACK_LOG}" "[Step 40] Match found: hijacking ${name}\n")
            
            string(TOUPPER ${RUST_NDK_TRIPLE} RUST_TARGET_UPPER)
            string(REPLACE "-" "_" RUST_TARGET_UPPER ${RUST_TARGET_UPPER})

            # Create a verbatim wrapper script
            set(HIJACK_SCRIPT "${CMAKE_BINARY_DIR}/cargo-hijack.sh")
            file(WRITE "${HIJACK_SCRIPT}" 
"#!/bin/sh
echo \"[Step 50] Hijack script RUNNING for ${RUST_NDK_TRIPLE}\" >> \"${GLOBAL_HIJACK_LOG}\"
export CARGO_TARGET_${RUST_TARGET_UPPER}_LINKER='${ANDROID_NDK_CLANG_PATH}'
export CARGO_TARGET_${RUST_TARGET_UPPER}_AR='${NDK_BIN_DIR}/llvm-ar'

# Run the real build
cargo build --release --package llguidance --target=${RUST_NDK_TRIPLE}
RES=$?

if [ $RES -eq 0 ]; then
    echo '  -> Build successful, relocating' >> \"${GLOBAL_HIJACK_LOG}\"
    mkdir -p target/release
    cp -f target/${RUST_NDK_TRIPLE}/release/libllguidance.a target/release/
    cp -f target/${RUST_NDK_TRIPLE}/release/llguidance.h target/release/
else
    echo '  -> Build FAILED' >> \"${GLOBAL_HIJACK_LOG}\"
fi
exit $RES
")
            execute_process(COMMAND chmod +x "${HIJACK_SCRIPT}")

            # Robustly filter out original commands to prevent leaks/conflicts
            set(HIJACKED_ARGN "")
            set(SKIP_NEXT FALSE)
            foreach(ARG IN LISTS ARGN)
                if (SKIP_NEXT)
                    set(SKIP_NEXT FALSE)
                    continue()
                endif()
                # Remove commands we are about to override
                if (ARG STREQUAL "BUILD_COMMAND" OR ARG STREQUAL "CONFIGURE_COMMAND" OR ARG STREQUAL "INSTALL_COMMAND")
                    set(SKIP_NEXT TRUE)
                    continue()
                endif()
                list(APPEND HIJACKED_ARGN ${ARG})
            endforeach()

            file(APPEND "${GLOBAL_HIJACK_LOG}" "[Step 41] Hijack script path: ${HIJACK_SCRIPT}\n")

            # Point ExternalProject to our wrapper and explicit command overrides
            _ExternalProject_Add(${name}
                ${HIJACKED_ARGN}
                CONFIGURE_COMMAND sh -c "echo '[Step 45] Configure started' >> '${GLOBAL_HIJACK_LOG}'"
                BUILD_COMMAND "${HIJACK_SCRIPT}"
                INSTALL_COMMAND ""
            )
        else()
            _ExternalProject_Add(${name} ${ARGN})
        endif()
    endfunction()
endif()

file(APPEND "${GLOBAL_HIJACK_LOG}" "[Step 99] ABOUT TO INCLUDE SUBDIRECTORY\n")
set(LLAMA_SRC ${CMAKE_CURRENT_LIST_DIR}/../../../../llama.cpp)
add_subdirectory(${LLAMA_SRC} build-llama)

add_library(${CMAKE_PROJECT_NAME} SHARED
        ai_chat.cpp
        gbnf_grammar.cpp)

target_compile_definitions(${CMAKE_PROJECT_NAME} PRIVATE
        GGML_SYSTEM_ARCH=${GGML_SYSTEM_ARCH}
        GGML_CPU_KLEIDIAI=$<BOOL:${GGML_CPU_KLEIDIAI}>
        GGML_OPENMP=$<BOOL:${GGML_OPENMP}>
)

target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
        ${LLAMA_SRC}
        ${LLAMA_SRC}/src
        ${LLAMA_SRC}/common
        ${LLAMA_SRC}/include
        ${LLAMA_SRC}/ggml/include
        ${LLAMA_SRC}/ggml/src)

target_link_libraries(${CMAKE_PROJECT_NAME}
        llama
        common
        android
        log)
