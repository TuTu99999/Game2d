package com.tutorial.androidgametutorial.entities;

import static com.tutorial.androidgametutorial.helpers.GameConstants.Sprite.SCALE_MULTIPLIER;

import android.graphics.Bitmap;
import android.graphics.PointF;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum Buildings implements BitmapMethods {


    HOUSE_ONE(0, 0, 64, 48, 23, 42, 12, 36),
    HOUSE_TWO(64, 4, 62, 44, 23, 36, 11, 31),
    HOUSE_SIX(304, 0, 64, 48, 39, 40, 18, 35),
    SHADOW_SHRINE(R.drawable.shadow_shrine, 370, 270, 198, 258);


    volatile Bitmap houseImg;
    PointF doorwayPoint;
    int hitboxRoof, hitboxFloor, hitboxHeight, hitboxWidth;
    private final int resourceId;
    private final int sourceX;
    private final int sourceY;
    private final int sourceWidth;
    private final int sourceHeight;
    private final boolean atlasBacked;


    Buildings(int x, int y, int width, int height, int doorwayX, int doorwayY, int hitboxRoof, int hitboxFloor) {
        this.resourceId = R.drawable.buildings_atlas;
        this.sourceX = x;
        this.sourceY = y;
        this.sourceWidth = width;
        this.sourceHeight = height;
        this.atlasBacked = true;

        this.hitboxRoof = hitboxRoof * SCALE_MULTIPLIER;
        this.hitboxFloor = hitboxFloor * SCALE_MULTIPLIER;
        this.hitboxHeight = this.hitboxFloor - this.hitboxRoof;
        this.hitboxWidth = width * SCALE_MULTIPLIER;

        doorwayPoint = new PointF(doorwayX * SCALE_MULTIPLIER, doorwayY * SCALE_MULTIPLIER);


    }

    Buildings(int resourceId, int drawWidth, int drawHeight,
              int hitboxRoof, int hitboxFloor) {
        this.resourceId = resourceId;
        this.sourceX = 0;
        this.sourceY = 0;
        this.sourceWidth = drawWidth;
        this.sourceHeight = drawHeight;
        this.atlasBacked = false;
        this.hitboxRoof = hitboxRoof;
        this.hitboxFloor = hitboxFloor;
        this.hitboxHeight = Math.max(20, hitboxFloor - hitboxRoof);
        this.hitboxWidth = drawWidth;
        this.doorwayPoint = new PointF(drawWidth / 2f, hitboxFloor);

    }

    private void ensureImageLoaded() {
        if (houseImg != null && !houseImg.isRecycled()) return;
        synchronized (this) {
            if (houseImg != null && !houseImg.isRecycled()) return;

            if (atlasBacked) {
                Bitmap atlas = BitmapCache.get(
                        MainActivity.getGameContext(), resourceId
                );
                Bitmap cell = Bitmap.createBitmap(
                        atlas, sourceX, sourceY, sourceWidth, sourceHeight
                );
                houseImg = Bitmap.createScaledBitmap(
                        cell,
                        sourceWidth * SCALE_MULTIPLIER,
                        sourceHeight * SCALE_MULTIPLIER,
                        false
                );
                if (houseImg != cell) cell.recycle();
                return;
            }

            Bitmap source = BitmapCache.decodeTemporary(
                    MainActivity.getGameContext(), resourceId, null
            );
            if (source == null) return;
            Bitmap cropped = cropTransparentSpace(source);
            houseImg = Bitmap.createScaledBitmap(
                    cropped, sourceWidth, sourceHeight, false
            );
            if (houseImg != cropped) cropped.recycle();
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

    public PointF getDoorwayPoint() {
        return doorwayPoint;
    }

    public int getHitboxRoof() {
        return hitboxRoof;
    }


    public Bitmap getHouseImg() {
        ensureImageLoaded();
        return houseImg;
    }
}
