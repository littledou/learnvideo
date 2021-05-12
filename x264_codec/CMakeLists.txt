cmake_minimum_required(VERSION 3.10.2)

add_library(x264_codec SHARED src/main/cpp/x264_encoder.cpp src/main/cpp/X264Native.cpp)

find_library(log-lib log)
target_link_libraries(
        x264_codec
        log
)
