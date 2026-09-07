package com.tutorial.androidgametutorial.entities.items;

import android.graphics.Bitmap;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum Items {

    EMPTY_POT(R.drawable.empty_pot),
    MEDIPACK(R.drawable.medipack),
    FISH(R.drawable.fish);

    private final int resourceId;
    private volatile Bitmap image;

    Items(int resId) {
        resourceId = resId;
    }


    public Bitmap getImage() {
        ensureLoaded();
        return image;
    }

    private void ensureLoaded() {
        if (image != null && !image.isRecycled()) return;
        synchronized (this) {
            if (image == null || image.isRecycled()) {
                image = BitmapCache.getScaled(
                        MainActivity.getGameContext(), resourceId,
                        GameConstants.Sprite.SCALE_MULTIPLIER, false
                );
            }
        }
    }

    public int getWidth() {
        ensureLoaded();
        return image.getWidth();
    }

    public int getHeight() {
        ensureLoaded();
        return image.getHeight();
    }
}
