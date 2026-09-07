package com.tutorial.androidgametutorial.entities;

import android.graphics.Bitmap;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum Weapons {

    BIG_SWORD(R.drawable.big_sword),
    SHADOW(R.drawable.shadow);

    private final int resourceId;
    private volatile Bitmap weaponImg;

    Weapons(int resId) {
        resourceId = resId;
    }

    public Bitmap getWeaponImg() {
        ensureLoaded();
        return weaponImg;
    }

    private void ensureLoaded() {
        if (weaponImg != null && !weaponImg.isRecycled()) return;
        synchronized (this) {
            if (weaponImg == null || weaponImg.isRecycled()) {
                weaponImg = BitmapCache.getScaled(
                        MainActivity.getGameContext(), resourceId,
                        GameConstants.Sprite.SCALE_MULTIPLIER, false
                );
            }
        }
    }

    public int getWidth() {
        ensureLoaded();
        return weaponImg.getWidth();
    }

    public int getHeight() {
        ensureLoaded();
        return weaponImg.getHeight();
    }
}
