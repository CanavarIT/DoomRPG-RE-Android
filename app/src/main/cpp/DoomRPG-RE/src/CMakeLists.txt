file(GLOB_RECURSE HEADER_FILES
        "*.hpp"
        "*.h")

file(GLOB_RECURSE SOURCE_FILES
        "*.cpp"
        "*.c")

add_library(${PROJECT_NAME} SHARED ${HEADER_FILES} ${SOURCE_FILES})

target_link_libraries(${PROJECT_NAME}
        SDL2::SDL2
        SDL2::mixer
        z
        fluidsynth_stub
)
