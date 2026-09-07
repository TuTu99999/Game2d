package com.tutorial.androidgametutorial.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.SparseArray;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.main.MainActivity;

public final class LoadoutIcons {

    private static final int COLUMNS = 4;
    private static final int ROWS = 3;
    private static Bitmap atlas;
    private static final SparseArray<Bitmap> resourceIcons = new SparseArray<>();
    private static final SparseArray<Bitmap> atlasIcons = new SparseArray<>();

    private LoadoutIcons() {
    }

    private static Bitmap getAtlas() {
        if (atlas == null) {
            atlas = BitmapCache.get(
                    MainActivity.getGameContext(), R.drawable.loadout_icons
            );
        }
        return atlas;
    }

    public static void draw(Canvas canvas, int column, int row,
                            RectF destination, Paint paint) {
        Bitmap image = getIconBitmap(column, row);
        if (image == null) return;
        canvas.drawBitmap(image, null, fitInside(image, destination), paint);
    }

    public static Bitmap getIconBitmap(int column, int row) {
        Bitmap image = getAtlas();
        if (image == null) return null;

        int safeColumn = Math.max(0, Math.min(COLUMNS - 1, column));
        int safeRow = Math.max(0, Math.min(ROWS - 1, row));
        int key = safeRow * COLUMNS + safeColumn;
        Bitmap icon = atlasIcons.get(key);
        if (icon != null) return icon;

        int cellWidth = image.getWidth() / COLUMNS;
        int cellHeight = image.getHeight() / ROWS;
        Bitmap cell = Bitmap.createBitmap(image,
                safeColumn * cellWidth,
                safeRow * cellHeight,
                cellWidth,
                cellHeight);
        icon = trimTransparentPixels(cell);
        if (icon != cell) cell.recycle();
        atlasIcons.put(key, icon);
        return icon;
    }

    public static void drawResource(Canvas canvas, int resourceId,
                                    RectF destination, Paint paint) {
        Bitmap image = resourceIcons.get(resourceId);
        if (image == null) {
            image = BitmapCache.get(MainActivity.getGameContext(), resourceId);
            if (image == null) return;
            resourceIcons.put(resourceId, image);
        }

        canvas.drawBitmap(image, null, fitInside(image, destination), paint);
    }

    private static RectF fitInside(Bitmap image, RectF destination) {
        float scale = Math.min(destination.width() / image.getWidth(),
                destination.height() / image.getHeight());
        float width = image.getWidth() * scale;
        float height = image.getHeight() * scale;
        return new RectF(
                destination.centerX() - width / 2f,
                destination.centerY() - height / 2f,
                destination.centerX() + width / 2f,
                destination.centerY() + height / 2f
        );
    }

    private static Bitmap trimTransparentPixels(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        int left = width;
        int top = height;
        int right = -1;
        int bottom = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((pixels[y * width + x] >>> 24) == 0) continue;
                left = Math.min(left, x);
                top = Math.min(top, y);
                right = Math.max(right, x);
                bottom = Math.max(bottom, y);
            }
        }

        if (right < left || bottom < top) return source;
        return Bitmap.createBitmap(source, left, top,
                right - left + 1, bottom - top + 1);
    }
}
