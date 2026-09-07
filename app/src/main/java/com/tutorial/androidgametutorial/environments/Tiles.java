package com.tutorial.androidgametutorial.environments;

import android.graphics.Bitmap;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.main.MainActivity;

import java.util.HashMap;
import java.util.Map;

public enum Tiles {

    OUTSIDE(R.drawable.tileset_floor, 22, 26),
    INSIDE(R.drawable.floor_inside, 22, 22),
    SNOW(R.drawable.tileset_floor, 22, 26),
    SHADOW(R.drawable.shadow_floor);

    private final int resourceId;
    private final int columns;
    private final int rows;
    private final boolean singleTexture;
    private volatile Bitmap[] sprites;

    Tiles(int resourceId, int columns, int rows) {
        this.resourceId = resourceId;
        this.columns = columns;
        this.rows = rows;
        this.singleTexture = false;
    }

    Tiles(int resourceId) {
        this.resourceId = resourceId;
        this.columns = 1;
        this.rows = 1;
        this.singleTexture = true;
    }

    public Bitmap getSprite(int id) {
        ensureLoaded();
        return sprites[Math.max(0, Math.min(sprites.length - 1, id))];
    }

    public void preload() {
        ensureLoaded();
    }

    private void ensureLoaded() {
        if (sprites != null) return;
        synchronized (this) {
            if (sprites == null) {
                sprites = SpriteSetCache.get(
                        resourceId, columns, rows, singleTexture
                );
            }
        }
    }

    /** Shares the identical OUTSIDE/SNOW tile set without retaining its sheet. */
    private static final class SpriteSetCache {
        private static final Map<String, Bitmap[]> SETS = new HashMap<>();

        private SpriteSetCache() {
        }

        private static synchronized Bitmap[] get(int resourceId, int columns,
                                                  int rows,
                                                  boolean singleTexture) {
            String key = resourceId + ":" + columns + ":" + rows;
            Bitmap[] cached = SETS.get(key);
            if (cached != null) return cached;

            Bitmap[] loaded = singleTexture
                    ? loadSingleTexture(resourceId)
                    : sliceAtlas(resourceId, columns, rows);
            SETS.put(key, loaded);
            return loaded;
        }

        private static Bitmap[] loadSingleTexture(int resourceId) {
            Bitmap image = BitmapCache.getScaled(
                    MainActivity.getGameContext(),
                    resourceId,
                    GameConstants.Sprite.SIZE,
                    GameConstants.Sprite.SIZE,
                    false
            );
            return new Bitmap[]{image};
        }

        private static Bitmap[] sliceAtlas(int resourceId, int columns,
                                           int rows) {
            Bitmap sheet = BitmapCache.decodeTemporary(
                    MainActivity.getGameContext(), resourceId, null
            );
            if (sheet == null) {
                throw new IllegalStateException(
                        "Unable to decode tile sheet " + resourceId
                );
            }

            Bitmap[] result = new Bitmap[columns * rows];
            try {
                for (int row = 0; row < rows; row++) {
                    for (int column = 0; column < columns; column++) {
                        int index = row * columns + column;
                        Bitmap cell = Bitmap.createBitmap(
                                sheet,
                                GameConstants.Sprite.DEFAULT_SIZE * column,
                                GameConstants.Sprite.DEFAULT_SIZE * row,
                                GameConstants.Sprite.DEFAULT_SIZE,
                                GameConstants.Sprite.DEFAULT_SIZE
                        );
                        Bitmap scaled = Bitmap.createScaledBitmap(
                                cell,
                                GameConstants.Sprite.SIZE,
                                GameConstants.Sprite.SIZE,
                                false
                        );
                        if (scaled != cell) cell.recycle();
                        result[index] = scaled;
                    }
                }
            } finally {
                if (!sheet.isRecycled()) sheet.recycle();
            }
            return result;
        }
    }
}
