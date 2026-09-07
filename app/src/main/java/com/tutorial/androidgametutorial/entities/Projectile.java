package com.tutorial.androidgametutorial.entities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.R;

public class Projectile {
    public enum VisualType {
        CHARGED, PULSE, COMET, OVERLOAD
    }

    private PointF pos;
    private float vx, vy;
    private int damage;
    private boolean active = true;
    private float speed;
    private float radius = 15; // bán kính va chạm

    private static Bitmap[] chargedFrames;
    private static Bitmap[] pulseFrames;
    private static final int[] CHARGED_FRAME_RES_IDS = {
            R.drawable.charged1, R.drawable.charged2, R.drawable.charged3, R.drawable.charged4,
            R.drawable.charged5, R.drawable.charged6
    };
    private static final int[] PULSE_FRAME_RES_IDS = {
            R.drawable.pulse1, R.drawable.pulse2, R.drawable.pulse3, R.drawable.pulse4
    };
    private final VisualType visualType;
    private final boolean slowsEnemy;
    private final Paint trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double spawnDelaySeconds;
    private int currentFrame = 0;
    private long lastFrameTime = 0;
    private static final long FRAME_DURATION = 80; // ms per frame

    static {
        chargedFrames = new Bitmap[CHARGED_FRAME_RES_IDS.length];
        for (int i = 0; i < CHARGED_FRAME_RES_IDS.length; i++) {
            chargedFrames[i] = BitmapFactory.decodeResource(
                MainActivity.getGameContext().getResources(),
                CHARGED_FRAME_RES_IDS[i]
            );
        }
        pulseFrames = new Bitmap[PULSE_FRAME_RES_IDS.length];
        for (int i = 0; i < PULSE_FRAME_RES_IDS.length; i++) {
            pulseFrames[i] = BitmapFactory.decodeResource(
                    MainActivity.getGameContext().getResources(),
                    PULSE_FRAME_RES_IDS[i]
            );
        }
    }

    public Projectile(PointF start, PointF target, int damage, float speed) {
        this(start, target, damage, speed, VisualType.CHARGED, false);
    }

    public Projectile(PointF start, PointF target, int damage, float speed,
                      VisualType visualType, boolean slowsEnemy) {
        this(start, target, damage, speed, visualType, slowsEnemy, 0L);
    }

    public Projectile(PointF start, PointF target, int damage, float speed,
                      VisualType visualType, boolean slowsEnemy, long spawnDelayMs) {
        this.pos = new PointF(start.x, start.y);
        this.damage = damage;
        this.speed = speed;
        this.visualType = visualType;
        this.slowsEnemy = slowsEnemy;
        this.spawnDelaySeconds = spawnDelayMs / 1000.0;

        if (visualType == VisualType.PULSE) {
            trailPaint.setColor(0xAA63E6FF);
        } else if (visualType == VisualType.COMET) {
            trailPaint.setColor(0xAACC72FF);
            radius = 22f;
        } else if (visualType == VisualType.OVERLOAD) {
            trailPaint.setColor(0xDDFFE237);
            radius = 18f;
        } else {
            trailPaint.setColor(0xAA5FCBFF);
        }

        float dx = target.x - start.x;
        float dy = target.y - start.y;
        float len = (float) Math.sqrt(dx * dx + dy * dy);

        // tránh chia cho 0 nếu start == target
        if (len == 0f) {
            vx = 0;
            vy = 0;
        } else {
            vx = dx / len * speed;
            vy = dy / len * speed;
        }
    }

    public void update(double delta) {
        if (spawnDelaySeconds > 0.0) {
            spawnDelaySeconds -= delta;
            if (spawnDelaySeconds > 0.0) return;
            delta = -spawnDelaySeconds;
            spawnDelaySeconds = 0.0;
        }
        // dùng delta (giả sử delta là giây)
        pos.x += vx * (float) delta;
        pos.y += vy * (float) delta;
    }


    public void render(Canvas c, Paint paint, float cameraX, float cameraY) {
        if (!active || !isReady()) return;
        // Animate pulse frames
        long now = System.currentTimeMillis();
        if (now - lastFrameTime > FRAME_DURATION) {
            Bitmap[] frames = visualType == VisualType.PULSE ? pulseFrames : chargedFrames;
            currentFrame = (currentFrame + 1) % frames.length;
            lastFrameTime = now;
        }
        Bitmap[] frames = visualType == VisualType.PULSE ? pulseFrames : chargedFrames;
        Bitmap frame = frames[currentFrame % frames.length];
        if (frame != null) {
            float drawX = pos.x + cameraX;
            float drawY = pos.y + cameraY;
            float velocityLength = (float) Math.sqrt(vx * vx + vy * vy);
            float directionX = velocityLength == 0f ? 0f : vx / velocityLength;
            float directionY = velocityLength == 0f ? 0f : vy / velocityLength;
            int trailCount = visualType == VisualType.COMET ? 6
                    : visualType == VisualType.OVERLOAD ? 5 : 3;
            for (int i = trailCount; i >= 1; i--) {
                float trailDistance = i * 11f;
                float trailRadius = Math.max(3f,
                        radius * (trailCount - i + 1) / (trailCount + 2f));
                trailPaint.setAlpha(Math.max(35, 170 - i * 20));
                c.drawCircle(
                        drawX - directionX * trailDistance,
                        drawY - directionY * trailDistance,
                        trailRadius,
                        trailPaint
                );
            }

            float angle = (float) Math.toDegrees(Math.atan2(vy, vx));
            float scale = visualType == VisualType.COMET ? 1.4f
                    : visualType == VisualType.OVERLOAD ? 1.25f
                    : visualType == VisualType.PULSE ? 1.1f : 1f;
            c.save();
            c.translate(drawX, drawY);
            c.rotate(angle);
            c.scale(scale, scale);
            c.drawBitmap(frame, -frame.getWidth() / 2f, -frame.getHeight() / 2f, null);
            c.restore();
        } else {
            c.drawCircle(pos.x + cameraX, pos.y + cameraY, radius, paint);
        }
    }

    public RectF getHitbox() {
        return new RectF(pos.x - radius, pos.y - radius, pos.x + radius, pos.y + radius);
    }

    public int getDamage() {
        return damage;
    }

    public boolean slowsEnemy() {
        return slowsEnemy;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isReady() {
        return spawnDelaySeconds <= 0.0;
    }

    public void deactivate() {
        active = false;
    }
    public boolean isOutOfBounds(int mapWidth, int mapHeight) {
        return pos.x < 0 || pos.x > mapWidth || pos.y < 0 || pos.y > mapHeight;
    }
}
