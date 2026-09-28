#ifndef FLUIDSYNTH_STUB_H
#define FLUIDSYNTH_STUB_H

#ifdef __cplusplus
extern "C" {
#endif

/* Opaque-типы */
typedef struct _fluid_settings_t fluid_settings_t;
typedef struct _fluid_synth_t fluid_synth_t;
typedef struct _fluid_audio_driver_t fluid_audio_driver_t;
typedef struct _fluid_player_t fluid_player_t;

/* Статусы плеера (для fluid_player_get_status) */
enum {
    FLUID_PLAYER_READY   = 0,
    FLUID_PLAYER_PLAYING = 1,
    FLUID_PLAYER_STOPPING = 2,
    FLUID_PLAYER_DONE    = 3
};

/* Settings */
fluid_settings_t* new_fluid_settings(void);
void delete_fluid_settings(fluid_settings_t* settings);
int fluid_settings_setnum(fluid_settings_t* settings, const char* name, double val);

/* Synth */
fluid_synth_t* new_fluid_synth(fluid_settings_t* settings);
void delete_fluid_synth(fluid_synth_t* synth);
int fluid_synth_sfload(fluid_synth_t* synth, const char* filename, int reset_presets);

/* Audio driver */
fluid_audio_driver_t* new_fluid_audio_driver(fluid_settings_t* settings, fluid_synth_t* synth);
void delete_fluid_audio_driver(fluid_audio_driver_t* driver);

/* SoundFont */
int fluid_is_soundfont(const char* filename);

/* Player (MIDI) */
fluid_player_t* new_fluid_player(fluid_synth_t* synth);
void delete_fluid_player(fluid_player_t* player);
int  fluid_player_add_mem(fluid_player_t* player, const void* buffer, long len);
int  fluid_player_play(fluid_player_t* player);
int  fluid_player_stop(fluid_player_t* player);
int  fluid_player_seek(fluid_player_t* player, int ticks);
int  fluid_player_set_loop(fluid_player_t* player, int loop);
int  fluid_player_get_status(fluid_player_t* player);

#ifdef __cplusplus
}
#endif

#endif /* FLUIDSYNTH_STUB_H */
