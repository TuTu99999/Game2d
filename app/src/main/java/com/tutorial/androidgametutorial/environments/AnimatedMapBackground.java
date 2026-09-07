package com.tutorial.androidgametutorial.environments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.BitmapCache;

import java.util.Random;

public class AnimatedMapBackground {

    private static final int PARTICLE_COUNT = 36;
    private static final float MAP_TRANSITION_SECONDS = 0.8f;

    private final Bitmap[] backgrounds = new Bitmap[5];
    private final Context context;
    private final Particle[] particles = new Particle[PARTICLE_COUNT];
    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF drawRect = new RectF();
    private final RectF particleRect = new RectF();

    private int currentMapLevel = 1;
    private int previousMapLevel = 1;
    private float transitionProgress = 1f;
    private float animationTime = 0f;

    public AnimatedMapBackground(Context context) {
        this.context = context.getApplicationContext();

        Random random = new Random(2026);
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle(
                    random.nextFloat(),
                    random.nextFloat(),
                    0.35f + random.nextFloat() * 0.65f,
                    random.nextFloat() * 6.28f
            );
        }

        borderPaint.setStyle(Paint.Style.STROKE);
    }

    private synchronized Bitmap getBackground(int mapLevel) {
        int safeLevel = Math.max(1, Math.min(4, mapLevel));
        Bitmap bitmap = backgrounds[safeLevel];
        if (bitmap != null && !bitmap.isRecycled()) return bitmap;

        int resourceId = switch (safeLevel) {
            case 2 -> R.drawable.background_snow;
            case 3 -> R.drawable.background_desert;
            case 4 -> R.drawable.background_shadow;
            default -> R.drawable.background_forest;
        };
        bitmap = BitmapCache.get(context, resourceId, Bitmap.Config.RGB_565);
        backgrounds[safeLevel] = bitmap;
        return bitmap;
    }

    public void preload(int mapLevel) {
        getBackground(mapLevel);
    }

    public void update(double delta, int mapLevel) {
        changeMapIfNeeded(mapLevel);

        float frameTime = (float) Math.min(delta, 0.05);
        animationTime += frameTime;
        if (transitionProgress < 1f) {
            transitionProgress = Math.min(
                    1f,
                    transitionProgress + frameTime / MAP_TRANSITION_SECONDS
            );
        }

        for (Particle particle : particles) {
            if (currentMapLevel == 1) {
                particle.x += (0.012f + particle.speed * 0.012f) * frameTime;
                particle.y += Math.sin(animationTime * 1.4f + particle.phase) * 0.004f * frameTime;
            } else if (currentMapLevel == 2) {
                particle.y += (0.06f + particle.speed * 0.08f) * frameTime;
                particle.x += Math.sin(animationTime * 1.8f + particle.phase) * 0.01f * frameTime;
            } else if (currentMapLevel == 3) {
                particle.x += (0.10f + particle.speed * 0.13f) * frameTime;
                particle.y += Math.sin(animationTime * 2f + particle.phase) * 0.006f * frameTime;
            } else {
                particle.y -= (0.025f + particle.speed * 0.035f) * frameTime;
                particle.x += Math.sin(animationTime * 1.4f + particle.phase) * 0.018f * frameTime;
            }

            wrapParticle(particle);
        }
    }

    private void changeMapIfNeeded(int mapLevel) {
        int safeLevel = Math.max(1, Math.min(4, mapLevel));
        if (safeLevel == currentMapLevel) return;

        previousMapLevel = currentMapLevel;
        currentMapLevel = safeLevel;
        transitionProgress = 0f;
    }

    private void wrapParticle(Particle particle) {
        if (particle.x > 1.03f) particle.x = -0.03f;
        if (particle.x < -0.03f) particle.x = 1.03f;
        if (particle.y > 1.03f) particle.y = -0.03f;
        if (particle.y < -0.03f) particle.y = 1.03f;
    }

    public void drawBackground(Canvas canvas, float cameraX, float cameraY, int mapLevel) {
        changeMapIfNeeded(mapLevel);
        canvas.drawColor(getBaseColor(currentMapLevel));

        if (transitionProgress < 1f) {
            drawParallaxBitmap(canvas, getBackground(previousMapLevel), cameraX, cameraY, 255);
            drawParallaxBitmap(
                    canvas,
                    getBackground(currentMapLevel),
                    cameraX,
                    cameraY,
                    Math.round(255 * transitionProgress)
            );
        } else {
            drawParallaxBitmap(canvas, getBackground(currentMapLevel), cameraX, cameraY, 255);
        }

        // Keep scenery quieter than the playable map so gameplay stays readable.
        particlePaint.setColor(Color.argb(getBackdropShadeAlpha(), 7, 14, 20));
        particlePaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), particlePaint);
    }

    private void drawParallaxBitmap(Canvas canvas, Bitmap bitmap,
                                    float cameraX, float cameraY, int alpha) {
        if (bitmap == null) return;

        float overscan = Math.max(canvas.getWidth(), canvas.getHeight()) * 0.08f;
        float scale = Math.max(
                (canvas.getWidth() + overscan * 2f) / bitmap.getWidth(),
                (canvas.getHeight() + overscan * 2f) / bitmap.getHeight()
        );

        float width = bitmap.getWidth() * scale;
        float height = bitmap.getHeight() * scale;
        float extraX = Math.max(0f, width - canvas.getWidth());
        float extraY = Math.max(0f, height - canvas.getHeight());

        float left = -extraX / 2f + cameraX * 0.025f
                + (float) Math.sin(animationTime * 0.08f) * 8f;
        float top = -extraY / 2f + cameraY * 0.015f
                + (float) Math.cos(animationTime * 0.07f) * 5f;

        left = Math.max(canvas.getWidth() - width, Math.min(0f, left));
        top = Math.max(canvas.getHeight() - height, Math.min(0f, top));

        drawRect.set(left, top, left + width, top + height);
        backgroundPaint.setAlpha(alpha);
        canvas.drawBitmap(bitmap, null, drawRect, backgroundPaint);
    }

    public void drawAtmosphere(Canvas canvas) {
        if (currentMapLevel == 1) {
            drawForestAtmosphere(canvas);
        } else if (currentMapLevel == 2) {
            drawSnowAtmosphere(canvas);
        } else if (currentMapLevel == 3) {
            drawDesertAtmosphere(canvas);
        } else {
            drawShadowAtmosphere(canvas);
        }
    }

    private void drawShadowAtmosphere(Canvas canvas) {
        for (int i = 0; i < particles.length; i++) {
            Particle particle = particles[i];
            float x = particle.x * canvas.getWidth();
            float y = particle.y * canvas.getHeight();
            float glow = (float) (0.6f + 0.4f
                    * Math.sin(animationTime * 2.4f + particle.phase));
            particlePaint.setColor(Color.argb((int) (95 * glow), 177, 92, 255));
            canvas.drawCircle(x, y, 2f + particle.speed * 4f, particlePaint);
        }

        particlePaint.setColor(Color.argb(34, 78, 32, 116));
        float fogY = canvas.getHeight() * 0.72f
                + (float) Math.sin(animationTime * 0.35f) * 22f;
        canvas.drawRect(0, fogY, canvas.getWidth(), fogY + 85f, particlePaint);
    }

    private void drawForestAtmosphere(Canvas canvas) {
        for (int i = 0; i < particles.length; i++) {
            Particle particle = particles[i];
            float x = particle.x * canvas.getWidth();
            float y = particle.y * canvas.getHeight();

            if (i % 3 == 0) {
                float glow = (float) (0.55f + 0.45f
                        * Math.sin(animationTime * 3f + particle.phase));
                particlePaint.setColor(Color.argb((int) (105 * glow), 255, 232, 95));
                canvas.drawCircle(x, y, 3f + particle.speed * 3f, particlePaint);
            } else if (i % 4 == 0) {
                particlePaint.setColor(Color.argb(75, 120, 190, 65));
                float size = 5f + particle.speed * 6f;
                particleRect.set(x - size, y - size / 2f, x + size, y + size / 2f);
                canvas.drawOval(particleRect, particlePaint);
            }
        }
    }

    private void drawSnowAtmosphere(Canvas canvas) {
        particlePaint.setColor(Color.argb(145, 235, 250, 255));
        for (Particle particle : particles) {
            float x = particle.x * canvas.getWidth();
            float y = particle.y * canvas.getHeight();
            canvas.drawCircle(x, y, 2f + particle.speed * 4f, particlePaint);
        }

        particlePaint.setColor(Color.argb(28, 220, 245, 255));
        float mistY = canvas.getHeight() * 0.22f
                + (float) Math.sin(animationTime * 0.2f) * 20f;
        canvas.drawRect(0, mistY, canvas.getWidth(), mistY + 55f, particlePaint);
    }

    private void drawDesertAtmosphere(Canvas canvas) {
        particlePaint.setStrokeWidth(2f);
        for (int i = 0; i < particles.length; i++) {
            if (i % 2 != 0) continue;

            Particle particle = particles[i];
            float x = particle.x * canvas.getWidth();
            float y = particle.y * canvas.getHeight();
            float length = 12f + particle.speed * 28f;
            particlePaint.setColor(Color.argb(65, 255, 224, 145));
            canvas.drawLine(x, y, x + length, y + 2f, particlePaint);
        }

        particlePaint.setColor(Color.argb(22, 255, 205, 115));
        float hazeY = canvas.getHeight() * 0.18f
                + (float) Math.sin(animationTime * 0.7f) * 12f;
        canvas.drawRect(0, hazeY, canvas.getWidth(), hazeY + 40f, particlePaint);
    }

    public void drawMapBorder(Canvas canvas, float cameraX, float cameraY,
                              int mapWidth, int mapHeight, boolean outdoorMap) {
        drawRect.set(cameraX, cameraY, cameraX + mapWidth, cameraY + mapHeight);

        if (outdoorMap) {
            drawMapTint(canvas);
            drawSoftEdgeTransition(canvas);
            drawEdgeDecorations(canvas, cameraX, cameraY, mapWidth, mapHeight);
        }

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(outdoorMap ? 4f : 18f);
        borderPaint.setColor(outdoorMap
                ? getBorderColor(currentMapLevel)
                : Color.argb(150, 8, 10, 18));
        canvas.drawRect(drawRect, borderPaint);
    }

    private void drawMapTint(Canvas canvas) {
        borderPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(getMapTintColor(currentMapLevel));
        canvas.drawRect(drawRect, borderPaint);
    }

    private void drawSoftEdgeTransition(Canvas canvas) {
        borderPaint.setStyle(Paint.Style.STROKE);

        // Several soft strokes make the rectangular tile edge fade into scenery.
        for (int layer = 5; layer >= 0; layer--) {
            float width = 10f + layer * 10f;
            int alpha = 18 + (5 - layer) * 8;
            borderPaint.setStrokeWidth(width);
            borderPaint.setColor(getSoftEdgeColor(currentMapLevel, alpha));
            canvas.drawRect(drawRect, borderPaint);
        }
    }

    private void drawEdgeDecorations(Canvas canvas, float cameraX, float cameraY,
                                     int mapWidth, int mapHeight) {
        int spacing = 82;
        int index = 0;

        for (int worldX = spacing / 2; worldX < mapWidth; worldX += spacing) {
            float x = cameraX + worldX;
            float offset = ((index * 37) % 25) - 12f;
            drawDecorationIfVisible(canvas, x, cameraY + offset, index);
            drawDecorationIfVisible(canvas, x, cameraY + mapHeight - offset, index + 11);
            index++;
        }

        for (int worldY = spacing / 2; worldY < mapHeight; worldY += spacing) {
            float y = cameraY + worldY;
            float offset = ((index * 29) % 25) - 12f;
            drawDecorationIfVisible(canvas, cameraX + offset, y, index + 23);
            drawDecorationIfVisible(canvas, cameraX + mapWidth - offset, y, index + 41);
            index++;
        }
    }

    private void drawDecorationIfVisible(Canvas canvas, float x, float y, int index) {
        float margin = 65f;
        if (x < -margin || y < -margin ||
                x > canvas.getWidth() + margin || y > canvas.getHeight() + margin) {
            return;
        }

        if (currentMapLevel == 1) {
            drawBushCluster(canvas, x, y, index);
        } else if (currentMapLevel == 2) {
            drawSnowBank(canvas, x, y, index);
        } else if (currentMapLevel == 3) {
            drawSandDune(canvas, x, y, index);
        } else {
            drawShadowRune(canvas, x, y, index);
        }
    }

    private void drawShadowRune(Canvas canvas, float x, float y, int index) {
        float pulse = 0.7f + (float) Math.sin(animationTime * 2f + index) * 0.2f;
        float radius = 10f + index % 4 * 2f;
        particlePaint.setStyle(Paint.Style.STROKE);
        particlePaint.setStrokeWidth(2f);
        particlePaint.setColor(Color.argb((int) (125 * pulse), 173, 84, 245));
        canvas.drawCircle(x, y, radius, particlePaint);
        canvas.drawLine(x - radius * 0.7f, y, x + radius * 0.7f, y, particlePaint);
        canvas.drawLine(x, y - radius * 0.7f, x, y + radius * 0.7f, particlePaint);
        particlePaint.setStyle(Paint.Style.FILL);
    }

    private void drawBushCluster(Canvas canvas, float x, float y, int index) {
        float sway = (float) Math.sin(animationTime * 1.2f + index) * 2f;
        float size = 13f + index % 4;

        particlePaint.setStyle(Paint.Style.FILL);
        particlePaint.setColor(Color.argb(225, 25, 76, 47));
        canvas.drawCircle(x - size * 0.7f + sway, y + 2f, size, particlePaint);
        canvas.drawCircle(x + size * 0.6f + sway, y + 3f, size * 0.9f, particlePaint);
        canvas.drawCircle(x + sway, y - size * 0.45f, size * 1.05f, particlePaint);

        particlePaint.setColor(Color.argb(210, 75, 139, 61));
        canvas.drawCircle(x - 4f + sway, y - size * 0.55f, size * 0.55f, particlePaint);
    }

    private void drawSnowBank(Canvas canvas, float x, float y, int index) {
        float width = 22f + index % 5 * 2f;

        particlePaint.setStyle(Paint.Style.FILL);
        particlePaint.setColor(Color.argb(210, 86, 155, 193));
        particleRect.set(x - width, y - 5f, x + width, y + 12f);
        canvas.drawOval(particleRect, particlePaint);

        particlePaint.setColor(Color.argb(235, 225, 247, 252));
        particleRect.set(x - width + 2f, y - 10f, x + width - 3f, y + 5f);
        canvas.drawOval(particleRect, particlePaint);

        if (index % 3 == 0) {
            float sparkle = 3f + (float) Math.sin(animationTime * 3f + index) * 1.5f;
            particlePaint.setStrokeWidth(2f);
            particlePaint.setColor(Color.argb(180, 255, 255, 255));
            canvas.drawLine(x, y - 18f - sparkle, x, y - 10f + sparkle, particlePaint);
            canvas.drawLine(x - 4f, y - 14f, x + 4f, y - 14f, particlePaint);
        }
    }

    private void drawSandDune(Canvas canvas, float x, float y, int index) {
        float width = 23f + index % 4 * 3f;

        particlePaint.setStyle(Paint.Style.FILL);
        particlePaint.setColor(Color.argb(215, 172, 92, 42));
        particleRect.set(x - width, y - 3f, x + width, y + 14f);
        canvas.drawOval(particleRect, particlePaint);

        particlePaint.setColor(Color.argb(225, 223, 153, 68));
        particleRect.set(x - width + 2f, y - 8f, x + width - 6f, y + 7f);
        canvas.drawOval(particleRect, particlePaint);

        particlePaint.setColor(Color.argb(210, 126, 65, 37));
        canvas.drawCircle(x + width * 0.35f, y - 2f, 5f + index % 3, particlePaint);
    }

    private int getBaseColor(int mapLevel) {
        if (mapLevel == 2) return Color.rgb(65, 118, 160);
        if (mapLevel == 3) return Color.rgb(150, 83, 38);
        if (mapLevel == 4) return Color.rgb(28, 12, 48);
        return Color.rgb(24, 68, 54);
    }

    private int getBackdropShadeAlpha() {
        if (currentMapLevel == 2) return 34;
        if (currentMapLevel == 3) return 46;
        if (currentMapLevel == 4) return 52;
        return 58;
    }

    private int getMapTintColor(int mapLevel) {
        if (mapLevel == 2) return Color.argb(15, 145, 220, 245);
        if (mapLevel == 3) return Color.argb(13, 245, 174, 77);
        if (mapLevel == 4) return Color.argb(18, 151, 67, 220);
        return Color.argb(11, 69, 145, 65);
    }

    private int getSoftEdgeColor(int mapLevel, int alpha) {
        if (mapLevel == 2) return Color.argb(alpha, 190, 232, 242);
        if (mapLevel == 3) return Color.argb(alpha, 207, 135, 57);
        if (mapLevel == 4) return Color.argb(alpha, 135, 69, 190);
        return Color.argb(alpha, 45, 103, 55);
    }

    private int getBorderColor(int mapLevel) {
        if (mapLevel == 2) return Color.argb(150, 178, 230, 245);
        if (mapLevel == 3) return Color.argb(150, 210, 132, 60);
        if (mapLevel == 4) return Color.argb(175, 158, 82, 224);
        return Color.argb(150, 68, 128, 70);
    }

    private static class Particle {
        private float x;
        private float y;
        private final float speed;
        private final float phase;

        private Particle(float x, float y, float speed, float phase) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.phase = phase;
        }
    }
}
