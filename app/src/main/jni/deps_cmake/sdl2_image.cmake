cmake_minimum_required(VERSION 3.4.0)

project(SDL2_image C)

file(GLOB SDL2_IMAGE_BASE
    SDL2_image/*.c)

set(SDL2_IMAGE_SOURCES
    ${SDL2_IMAGE_BASE}
    )

add_library(SDL2_image
            STATIC
            ${SDL2_IMAGE_SOURCES})

target_link_libraries(SDL2_image
    SDL2
    )

target_include_directories(SDL2_image PUBLIC
        SDL2_image)

target_include_directories(SDL2_image PRIVATE
        SDL2_image/external/libpng-1.6.32)

set_target_properties(SDL2_image
    PROPERTIES
    COMPILE_FLAGS
    "-DLOAD_BMP\
    -DLOAD_GIF\
    -DLOAD_LBM\
    -DLOAD_PCX\
    -DLOAD_PNM\
    -DLOAD_TGA\
    -DLOAD_XCF\
    -DLOAD_XPM\
    -DLOAD_XV")
