package com.tutorial.androidgametutorial.entities;


import android.graphics.Bitmap;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum GameObjects implements BitmapMethods {

    PILLAR_YELLOW(0, 6, 16, 42, 32, 38),
    STATUE_ANGRY_YELLOW(16, 1, 32, 47, 24, 35),
    MONK_STATUE_BALL_YELLOW(49, 2, 30, 30, 16, 26),
    MONK_STATUE_YELLOW(81, 2, 30, 30, 16, 26),
    SOLDIER_SPEAR_YELLOW(112, 1, 16, 31, 23, 28),
    PLANTER_STICKS_YELLOW(128, 11, 16, 20, 12, 17),
    CUBE_YELLOW(32, 48, 16, 16, 3, 13),
    FROG_YELLOW(48, 38, 32, 26, 16, 24),
    SOLDIER_SWORD_YELLOW(81, 32, 31, 32, 20, 29),
    PILLAR_SHORT_YELLOW(112, 32, 16, 32, 21, 30),
    PILLAR_SNOW_YELLOW(128, 32, 16, 32, 21, 30),
    PILLAR_GREEN(0, 70, 16, 42, 32, 38),
    STATUE_ANGRY_GREEN(16, 65, 32, 47, 24, 35),
    MONK_STATUE_BALL_GREEN(49, 66, 30, 30, 16, 26),
    MONK_STATUE_GREEN(81, 66, 30, 30, 16, 26),
    SOLDIER_SPEAR_GREEN(112, 65, 16, 31, 23, 28),
    PLANTER_STICKS_GREEN(128, 75, 16, 20, 12, 17),
    CUBE_GREEN(32, 112, 16, 16, 3, 13),
    FROG_GREEN(48, 102, 32, 26, 16, 24),
    SOLDIER_SWORD_GREEN(81, 96, 31, 32, 20, 29),
    PILLAR_SHORT_GREEN(112, 96, 16, 32, 21, 30),
    PILLAR_SNOW_GREEN(128, 96, 16, 32, 21, 30),
    POT_ONE_FULL(144, 0, 16, 19, 10, 17),
    
    POT_ONE_EMPTY(160, 0, 16, 19, 10, 17),
    POT_TWO_FULL(144, 19, 16, 21, 12, 19),
    POT_TWO_EMPTY(160, 20, 16, 20, 12, 19),
    BASKET_FULL_RED_FRUIT(144, 40, 16, 16, 5, 14),
    BASKET_FULL_CHICKEN(160, 40, 16, 16, 5, 14),
    BASKET_EMPTY(144, 56, 16, 16, 5, 14),
    BASKET_FULL_BREAD(160, 56, 16, 16, 5, 14),
    OVEN_SNOW_YELLOW(144, 73, 28, 39, 20, 35),
    OVEN_YELLOW(0, 129, 28, 28, 10, 24),
    OVEN_GREEN(28, 128, 30, 29, 10, 24),
    STOMP(58, 128, 16, 22, 10, 18),
    SMALL_POT_FULL(0, 112, 16, 13, 4, 10),
    SMALL_POT_EMPTY(16, 12, 16, 13, 4, 10),

    SHADOW_ROCK(R.drawable.shadow_rock, 138, 116, 82, 110),
    BROKEN_SHADOW_PILLAR(R.drawable.broken_shadow_pillar, 112, 176, 126, 168);

    volatile Bitmap objectImg;
    int width, height;
    int hitboxRoof, hitboxFloor, hitboxHeight;
    private final int resourceId;
    private final int sourceX;
    private final int sourceY;
    private final int sourceWidth;
    private final int sourceHeight;
    private final boolean atlasBacked;

    GameObjects(int x, int y, int width, int height, int hitboxRoof, int hitboxFloor) {
        this.resourceId = R.drawable.world_objects;
        this.sourceX = x;
        this.sourceY = y;
        this.sourceWidth = width;
        this.sourceHeight = height;
        this.atlasBacked = true;
        this.width = width;
        this.height = height;
        this.hitboxRoof = hitboxRoof;
        this.hitboxFloor = hitboxFloor;
        this.hitboxHeight = (hitboxFloor - hitboxRoof) * GameConstants.Sprite.SCALE_MULTIPLIER;
    }

    GameObjects(int resourceId, int drawWidth, int drawHeight,
                int hitboxRoof, int hitboxFloor) {
        this.resourceId = resourceId;
        this.sourceX = 0;
        this.sourceY = 0;
        this.sourceWidth = drawWidth;
        this.sourceHeight = drawHeight;
        this.atlasBacked = false;
        this.width = drawWidth;
        this.height = drawHeight;
        this.hitboxRoof = hitboxRoof;
        this.hitboxFloor = hitboxFloor;
        this.hitboxHeight = Math.max(12, hitboxFloor - hitboxRoof);

    }

    private void ensureImageLoaded() {
        if (objectImg != null && !objectImg.isRecycled()) return;
        synchronized (this) {
            if (objectImg != null && !objectImg.isRecycled()) return;

            if (atlasBacked) {
                Bitmap atlas = BitmapCache.get(
                        MainActivity.getGameContext(), resourceId
                );
                Bitmap cell = Bitmap.createBitmap(
                        atlas, sourceX, sourceY, sourceWidth, sourceHeight
                );
                objectImg = Bitmap.createScaledBitmap(
                        cell,
                        sourceWidth * GameConstants.Sprite.SCALE_MULTIPLIER,
                        sourceHeight * GameConstants.Sprite.SCALE_MULTIPLIER,
                        false
                );
                if (objectImg != cell) cell.recycle();
                return;
            }

            Bitmap source = BitmapCache.decodeTemporary(
                    MainActivity.getGameContext(), resourceId, null
            );
            if (source == null) return;
            Bitmap cropped = cropTransparentSpace(source);
            objectImg = Bitmap.createScaledBitmap(
                    cropped, sourceWidth, sourceHeight, false
            );
            if (objectImg != cropped) cropped.recycle();
            if (cropped != source && !source.isRecycled()) source.recycle();
        }
    }

    private static Bitmap cropTransparentSpace(Bitmap image) {
        int left = image.getWidth();
        int top = image.getHeight();
        int right = -1;
        int bottom = -1;
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0,
                image.getWidth(), image.getHeight());

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((pixels[y * image.getWidth() + x] >>> 24) > 12) {
                    left = Math.min(left, x);
                    top = Math.min(top, y);
                    right = Math.max(right, x);
                    bottom = Math.max(bottom, y);
                }
            }
        }

        if (right < left || bottom < top) return image;
        return Bitmap.createBitmap(image, left, top, right - left + 1, bottom - top + 1);
    }

    public int getHitboxHeight() {
        return hitboxHeight;
    }

    public int getHitboxWidth() {
        if (this == SHADOW_ROCK || this == BROKEN_SHADOW_PILLAR) {
            return width;
        }
        return width * GameConstants.Sprite.SCALE_MULTIPLIER;
    }

    public Bitmap getObjectImg() {
        ensureImageLoaded();
        return objectImg;
    }

    public int getHitboxRoof() {
        if (this == SHADOW_ROCK || this == BROKEN_SHADOW_PILLAR) {
            return hitboxRoof;
        }
        return hitboxRoof * GameConstants.Sprite.SCALE_MULTIPLIER;
    }
}
