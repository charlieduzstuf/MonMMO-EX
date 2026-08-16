#include "SwordBridge.h"
#include <cstdlib>
#include <ctime>

// Stub implementation — replace function bodies with calls into your
// RecompSwordC compiled objects once they are linked into this target.
// See CMakeLists-PC.cmake for how to add them.

static bool g_initialized = false;

extern "C" {

int sword_bridge_init(void) {
    if (g_initialized) return 1;
    std::srand(static_cast<unsigned>(std::time(nullptr)));
    // TODO: call into RecompSwordC init routines here once linked
    g_initialized = true;
    return 1;
}

// Stub encounter table. Replace with real data from pokesword prog/ code.
// area_id 101-199 = named routes, 200+ = Wild Area zones.
int sword_bridge_pick_encounter(int area_id, int* out_dex_id, int* out_level) {
    if (!out_dex_id || !out_level) return -1;
    // ponytail: flat table, replace with data from decomp when available
    struct Entry { int dex; int min_lv; int max_lv; };
    static const Entry table[][4] = {
        /* area 101 - Route 1 */   {{831, 2, 5}, {835, 3, 5}, {840, 3, 6}, {832, 2, 4}},
        /* area 102 - Route 2 */   {{835, 5, 8}, {843, 5, 9}, {848, 6, 9}, {052, 6, 9}},
        /* area 200 - Wild Area */ {{884, 20,55}, {882, 15,45}, {851, 15,50}, {879,20,55}},
    };
    int row = -1;
    if (area_id == 101) row = 0;
    else if (area_id == 102) row = 1;
    else if (area_id == 200) row = 2;
    if (row < 0) return 1;
    const Entry& e = table[row][std::rand() % 4];
    *out_dex_id = e.dex;
    *out_level  = e.min_lv + std::rand() % (e.max_lv - e.min_lv + 1);
    return 0;
}

void sword_bridge_shutdown(void) {
    g_initialized = false;
}

// JNI entry points matching SwordShieldService.kt native declarations.
// Build with -I$(JAVA_HOME)/include and link jvm to enable these.
#ifdef SWORD_BRIDGE_JNI
#include <jni.h>

JNIEXPORT jintArray JNICALL
Java_de_fiereu_openmmo_server_game_services_sword_SwordShieldService_bridgePickEncounter(
    JNIEnv* env, jobject /*self*/, jint areaId)
{
    int dex = 0, level = 0;
    sword_bridge_pick_encounter(static_cast<int>(areaId), &dex, &level);
    jintArray arr = env->NewIntArray(2);
    jint buf[2] = {dex, level};
    env->SetIntArrayRegion(arr, 0, 2, buf);
    return arr;
}

JNIEXPORT jboolean JNICALL
Java_de_fiereu_openmmo_server_game_services_sword_SwordShieldService_bridgeInit(
    JNIEnv* /*env*/, jobject /*self*/)
{
    return sword_bridge_init() ? JNI_TRUE : JNI_FALSE;
}

#endif // SWORD_BRIDGE_JNI

} // extern "C"
