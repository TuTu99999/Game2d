package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.environments.GameMap;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;

public class Boss {

    public static final float ATTACK_START_RANGE = 145f;
    public static final float DRAW_WIDTH = 180f;
    public static final float DRAW_HEIGHT = 150f;
    private static final float DRAW_SCALE = 1.3f;
    private static final float ATTACK_HIT_RANGE = 175f;
    private static final long PREPARE_DURATION = 250L;
    private static final long HURT_DURATION = 240L;
    private static final long WALK_FRAME_DURATION = 90L;
    private static final long ATTACK_FRAME_DURATION = 105L;
    private static final long DEATH_FRAME_DURATION = 220L;
    private static final int[] DOWN_ATTACK_FRAME_ORDER = {0, 0, 0, 3, 4, 5, 6, 7};

    private final PointF position;
    private final RectF hitbox;
    private final Paint spritePaint = new Paint();

    private float speed = 2.0f;
    private int obstacleDirection = 1;
    private int faceDir = GameConstants.Face_Dir.DOWN;

    private Bitmap[][] currentSprites;
    private int currentFrame;
    private long lastFrameTime;
    private long stateStartTime;
    private BossState state;

    private int attackDamage = 35;
    private int maxHealth = 200;
    private int currentHealth = 200;
    private boolean attackChecked;
    private boolean active = true;
    private boolean deathAnimationFinished;
    private boolean animationFinished;

    public Boss(PointF position) {
        this.position = new PointF(position.x, position.y);
        hitbox = new RectF(
                position.x + 18f,
                position.y + 48f,
                position.x + 78f,
                position.y + 94f
        );
        spritePaint.setFilterBitmap(false);
        setState(BossState.IDLE);
    }

    public void applyDifficulty(boolean hardMode) {
        // Táº¡m thá»i Ä‘á»ƒ Hard cÃ³ sá»©c máº¡nh ngang Easy, nhÆ°ng váº«n má»Ÿ map 4.
        attackDamage = 35;
        maxHealth = 200;
        currentHealth = maxHealth;
        active = true;
        deathAnimationFinished = false;
    }

    public void update(long nowMillis, Player targetPlayer, float cameraX, float cameraY) {
        updateAnimation(nowMillis);
        if (state == BossState.DEAD || !active) return;

        switch (state) {
            case PREPARE_ATTACK -> {
                if (nowMillis - stateStartTime >= PREPARE_DURATION) {
                    setState(BossState.ATTACK);
                }
            }
            case ATTACK -> {
                performAttack(targetPlayer, cameraX, cameraY);
                if (animationFinished) {
                    setState(BossState.IDLE);
                }
            }
            case HURT -> {
                if (nowMillis - stateStartTime >= HURT_DURATION) {
                    setState(BossState.IDLE);
                }
            }
            default -> {
                // IDLE và WALK được điều khiển trong Playing.
            }
        }
    }

    private void updateAnimation(long nowMillis) {
        if (currentSprites == null || currentSprites.length == 0) return;
        Bitmap[] frames = currentSprites[Math.max(0, Math.min(3, faceDir))];
        long frameDuration = getFrameDuration();
        if (frames.length == 0 || nowMillis - lastFrameTime < frameDuration) return;

        if (currentFrame < frames.length - 1) {
            currentFrame++;
        } else if (state == BossState.DEAD) {
            // Chá»‰ bÃ¡o cháº¿t xong sau khi frame cuá»‘i Ä‘Ã£ hiá»‡n Ä‘á»§ má»™t nhá»‹p.
            deathAnimationFinished = true;
        } else if (state == BossState.PREPARE_ATTACK
                || state == BossState.ATTACK
                || state == BossState.HURT) {
            animationFinished = true;
        } else {
            currentFrame = 0;
        }
        lastFrameTime = nowMillis;
    }

    private long getFrameDuration() {
        return switch (state) {
            case WALK -> WALK_FRAME_DURATION;
            case ATTACK -> ATTACK_FRAME_DURATION;
            case DEAD -> DEATH_FRAME_DURATION;
            case HURT -> 150L;
            default -> 180L;
        };
    }

    public void startAttackToward(float targetX, float targetY) {
        if (!active || (state != BossState.IDLE && state != BossState.WALK)) return;
        updateFaceDirection(targetX - hitbox.centerX(), targetY - hitbox.centerY());
        setState(BossState.PREPARE_ATTACK);
    }

    public void moveToward(float targetX, float targetY, GameMap gameMap) {
        if (!active || (state != BossState.IDLE && state != BossState.WALK)) return;

        float distanceX = targetX - hitbox.centerX();
        float distanceY = targetY - hitbox.centerY();
        float distance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
        if (distance < 1f) return;

        setState(BossState.WALK);
        updateFaceDirection(distanceX, distanceY);
        float moveX = distanceX / distance * speed;
        float moveY = distanceY / distance * speed;

        if (Math.abs(distanceX) > Math.abs(distanceY)) {
            if (!tryMove(moveX, 0f, gameMap) && !tryMove(0f, moveY, gameMap)) {
                moveAroundObstacle(false, gameMap);
            }
        } else if (!tryMove(0f, moveY, gameMap) && !tryMove(moveX, 0f, gameMap)) {
            moveAroundObstacle(true, gameMap);
        }
    }

    private boolean tryMove(float moveX, float moveY, GameMap gameMap) {
        if (Math.abs(moveX) < 0.01f && Math.abs(moveY) < 0.01f) return false;
        if (!HelpMethods.CanWalkHere(hitbox, moveX, moveY, gameMap)) return false;

        updateFaceDirection(moveX, moveY);
        position.offset(moveX, moveY);
        hitbox.offset(moveX, moveY);
        return true;
    }

    private void moveAroundObstacle(boolean horizontal, GameMap gameMap) {
        float moveX = horizontal ? obstacleDirection * speed : 0f;
        float moveY = horizontal ? 0f : obstacleDirection * speed;
        if (!tryMove(moveX, moveY, gameMap)) {
            obstacleDirection *= -1;
            tryMove(-moveX, -moveY, gameMap);
        }
    }

    private void updateFaceDirection(float dx, float dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            faceDir = dx < 0f
                    ? GameConstants.Face_Dir.LEFT
                    : GameConstants.Face_Dir.RIGHT;
        } else {
            faceDir = dy < 0f
                    ? GameConstants.Face_Dir.UP
                    : GameConstants.Face_Dir.DOWN;
        }
    }

    private void performAttack(Player targetPlayer, float cameraX, float cameraY) {
        boolean attackMomentReached = currentFrame >= 4;
        if (attackChecked || !attackMomentReached) return;

        attackChecked = true;
        float playerWorldX = targetPlayer.getHitbox().centerX() - cameraX;
        float playerWorldY = targetPlayer.getHitbox().centerY() - cameraY;
        float dx = playerWorldX - hitbox.centerX();
        float dy = playerWorldY - hitbox.centerY();
        if (dx * dx + dy * dy <= ATTACK_HIT_RANGE * ATTACK_HIT_RANGE) {
            targetPlayer.damageCharacter(attackDamage);
        }
    }

    public void draw(Canvas canvas, float cameraX, float cameraY) {
        if (currentSprites == null || currentSprites.length == 0) return;
        Bitmap[] frames = currentSprites[Math.max(0, Math.min(3, faceDir))];
        if (frames.length == 0) return;
        int renderFrame = Math.min(currentFrame, frames.length - 1);
        if (state == BossState.ATTACK && faceDir == GameConstants.Face_Dir.DOWN) {
            renderFrame = DOWN_ATTACK_FRAME_ORDER[Math.min(
                    currentFrame, DOWN_ATTACK_FRAME_ORDER.length - 1)];
        }
        Bitmap frame = frames[Math.min(renderFrame, frames.length - 1)];
        if (frame == null) return;

        float centerX = hitbox.centerX() + cameraX;
        float bottom = hitbox.bottom + cameraY + 4f;
        float scale = Math.min(
                DRAW_WIDTH * DRAW_SCALE / frame.getWidth(),
                DRAW_HEIGHT * DRAW_SCALE / frame.getHeight()
        );
        float drawWidth = frame.getWidth() * scale;
        float drawHeight = frame.getHeight() * scale;
        RectF destination = new RectF(
                centerX - drawWidth / 2f,
                bottom - drawHeight,
                centerX + drawWidth / 2f,
                bottom
        );

        spritePaint.setAlpha(255);
        if (state == BossState.HURT) {
            destination.offset(currentFrame % 2 == 0 ? -5f : 5f, 0f);
            spritePaint.setAlpha(175);
        }
        canvas.drawBitmap(frame, null, destination, spritePaint);
        spritePaint.setAlpha(255);
    }

    public void setState(BossState newState) {
        if (newState == null || (state == newState && currentSprites != null)) return;
        state = newState;
        stateStartTime = System.currentTimeMillis();

        switch (newState) {
            case IDLE -> setAnimation(BossAnimation.IDLE);
            case WALK -> setAnimation(BossAnimation.WALK);
            case PREPARE_ATTACK -> setAnimation(BossAnimation.PREPARE_ATTACK);
            case ATTACK -> {
                attackChecked = false;
                setAnimation(BossAnimation.ATTACK);
            }
            case HURT -> setAnimation(BossAnimation.HURT);
            case DEAD -> setAnimation(BossAnimation.DEAD);
        }
    }

    private void setAnimation(BossAnimation animation) {
        currentSprites = animation.getSprites();
        currentFrame = 0;
        animationFinished = false;
        lastFrameTime = System.currentTimeMillis();
    }

    public void damage(int damage) {
        if (!active) return;
        currentHealth = Math.max(0, currentHealth - damage);
        if (currentHealth == 0) {
            active = false;
            deathAnimationFinished = false;
            setState(BossState.DEAD);
        } else if (state == BossState.IDLE || state == BossState.WALK) {
            setState(BossState.HURT);
        }
    }

    public PointF getPosition() { return position; }
    public RectF getHitbox() { return hitbox; }
    public BossState getState() { return state; }
    public int getMaxHealth() { return maxHealth; }
    public int getCurrentHealth() { return currentHealth; }
    public boolean isActive() { return active; }
    public boolean isDeathAnimationFinished() { return deathAnimationFinished; }
    public int getFaceDir() { return faceDir; }
    public void setSpeed(float speed) { this.speed = speed; }
    public Bitmap[][] getSprites() { return currentSprites; }
    public int getCurrentFrame() { return currentFrame; }

    public void shiftTimers(long pausedDuration) {
        if (pausedDuration <= 0L) return;
        lastFrameTime += pausedDuration;
        stateStartTime += pausedDuration;
    }

    // Giữ API cũ để các đoạn code khác không bị ảnh hưởng.
    public void setFacingRight(boolean facingRight) {
        faceDir = facingRight ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT;
    }

    public boolean isFacingRight() {
        return faceDir == GameConstants.Face_Dir.RIGHT;
    }
}
