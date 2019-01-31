cmake_minimum_required(VERSION 3.4.0)

project(SDL_pnglite C)

file(GLOB SDL_pnglite_SOURCES
    SDL_pnglite/pnglite.c
	SDL_pnglite/SDL_pnglite.c
    )

add_library(SDL_pnglite STATIC
    ${SDL_pnglite_SOURCES}
    )

target_include_directories(SDL_pnglite
    PUBLIC
    SDL_pnglite/
    )
include_directories(SDL/include)