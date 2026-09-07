package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum FinalBossAnimation {
    IDLE(R.drawable.final_boss_walk_8x4, 8, 1, true),
    WALK(R.drawable.final_boss_walk_8x4, 8, 8, true),
    ATTACK(R.drawable.final_boss_attack_7x4, 7, 7, false),
    HURT(R.drawable.final_boss_walk_8x4, 8, 1, false),
    DEATH(R.drawable.final_boss_death_8x4, 8, 8, false),
    CAST(R.drawable.final_boss_cast_8x4, 8, 8, false),
    SPELL(48, 8, false);

    private static final int LEGACY_COLUMNS = 8;
    private static final int LEGACY_FRAME_WIDTH = 140;
    private static final int LEGACY_FRAME_HEIGHT = 93;
    private final int frameCount;
    private final boolean looping;
    private final boolean directionalAnimation;
    private final int resourceId;
    private final int sheetRows;
    private volatile Bitmap[][] directionalFrames;
    private final Bitmap[] legacyFrames;
    private final int legacyStartFrame;

    FinalBossAnimation(int resourceId, int sheetRows,
                       int frameCount, boolean looping) {
        this.frameCount = frameCount;
        this.looping = looping;
        this.directionalAnimation = true;
        this.resourceId = resourceId;
        this.sheetRows = sheetRows;
        legacyFrames = null;
        legacyStartFrame = -1;
    }

    FinalBossAnimation(int startFrame, int frameCount, boolean looping) {
        this.frameCount = frameCount;
        this.looping = looping;
        this.directionalAnimation = false;
        this.resourceId = 0;
        this.sheetRows = 0;
        legacyFrames = new Bitmap[frameCount];
        legacyStartFrame = startFrame;
    }

    public Bitmap getFrame(int direction, int index) {
        int safeIndex = Math.max(0, Math.min(frameCount - 1, index));
        if (!directionalAnimation) return getFrame(safeIndex);
        ensureDirectionalFrames();
        int safeDirection = Math.max(0, Math.min(3, direction));
        return directionalFrames[safeDirection][safeIndex];
    }

    public Bitmap getFrame(int index) {
        int safeIndex = Math.max(0, Math.min(frameCount - 1, index));
        if (directionalAnimation) {
            ensureDirectionalFrames();
            return directionalFrames[0][safeIndex];
        }
        ensureLegacyFrames();
        return legacyFrames[safeIndex];
    }

    private void ensureDirectionalFrames() {
        if (directionalFrames != null) return;
        synchronized (this) {
            if (directionalFrames != null) return;

            if (this == IDLE || this == HURT) {
                Bitmap[][] walkFrames = WALK.getDirectionalFrames();
                directionalFrames = new Bitmap[walkFrames.length][1];
                for (int direction = 0; direction < walkFrames.length; direction++) {
                    directionalFrames[direction][0] = walkFrames[direction][0];
                }
            } else {
                directionalFrames = DirectionalSpriteLoader.load(
                        resourceId, sheetRows, 0, frameCount
                );
            }
        }
    }

    private Bitmap[][] getDirectionalFrames() {
        ensureDirectionalFrames();
        return directionalFrames;
    }

    private void ensureLegacyFrames() {
        if (legacyFrames[0] != null) return;
        synchronized (this) {
            if (legacyFrames[0] != null) return;

            Bitmap sheet = BitmapCache.decodeTemporary(
                    MainActivity.getGameContext(),
                    R.drawable.sringer_of_seath_spritsheet,
                    null
            );
            if (sheet == null) {
                throw new IllegalStateException("Unable to decode final boss spell sheet");
            }
            try {
                for (int index = 0; index < frameCount; index++) {
                    int sheetIndex = legacyStartFrame + index;
                    int x = (sheetIndex % LEGACY_COLUMNS) * LEGACY_FRAME_WIDTH;
                    int y = (sheetIndex / LEGACY_COLUMNS) * LEGACY_FRAME_HEIGHT;
                    legacyFrames[index] = Bitmap.createBitmap(
                            sheet, x, y,
                            LEGACY_FRAME_WIDTH, LEGACY_FRAME_HEIGHT
                    );
                }
            } finally {
                sheet.recycle();
            }
        }
    }

    public int getFrameCount() {
        return frameCount;
    }

    public boolean isLooping() {
        return looping;
    }
}
