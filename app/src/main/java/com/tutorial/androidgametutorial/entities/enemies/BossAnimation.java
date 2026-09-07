package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;

import com.tutorial.androidgametutorial.R;

/** Boss map 3 dÃ¹ng 4 cá»™t: down, up, left, right. */
public enum BossAnimation {

    IDLE(R.drawable.boss3_walk_8x4, 10, 0, 1),
    WALK(R.drawable.boss3_walk_8x4, 10, 0, 8),
    PREPARE_ATTACK(R.drawable.boss3_attack_8x4, 8, 0, 1),
    ATTACK(R.drawable.boss3_attack_8x4, 8, 0, 8),
    HURT(R.drawable.boss3_walk_8x4, 10, 0, 1),
    DEAD(R.drawable.boss3_death_8x4, 8, 0, 8);

    private final int resourceId;
    private final int sheetRows;
    private final int startRow;
    private final int frameCount;
    private volatile Bitmap[][] sprites;

    BossAnimation(int resourceId, int sheetRows, int startRow, int frameCount) {
        this.resourceId = resourceId;
        this.sheetRows = sheetRows;
        this.startRow = startRow;
        this.frameCount = frameCount;
    }

    public Bitmap[][] getSprites() {
        ensureLoaded();
        return sprites;
    }

    private void ensureLoaded() {
        if (sprites != null) return;
        synchronized (this) {
            if (sprites != null) return;

            // Idle/hurt and prepare reuse frames already owned by the complete
            // walk/attack animations instead of decoding the same sheet again.
            if (this == IDLE || this == HURT) {
                sprites = firstFrameOf(WALK.getSprites());
            } else if (this == PREPARE_ATTACK) {
                sprites = firstFrameOf(ATTACK.getSprites());
            } else {
                sprites = DirectionalSpriteLoader.load(
                        resourceId, sheetRows, startRow, frameCount, true
                );
            }
        }
    }

    private static Bitmap[][] firstFrameOf(Bitmap[][] source) {
        Bitmap[][] result = new Bitmap[source.length][1];
        for (int direction = 0; direction < source.length; direction++) {
            result[direction][0] = source[direction][0];
        }
        return result;
    }
}
