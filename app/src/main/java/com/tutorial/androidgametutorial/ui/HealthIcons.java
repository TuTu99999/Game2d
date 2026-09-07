package com.tutorial.androidgametutorial.ui;

import android.graphics.Bitmap;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum HealthIcons implements BitmapMethods {

    HEART_FULL(0),
    HEART_3Q(1),
    HEART_HALF(2),
    HEART_1Q(3),
    HEART_EMPTY(4);

    private volatile Bitmap icon;
    private final int xPos;
    private static final int ICON_SIZE = 16;

    HealthIcons(int xPos) {
        this.xPos = xPos;
    }
    public Bitmap getIcon() {
        if (icon == null || icon.isRecycled()) {
            synchronized (this) {
                if (icon == null || icon.isRecycled()) {
                    Bitmap atlas = BitmapCache.get(
                            MainActivity.getGameContext(), R.drawable.health_icons
                    );
                    Bitmap cell = Bitmap.createBitmap(
                            atlas, xPos * ICON_SIZE, 0, ICON_SIZE, ICON_SIZE
                    );
                    icon = Bitmap.createScaledBitmap(
                            cell,
                            ICON_SIZE * GameConstants.Sprite.SCALE_MULTIPLIER,
                            ICON_SIZE * GameConstants.Sprite.SCALE_MULTIPLIER,
                            false
                    );
                    if (icon != cell) cell.recycle();
                }
            }
        }
        return icon;
    }
}
