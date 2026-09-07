package com.tutorial.androidgametutorial.effects;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.R;

public class ExplosionEffect {
    private static final int FRAME_COUNT = 5;
    private static final int[] FRAME_RES_IDS = {
        R.drawable.hits_11,
        R.drawable.hits_12,
        R.drawable.hits_13,
        R.drawable.hits_14,
        R.drawable.hits_15
    };
    private static Bitmap[] frames;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private static final long FRAME_DURATION = 50; // ms per frame
    private boolean active = true;
    private PointF pos;
    private final float renderSize;
    private final RectF destination = new RectF();
    static {
        frames = new Bitmap[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = BitmapFactory.decodeResource(
                MainActivity.getGameContext().getResources(),
                FRAME_RES_IDS[i]
            );
        }

    }

    public ExplosionEffect(PointF pos) {
        this(pos, 96f);
    }

    public ExplosionEffect(PointF pos, float renderSize) {
        this.pos = pos;
        this.renderSize = renderSize;
        this.lastFrameTime = System.currentTimeMillis();
    }

    public void update() {
        if (!active) return;
        long now = System.currentTimeMillis();
        if (now - lastFrameTime > FRAME_DURATION) {
            currentFrame++;
            lastFrameTime = now;
            if (currentFrame >= FRAME_COUNT) {
                active = false;
            }
        }
    }

    public void render(Canvas c, float cameraX, float cameraY) {
        if (!active) return;
        Bitmap frame = frames[Math.min(currentFrame, FRAME_COUNT - 1)];
        if (frame != null) {
            float centerX = pos.x + cameraX;
            float centerY = pos.y + cameraY;
            destination.set(
                    centerX - renderSize / 2f,
                    centerY - renderSize / 2f,
                    centerX + renderSize / 2f,
                    centerY + renderSize / 2f
            );
            c.drawBitmap(frame, null, destination, null);
        }
    }

    public boolean isActive() {
        return active;
    }

}
