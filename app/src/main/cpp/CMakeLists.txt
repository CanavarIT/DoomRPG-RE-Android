cmake_minimum_required(VERSION 3.25)
project(doom_android LANGUAGES C CXX)

# --- Пути ---
set(SDL2_LIB_DIR   "${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}")
set(SDL2_INC_DIR   "${CMAKE_CURRENT_SOURCE_DIR}/third_party/SDL2/include")
set(SDL2MIX_INC    "${CMAKE_CURRENT_SOURCE_DIR}/third_party/SDL2_mixer/include")
set(FS_INC_DIR     "${CMAKE_CURRENT_SOURCE_DIR}/third_party/fluidsynth/include")

# --- FluidSynth заглушка ---
add_library(fluidsynth_stub STATIC
    third_party/fluidsynth/src/stub.c)
target_include_directories(fluidsynth_stub PUBLIC ${FS_INC_DIR})

# --- SDL2: подсказки для find_package(SDL2) внутри движка ---
set(SDL2_PATH            "${CMAKE_CURRENT_SOURCE_DIR}/third_party/SDL2")
set(SDL2_INCLUDE_DIR     "${SDL2_INC_DIR}")
set(SDL2_LIBRARY         "${SDL2_LIB_DIR}/libSDL2.so")
set(SDL2_BUILDING_LIBRARY TRUE)

# --- SDL2_mixer: подсказки для find_package(SDL2_mixer) ---
set(SDL2_MIXER_INCLUDE_DIR "${SDL2MIX_INC}")
set(SDL2_MIXER_LIBRARY     "${SDL2_LIB_DIR}/libSDL2_mixer.so")

# --- Путь к модулям поиска движка ---
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/DoomRPG-RE/cmake/Modules")

# --- Движок DoomRPG ---
add_subdirectory(DoomRPG-RE)

# --- JNI мост ---
add_library(native-lib SHARED native-lib.c)
target_link_libraries(native-lib
    DoomRPG
    "${SDL2_LIB_DIR}/libSDL2.so"
    "${SDL2_LIB_DIR}/libSDL2_mixer.so"
    log
    android
)