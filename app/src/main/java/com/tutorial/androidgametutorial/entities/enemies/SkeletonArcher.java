package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.entities.GameCharacters;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.environments.GameMap;
import com.tutorial.androidgametutorial.gamestates.Playing;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.main.MainActivity;

public class SkeletonArcher extends Skeleton {

    private static final int GRID_SIZE = 4;
    private static final long SHOOT_DURATION = 760L;
    private static final long SHOOT_COOLDOWN = 1450L;
    private static volatile Bitmap spriteSheet;

    private final Paint spritePaint = new Paint();
    private final Rect sourceRect = new Rect();
    private final RectF targetRect = new RectF();
    private boolean shooting;
    private boolean arrowReleased;
    private boolean moving;
    private boolean activated;
    private long shootingStarted;
    private long lastShotTime = System.currentTimeMillis();
    private long deathStarted;
    private int strafeDirection = 1;
    private int obstacleDirection = 1;

    public SkeletonArcher(PointF position) {
        super(position, GameCharacters.SKELETON);
        applyDifficulty(Game.Difficulty.HARD);
        spritePaint.setFilterBitmap(false);
    }

    @Override
    public void applyDifficulty(Game.Difficulty difficulty) {
        setStartHealth(100);
        setDamage(15);
    }

    @Override
    public void update(double delta, GameMap map, Player player,
                       float cameraX, float cameraY, Playing playing) {
        if (!isActive()) return;

        long now = System.currentTimeMillis();
        if (!activated) {
            activated = true;
            lastShotTime = now;
        }
        float playerX = player.getHitbox().centerX() - cameraX;
        float playerY = player.getHitbox().centerY() - cameraY;
        float dx = playerX - getHitbox().centerX();
        float dy = playerY - getHitbox().centerY();
        float distance = Math.max(1f, (float) Math.sqrt(dx * dx + dy * dy));

        setFaceDir(dx >= 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT);

        if (shooting) {
            moving = false;
            long shootTime = now - shootingStarted;
            if (!arrowReleased && shootTime >= 390L) {
                arrowReleased = true;
                playing.addEnemyArrow(new EnemyArrow(
                        new PointF(getHitbox().centerX(), getHitbox().centerY() - 12f),
                        new PointF(playerX, playerY),
                        getDamage()
                ));
            }
            if (shootTime >= SHOOT_DURATION) {
                shooting = false;
                lastShotTime = now;
            }
            return;
        }

        if (distance <= 720f && now - lastShotTime >= SHOOT_COOLDOWN) {
            shooting = true;
            arrowReleased = false;
            shootingStarted = now;
            moving = false;
            return;
        }

        moveAtRangedDistance(delta, dx, dy, distance, map, now);
    }

    private void moveAtRangedDistance(double delta, float dx, float dy,
                                      float distance, GameMap map, long now) {
        float step = Math.min(9f, 145f * (float) Math.min(delta, 0.05));
        float moveX;
        float moveY;

        if (distance < 270f) {
            moveX = -dx / distance * step;
            moveY = -dy / distance * step;
        } else if (distance > 520f) {
            moveX = dx / distance * step;
            moveY = dy / distance * step;
        } else {
            if ((now / 2200L) % 2 == 0) strafeDirection = 1;
            else strafeDirection = -1;
            moveX = -dy / distance * step * strafeDirection;
            moveY = dx / distance * step * strafeDirection;
        }

        moving = tryMove(moveX, moveY, map)
                || tryMove(moveX, 0f, map)
                || tryMove(0f, moveY, map);

        if (!moving) {
            float sideStep = obstacleDirection * step;
            moving = tryMove(sideStep, 0f, map) || tryMove(0f, sideStep, map);
            if (!moving) obstacleDirection *= -1;
        }
    }

    private boolean tryMove(float x, float y, GameMap map) {
        if (Math.abs(x) < 0.01f && Math.abs(y) < 0.01f) return false;
        if (!HelpMethods.CanWalkHere(getHitbox(), x, y, map)) return false;
        getHitbox().offset(x, y);
        return true;
    }

    @Override
    public boolean isPreparingAttack() {
        // Prevent Playing from starting the normal Skeleton melee attack.
        return true;
    }

    @Override
    public void setSkeletonInactive() {
        if (!isActive()) return;
        super.setSkeletonInactive();
        deathStarted = System.currentTimeMillis();
        shooting = false;
        moving = false;
    }

    public boolean isVisible() {
        return isActive() || System.currentTimeMillis() - deathStarted < 900L;
    }

    public void draw(Canvas canvas, float cameraX, float cameraY) {
        preloadSprite();
        if (spriteSheet == null || !isVisible()) return;

        int row;
        int frame;
        long now = System.currentTimeMillis();
        if (!isActive()) {
            row = 3;
            frame = Math.min(3, (int) ((now - deathStarted) / 190L));
        } else if (shooting) {
            row = 2;
            frame = Math.min(3, (int) ((now - shootingStarted) / 190L));
        } else if (moving) {
            row = 1;
            frame = (int) ((now / 170L) % 4);
        } else {
            row = 0;
            frame = (int) ((now / 230L) % 4);
        }

        setSourceRect(frame, row);
        float centerX = getHitbox().centerX() + cameraX;
        float bottom = getHitbox().bottom + cameraY + 12f;
        float drawWidth = 138f * 1.3f;
        float drawHeight = 134f * 1.3f;
        targetRect.set(
                centerX - drawWidth / 2f,
                bottom - drawHeight,
                centerX + drawWidth / 2f,
                bottom
        );

        canvas.save();
        if (getFaceDir() == GameConstants.Face_Dir.RIGHT) {
            canvas.scale(-1f, 1f, centerX, bottom);
        }
        canvas.drawBitmap(spriteSheet, sourceRect, targetRect, spritePaint);
        canvas.restore();
    }

    private void setSourceRect(int column, int row) {
        int left = Math.round(column * spriteSheet.getWidth() / (float) GRID_SIZE);
        int right = Math.round((column + 1) * spriteSheet.getWidth() / (float) GRID_SIZE);
        int top = Math.round(row * spriteSheet.getHeight() / (float) GRID_SIZE);
        int bottom = Math.round((row + 1) * spriteSheet.getHeight() / (float) GRID_SIZE);
        sourceRect.set(left, top, right, bottom);
    }

    public static void preloadSprite() {
        if (spriteSheet != null) return;
        synchronized (SkeletonArcher.class) {
            if (spriteSheet != null) return;
            spriteSheet = BitmapCache.get(
                    MainActivity.getGameContext(),
                    R.drawable.skeleton_archer_spritesheet
            );
        }
    }
}
