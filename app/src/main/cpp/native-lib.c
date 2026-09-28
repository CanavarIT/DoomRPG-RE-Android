#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <unistd.h>

#include <SDL.h>

#define LOG_TAG "DoomRPG"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static char g_dataDir[4096] = {0};

/* Виртуальные нажатия клавиш (для экранных кнопок) */

extern int DoomRPG_main(int argc, char* args[]);

JNIEXPORT void JNICALL
Java_com_doom_rpg_MainActivity_nativeSetDataDir(JNIEnv* env, jclass clazz, jstring dir) {
    (void)clazz;
    if (dir == NULL) return;
    const char* d = (*env)->GetStringUTFChars(env, dir, NULL);
    if (d != NULL) {
        strncpy(g_dataDir, d, sizeof(g_dataDir) - 1);
        g_dataDir[sizeof(g_dataDir) - 1] = '\0';
        LOGI("nativeSetDataDir: %s", g_dataDir);
        (*env)->ReleaseStringUTFChars(env, dir, d);
    }
}

JNIEXPORT void JNICALL
Java_com_doom_rpg_MainActivity_nativeSendKey(JNIEnv* env, jclass clazz,
                                             jint scancode, jboolean pressed) {
    (void)env; (void)clazz;
    LOGI("nativeSendKey: scancode=%d, pressed=%d", scancode, pressed);

    extern Uint8 g_virtualKeys[SDL_NUM_SCANCODES];
    if (scancode >= 0 && scancode < SDL_NUM_SCANCODES) {
        g_virtualKeys[scancode] = pressed ? 1 : 0;
    }

    SDL_Event ev;
    SDL_zero(ev);
    ev.type = pressed ? SDL_KEYDOWN : SDL_KEYUP;
    ev.key.type = pressed ? SDL_KEYDOWN : SDL_KEYUP;
    ev.key.timestamp = 0;
    ev.key.windowID = 0;
    ev.key.state = pressed ? SDL_PRESSED : SDL_RELEASED;
    ev.key.repeat = 0;
    ev.key.keysym.scancode = (SDL_Scancode)scancode;
    ev.key.keysym.sym = SDL_GetKeyFromScancode((SDL_Scancode)scancode);
    ev.key.keysym.mod = KMOD_NONE;

    SDL_PushEvent(&ev);
}


int SDL_main(int argc, char* argv[]) {
    LOGI("SDL_main called. dataDir = %s", g_dataDir);

    if (g_dataDir[0] != '\0') {
        if (chdir(g_dataDir) != 0) {
            LOGE("chdir(%s) failed", g_dataDir);
        } else {
            LOGI("cwd changed to %s", g_dataDir);
        }
    }

    return DoomRPG_main(argc, argv);
}
