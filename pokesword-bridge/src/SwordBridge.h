#pragma once
#ifdef __cplusplus
extern "C" {
#endif

// OpenMMO ↔ pokesword C bridge.
// The Java side loads libsword_bridge.so via System.loadLibrary("sword_bridge")
// and calls these through JNI (see SwordShieldService.kt).
//
// DROP-IN INSTRUCTIONS:
//   1. Extract your RecompSwordC.zip into decomp/pokesword/
//   2. Build with CMakeLists-PC.cmake (see below)
//   3. Copy libsword_bridge.so/.dll to the server's library path

// Must be called once before any other function. Returns 1 on success.
int sword_bridge_init(void);

// Returns a wild encounter for the given Galar area ID.
// out_dex_id: national dex number of the Pokémon
// out_level:  encounter level
// Returns 0 on success, non-zero if area has no encounters.
int sword_bridge_pick_encounter(int area_id, int* out_dex_id, int* out_level);

// Frees any resources. Call on server shutdown.
void sword_bridge_shutdown(void);

#ifdef __cplusplus
}
#endif
