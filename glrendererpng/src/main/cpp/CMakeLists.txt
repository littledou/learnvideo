cmake_minimum_required(VERSION 3.10.2)

include_directories(./include)
include_directories(./src)

set(libpng_source_dir ${CMAKE_CURRENT_SOURCE_DIR}/3rdpart/libpng-1.6.37)
#set(libpng_source_dir /Users/loki/Desktop/tools/extra/libpng-1.6.37)
include_directories(${libpng_source_dir})
add_library(libpng STATIC
        ${libpng_source_dir}/arm/arm_init.c
        ${libpng_source_dir}/arm/filter_neon.S
        ${libpng_source_dir}/arm/filter_neon_intrinsics.c
        ${libpng_source_dir}/arm/palette_neon_intrinsics.c
        ${libpng_source_dir}/png.c
        ${libpng_source_dir}/pngerror.c
        ${libpng_source_dir}/pngget.c
        ${libpng_source_dir}/pngmem.c
        ${libpng_source_dir}/pngpread.c
        ${libpng_source_dir}/pngread.c
        ${libpng_source_dir}/pngrio.c
        ${libpng_source_dir}/pngrtran.c
        ${libpng_source_dir}/pngrutil.c
        ${libpng_source_dir}/pngset.c
        ${libpng_source_dir}/pngtrans.c
        ${libpng_source_dir}/pngwio.c
        ${libpng_source_dir}/pngwrite.c
        ${libpng_source_dir}/pngwtran.c
        ${libpng_source_dir}/pngwutil.c)

include_directories(./librenderer)
add_library(librenderer STATIC
        librenderer/PngPreviewController.cpp
        librenderer/image.c
        librenderer/png_decoder.cpp
        librenderer/PngPreviewTexture.cpp
        librenderer/PngPreviewRenderer.cpp
        librenderer/EGLController.cpp
        )

add_library(egluse SHARED
        src/native-lib.cpp
        src/image-preview.cpp)

#find_library(glesv2-lib GLESv2)
#then: ${glesv2-lib}
target_link_libraries(
        egluse
        librenderer
        libpng
        android
        EGL
        GLESv2
        z
        log
)