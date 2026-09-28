package com.doom.rpg;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.libsdl.app.SDLActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends SDLActivity {

    private static final String TAG = "DoomRPG";

    public static native void nativeSetDataDir(String dir);
    public static native void nativeSendKey(int scancode, boolean pressed);

    // SDL scancodes
    private static final int SC_UP     = 82;
    private static final int SC_DOWN   = 81;
    private static final int SC_LEFT   = 80;
    private static final int SC_RIGHT  = 79;
    private static final int SC_RETURN = 40;
    private static final int SC_ESCAPE = 41;
    private static final int SC_TAB    = 43;
    private static final int SC_0      = 39;
    private static final int SC_1      = 30;
    private static final int SC_2      = 31;
    private static final int SC_3      = 32;
    private static final int SC_4      = 33;
    private static final int SC_5      = 34;
    private static final int SC_6      = 35;
    private static final int SC_7      = 36;
    private static final int SC_8      = 37;
    private static final int SC_9      = 38;
    private static final int SC_BACKSPACE = 42;

    private ViewGroup rootLayout;
    private RelativeLayout gamePadLayout;
    private RelativeLayout numPadLayout;
    private boolean passwordMode = false;

    static {
        System.loadLibrary("SDL2");
        System.loadLibrary("SDL2_mixer");
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String dataDir = getFilesDir().getAbsolutePath();
        Log.i(TAG, "Setting dataDir = " + dataDir);

        copyAssets();
        nativeSetDataDir(dataDir);

        super.onCreate(savedInstanceState);

        setupControls();
    }

    @Override
    protected String[] getLibraries() {
        return new String[] { "SDL2", "SDL2_mixer", "native-lib" };
    }

    /**
     * Called from native code when password dialog opens/closes.
     * Must be public for JNI.
     */
    public void setPasswordKeypadVisible(final boolean visible) {
        runOnUiThread(() -> {
            passwordMode = visible;
            if (numPadLayout != null) {
                numPadLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
            if (gamePadLayout != null) {
                // Hide movement pad while entering password to avoid clutter
                gamePadLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
            }
            Log.i(TAG, "Password keypad visible=" + visible);
        });
    }

    private void setupControls() {
        android.view.View contentView = SDLActivity.getContentView();
        if (!(contentView instanceof ViewGroup)) {
            Log.e(TAG, "getContentView() is not a ViewGroup");
            return;
        }
        rootLayout = (ViewGroup) contentView;

        Log.i(TAG, "setupControls: adding buttons to SDL layout");

        setupGamePad();
        setupNumPad();
    }

    private void setupGamePad() {
        gamePadLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams fill = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rootLayout.addView(gamePadLayout, fill);

        int size = 140;

        // D-Pad left
        addGameBtn("▲", SC_UP, size, true, 80, 400);
        addGameBtn("▼", SC_DOWN, size, true, 80, 100);
        addGameBtn("◄", SC_LEFT, size, true, 10, 250);
        addGameBtn("►", SC_RIGHT, size, true, 150, 250);

        // Action right
        addGameBtn("E", SC_RETURN, size, false, 80, 100);
        addGameBtn("X", SC_ESCAPE, size, false, 80, 400);
        addGameBtn("M", SC_TAB, size, false, 240, 250);
    }

    private void addGameBtn(String text, int scancode, int size, boolean left, int marginH, int marginBottom) {
        Button b = makeButton(text, scancode, size, 0.35f);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(size, size);
        p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if (left) {
            p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            p.leftMargin = marginH;
        } else {
            p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            p.rightMargin = marginH;
        }
        p.bottomMargin = marginBottom;
        gamePadLayout.addView(b, p);
    }

    private void setupNumPad() {
        numPadLayout = new RelativeLayout(this);
        numPadLayout.setVisibility(View.GONE);
        RelativeLayout.LayoutParams fill = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rootLayout.addView(numPadLayout, fill);

        // Title
        TextView title = new TextView(this);
        title.setText("ENTER CODE");
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setBackgroundColor(0xAA000000);
        title.setPadding(24, 12, 24, 12);
        RelativeLayout.LayoutParams tp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tp.bottomMargin = 620;
        numPadLayout.addView(title, tp);

        // Grid 3x4: 1-9, DEL, 0, OK
        int btnSize = 120;
        int gap = 12;
        int gridW = btnSize * 3 + gap * 2;
        int gridH = btnSize * 4 + gap * 3;

        RelativeLayout grid = new RelativeLayout(this);
        RelativeLayout.LayoutParams gp = new RelativeLayout.LayoutParams(gridW, gridH);
        gp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        gp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        gp.bottomMargin = 80;
        numPadLayout.addView(grid, gp);

        String[] labels = {
                "1", "2", "3",
                "4", "5", "6",
                "7", "8", "9",
                "⌫", "0", "OK"
        };
        int[] codes = {
                SC_1, SC_2, SC_3,
                SC_4, SC_5, SC_6,
                SC_7, SC_8, SC_9,
                SC_LEFT, SC_0, SC_RETURN  // DEL = left arrow (backspace in password), OK = Enter
        };

        for (int i = 0; i < 12; i++) {
            int row = i / 3;
            int col = i % 3;
            Button b = makeButton(labels[i], codes[i], btnSize, 0.55f);
            if (i == 9) {
                // DEL slightly red tint
                b.setBackgroundColor(0x80AA3333);
            } else if (i == 11) {
                // OK green tint
                b.setBackgroundColor(0x8033AA55);
            }
            RelativeLayout.LayoutParams bp = new RelativeLayout.LayoutParams(btnSize, btnSize);
            bp.leftMargin = col * (btnSize + gap);
            bp.topMargin = row * (btnSize + gap);
            grid.addView(b, bp);
        }
    }

    private Button makeButton(String text, final int scancode, int size, float alpha) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(text.length() > 1 ? 20 : 26);
        b.setTextColor(Color.WHITE);
        b.setAlpha(alpha);
        b.setBackgroundColor(0x60000000);
        b.setPadding(0, 0, 0, 0);

        final float baseAlpha = alpha;
        b.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    nativeSendKey(scancode, true);
                    v.setAlpha(Math.min(1f, baseAlpha + 0.35f));
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    nativeSendKey(scancode, false);
                    v.setAlpha(baseAlpha);
                    return true;
            }
            return false;
        });
        return b;
    }

    private void copyAssets() {
        AssetManager am = getAssets();
        String[] files;
        try {
            files = am.list("");
        } catch (IOException e) {
            Log.e(TAG, "Failed to list assets", e);
            return;
        }

        for (String filename : files) {
            if (!filename.equals("DoomRPG.zip") && !filename.equals("gm.sf2")) {
                continue;
            }

            long assetSize = -1;
            try {
                assetSize = am.openFd(filename).getLength();
            } catch (IOException e) {
                Log.w(TAG, "Cannot get asset size for " + filename
                        + ", will always overwrite");
            }

            File outFile = new File(getFilesDir(), filename);

            if (assetSize > 0 && outFile.exists() && outFile.length() == assetSize) {
                Log.i(TAG, "Asset up to date: " + filename
                        + " (" + outFile.length() + " bytes)");
                continue;
            }

            if (outFile.exists()) {
                outFile.delete();
                Log.i(TAG, "Deleted old asset: " + filename);
            }

            InputStream in = null;
            OutputStream out = null;
            try {
                in = am.open(filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                Log.i(TAG, "Copied asset: " + filename
                        + " -> " + outFile.getAbsolutePath()
                        + " (" + outFile.length() + " bytes)");
            } catch (IOException e) {
                Log.e(TAG, "Failed to copy asset: " + filename, e);
            } finally {
                if (in != null) try { in.close(); } catch (IOException ignored) {}
                if (out != null) try { out.close(); } catch (IOException ignored) {}
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
