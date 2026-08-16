cmake_minimum_required(VERSION 3.13)
project(sword_bridge CXX)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ── Drop-in instructions ───────────────────────────────────────────────────
# 1. Extract RecompSwordC.zip to decomp/pokesword/recomp/
#    (the zip should produce .o / .a files of the statically recompiled code)
# 2. Set JAVA_HOME so JNI headers are found.
# 3. cmake -B build-pc -DCMAKE_BUILD_TYPE=Release -P CMakeLists-PC.cmake
#    (or: cmake -B build-pc -DJAVA_HOME=/path/to/jdk)
# 4. cmake --build build-pc
# 5. Copy build-pc/libsword_bridge.so to the JVM library path.
# ───────────────────────────────────────────────────────────────────────────

option(SWORD_BRIDGE_JNI "Build JNI entry points for OpenMMO" ON)

# Find JNI headers if JNI is requested
if(SWORD_BRIDGE_JNI)
    find_package(Java REQUIRED)
    find_package(JNI REQUIRED)
endif()

# Shared library consumed by the Kotlin server via System.loadLibrary
add_library(sword_bridge SHARED
    src/SwordBridge.cpp
)

target_include_directories(sword_bridge PRIVATE src)

if(SWORD_BRIDGE_JNI)
    target_compile_definitions(sword_bridge PRIVATE SWORD_BRIDGE_JNI)
    target_include_directories(sword_bridge PRIVATE ${JNI_INCLUDE_DIRS})
    target_link_libraries(sword_bridge PRIVATE ${JNI_LIBRARIES})
endif()

# ── Uncomment and adjust once RecompSwordC objects are present ──────────────
# Extract RecompSwordC.zip into pokesword-bridge/recomp/ then enable one of:
# file(GLOB RECOMP_OBJS "${CMAKE_SOURCE_DIR}/recomp/*.o")
# target_sources(sword_bridge PRIVATE ${RECOMP_OBJS})
# OR:
# target_link_libraries(sword_bridge PRIVATE ${CMAKE_SOURCE_DIR}/recomp/libsword_recomp.a)
# ───────────────────────────────────────────────────────────────────────────

install(TARGETS sword_bridge LIBRARY DESTINATION lib)
