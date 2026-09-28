#include <fluidsynth.h>
#include <stdlib.h>

/* Settings */
fluid_settings_t* new_fluid_settings(void) { return (fluid_settings_t*)calloc(1, 1); }
void delete_fluid_settings(fluid_settings_t* s) { free(s); }
int fluid_settings_setnum(fluid_settings_t* s, const char* n, double v) { (void)s;(void)n;(void)v; return 0; }

/* Synth */
fluid_synth_t* new_fluid_synth(fluid_settings_t* s) { (void)s; return (fluid_synth_t*)calloc(1, 1); }
void delete_fluid_synth(fluid_synth_t* s) { free(s); }
int fluid_synth_sfload(fluid_synth_t* s, const char* f, int r) { (void)s;(void)f;(void)r; return -1; }

/* Audio driver */
fluid_audio_driver_t* new_fluid_audio_driver(fluid_settings_t* s, fluid_synth_t* sy) { (void)s;(void)sy; return (fluid_audio_driver_t*)calloc(1, 1); }
void delete_fluid_audio_driver(fluid_audio_driver_t* d) { free(d); }

/* SoundFont */
int fluid_is_soundfont(const char* f) { (void)f; return 1; }

/* Player */
fluid_player_t* new_fluid_player(fluid_synth_t* s) { (void)s; return (fluid_player_t*)calloc(1, 1); }
void delete_fluid_player(fluid_player_t* p) { free(p); }
int  fluid_player_add_mem(fluid_player_t* p, const void* b, long l) { (void)p;(void)b;(void)l; return -1; }
int  fluid_player_play(fluid_player_t* p) { (void)p; return -1; }
int  fluid_player_stop(fluid_player_t* p) { (void)p; return -1; }
int  fluid_player_seek(fluid_player_t* p, int t) { (void)p;(void)t; return -1; }
int  fluid_player_set_loop(fluid_player_t* p, int l) { (void)p;(void)l; return -1; }
int  fluid_player_get_status(fluid_player_t* p) { (void)p; return FLUID_PLAYER_READY; }
