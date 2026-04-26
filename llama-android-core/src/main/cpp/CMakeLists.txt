cmake_minimum_required(VERSION 3.31.6)

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

# Capture absolute paths once at the start to ensure they don't change in subdirectories
get_filename_component(HIJACK_SCRIPT_PATH "${CMAKE_CURRENT_LIST_DIR}/cargo-hijack.sh" ABSOLUTE)
get_filename_component(LLAMA_SRC "${CMAKE_CURRENT_LIST_DIR}/../../../../llama.cpp" ABSOLUTE)

# Store in cache to make them globally accessible to all scopes and functions
set(HIJACK_SCRIPT_PATH "${HIJACK_SCRIPT_PATH}" CACHE INTERNAL "Path to the Rust hijack script")
set(LLAMA_SRC "${LLAMA_SRC}" CACHE INTERNAL "Path to the llama.cpp submodule")

if(DEFINED ANDROID_ABI)
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
    if (LLAMA_LLGUIDANCE)
        message(STATUS "Enabling Rust build interceptor (LLAMA_LLGUIDANCE is ON)")
        message(STATUS "Resolved Hijack Script: ${HIJACK_SCRIPT_PATH}")
        
        include(ExternalProject)

        function(ExternalProject_Add name)
            if (name STREQUAL "llguidance_ext")
                message(STATUS "Intercepting and hijacking ExternalProject: ${name}")

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

                # Point ExternalProject to our static wrapper with explicit environment variables.
                # We pass PATH, RUSTUP_HOME and CARGO_HOME to ensure the script can find and use the 
                # correct Rust toolchain, especially in custom or restricted environments.
                _ExternalProject_Add(${name}
                    ${HIJACKED_ARGN}
                    CONFIGURE_COMMAND ${CMAKE_COMMAND} -E echo "Rust configuration phase starting..."
                    BUILD_COMMAND ${CMAKE_COMMAND} -E env 
                        "PATH=$ENV{PATH}"
                        RUST_NDK_TRIPLE=${RUST_NDK_TRIPLE}
                        ANDROID_NDK_CLANG_PATH=${ANDROID_NDK_CLANG_PATH}
                        NDK_BIN_DIR=${NDK_BIN_DIR}
                        sh "${HIJACK_SCRIPT_PATH}"
                    INSTALL_COMMAND ""
                )
            else()
                _ExternalProject_Add(${name} ${ARGN})
            endif()
        endfunction()
    else()
        message(STATUS "Skipping Rust build interceptor (LLAMA_LLGUIDANCE is OFF)")
    endif()
endif()

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
