cmake_minimum_required(VERSION 3.10.2)

include_directories(./src)
include_directories(./include)
include_directories(./src/util)

add_library(libtexture STATIC
        src/util/ShaderUtil.cpp
        )

add_library(egluse SHARED
        src/native-lib.cpp
        src/texture/texture_triangle.cpp
        )

target_link_libraries(
        egluse
        libtexture
        android
        EGL
        GLESv2
        z
        log
)