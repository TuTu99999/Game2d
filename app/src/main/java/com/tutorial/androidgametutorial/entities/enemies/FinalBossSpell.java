package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.entities.Player;

public class FinalBossSpell {

    private static final long FRAME_TIME = 85L;
    private static final float SPELL_SCALE = 1.8f;
    private static final float HIT_RADIUS = 92f * SPELL_SCALE;
    private static final float DRAW_WIDTH = 210f * SPELL_SCALE;
    private static final float DRAW_HEIGHT = 140f * SPELL_SCALE;

    private final float worldX;
    private final float worldY;
    private final int damage;
    private final Paint paint = new Paint();
    private int frame;
    private long lastFrameTime;
    private boolean damageChecked;
    private boolean active = true;

    public FinalBossSpell(float worldX, float worldY, int damage) {
        this.worldX = worldX;
        this.worldY = worldY;
        this.damage = damage;
        this.lastFrameTime = System.currentTimeMillis();
    }

    public void update(long now, Player player, float cameraX, float cameraY) {
        if (!active) return;

        if (now - lastFrameTime >= FRAME_TIME) {
            frame++;
            lastFrameTime = now;
            if (frame >= FinalBossAnimation.SPELL.getFrameCount()) {
                frame = FinalBossAnimation.SPELL.getFrameCount() - 1;
                active = false;
                return;
            }
        }

        if (!damageChecked && frame >= 4) {
            damageChecked = true;
            float playerWorldX = player.getHitbox().centerX() - cameraX;
            float playerWorldY = player.getHitbox().centerY() - cameraY;
            float dx = playerWorldX - worldX;
            float dy = playerWorldY - worldY;
            if (dx * dx + dy * dy <= HIT_RADIUS * HIT_RADIUS) {
                player.damageCharacter(damage);
            }
        }
    }

    public void draw(Canvas canvas, float cameraX, float cameraY) {
        if (!active) return;

        Bitmap image = FinalBossAnimation.SPELL.getFrame(frame);
        RectF target = new RectF(
                worldX + cameraX - DRAW_WIDTH / 2f,
                worldY + cameraY - DRAW_HEIGHT + 28f,
                worldX + cameraX + DRAW_WIDTH / 2f,
                worldY + cameraY + 28f
        );
        canvas.drawBitmap(image, null, target, paint);
    }

    public boolean isActive() {
        return active;
    }
}
