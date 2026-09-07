package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.entities.GameCharacters;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.main.MainActivity;

public class ShadowWraith extends Skeleton {

    private static volatile Bitmap sprite;
    private static final Paint SPRITE_PAINT = new Paint();

    public ShadowWraith(PointF position) {
        super(position, GameCharacters.SKELETON);
        applyDifficulty(Game.Difficulty.HARD);
    }

    @Override
    public void applyDifficulty(Game.Difficulty difficulty) {
        setStartHealth(100);
        setDamage(15);
    }

    public static void preloadSprite() {
        if (sprite != null) return;
        synchronized (ShadowWraith.class) {
            if (sprite != null) return;

            Bitmap original = BitmapCache.decodeTemporary(
                    MainActivity.getGameContext(), R.drawable.shadow_wraith, null
            );
            if (original == null) return;
            Bitmap cropped = cropTransparentSpace(original);
            sprite = Bitmap.createScaledBitmap(cropped, 108, 126, false);
            if (sprite != cropped) cropped.recycle();
            if (cropped != original && !original.isRecycled()) original.recycle();
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

    public void draw(Canvas canvas, float cameraX, float cameraY) {
        preloadSprite();
        if (sprite == null) return;

        float bob = (float) Math.sin(System.currentTimeMillis() / 170.0) * 5f;
        float centerX = getHitbox().centerX() + cameraX;
        float bottom = getHitbox().bottom + cameraY + 8f + bob;
        float width = sprite.getWidth() * 1.3f;
        float height = sprite.getHeight() * 1.3f;
        RectF target = new RectF(
                centerX - width / 2f,
                bottom - height,
                centerX + width / 2f,
                bottom
        );

        int alpha = isAttacking() ? 255 : 220 + (int) (20 * Math.sin(System.currentTimeMillis() / 210.0));
        SPRITE_PAINT.setAlpha(Math.max(185, Math.min(255, alpha)));

        canvas.save();
        if (getFaceDir() == GameConstants.Face_Dir.RIGHT) {
            canvas.scale(-1f, 1f, centerX, bottom);
        }
        canvas.drawBitmap(sprite, null, target, SPRITE_PAINT);
        canvas.restore();
        SPRITE_PAINT.setAlpha(255);
    }
}
