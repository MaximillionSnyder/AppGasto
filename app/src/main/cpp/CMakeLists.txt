cmake_minimum_required(VERSION 3.22.1)
project("gastosec")

add_library(gastosec SHARED
        native-lib.cpp
        integrity.cpp
        antidebug.cpp)

target_compile_features(gastosec PRIVATE cxx_std_17)
target_compile_options(gastosec PRIVATE
        -O2
        -fvisibility=hidden
        -fstack-protector-strong
        -Wformat-security)

target_include_directories(gastosec PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/generated)

find_library(log-lib log)

target_link_libraries(gastosec ${log-lib})
