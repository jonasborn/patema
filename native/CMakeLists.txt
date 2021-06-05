cmake_minimum_required(VERSION 2.8)
project(patema-native)
set(CMAKE_BUILD_TYPE Release)
set(JAVA_INCLUDE_PATH $ENV{JAVA_HOME}/include)
message(STATUS ${JAVA_INCLUDE_PATH})


find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
set(SOURCE_FILES TapeLinux.c)
add_library(patema-native SHARED ${SOURCE_FILES})