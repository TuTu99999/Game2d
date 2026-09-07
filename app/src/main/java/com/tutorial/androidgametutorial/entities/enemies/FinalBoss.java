package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.environments.GameMap;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;

import java.util.ArrayList;
import java.util.Iterator;

public class FinalBoss {

    public static final float ATTACK_RANGE = 165f;
    private static final float DRAW_SCALE = 1.3f;
    private static final float ATTACK_HIT_RANGE = 195f;
    private static final float MOVE_SPEED = 150f;
    private static final long SPELL_COOLDOWN = 2800L;
    private static final long DAMAGE_COOLDOWN = 80L;

    private final RectF hitbox;
    private final Paint spritePaint = new Paint();
    private final ArrayList<FinalBossSpell> spells = new ArrayList<>();
    private FinalBossAnimation animation = FinalBossAnimation.IDLE;
    private int frame;
    private int maxHealth = 400;
    private int currentHealth = 400;
    private int attackDamage = 35;
    private long lastFrameTime = System.currentTimeMillis();
    private long lastSpellTime = System.currentTimeMillis();
    private long lastDamageTime;
    private int faceDir = GameConstants.Face_Dir.DOWN;
    private boolean attackChecked;
    private boolean spellCreated;
    private boolean animationFinished;
    private boolean active = true;
    private boolean deathAnimationFinished;
    private int obstacleDirection = 1;

    public FinalBoss(PointF position) {
        hitbox = new RectF(position.x, position.y,
                position.x + 82f, position.y + 82f);
        spritePaint.setFilterBitmap(false);
    }

    public void update(double delta, long now, Player player,
                       float cameraX, float cameraY, GameMap map) {
        updateSpells(now, player, cameraX, cameraY);
        updateAnimation(now);

        if (!active) return;

        if (animation == FinalBossAnimation.ATTACK) {
            if (!attackChecked && frame >= 3) {
                attackChecked = true;
                damagePlayerIfClose(player, cameraX, cameraY);
            }
            if (isAnimationFinished()) setAnimation(FinalBossAnimation.IDLE);
            return;
        }

        if (animation == FinalBossAnimation.CAST) {
            if (!spellCreated && frame >= 4) {
                spellCreated = true;
                spells.add(new FinalBossSpell(
                        player.getHitbox().centerX() - cameraX,
                        player.getHitbox().centerY() - cameraY,
                        25
                ));
            }
            if (isAnimationFinished()) setAnimation(FinalBossAnimation.IDLE);
            return;
        }

        if (animation == FinalBossAnimation.HURT) {
            if (isAnimationFinished()) setAnimation(FinalBossAnimation.IDLE);
            return;
        }

        float playerWorldX = player.getHitbox().centerX() - cameraX;
        float playerWorldY = player.getHitbox().centerY() - cameraY;
        float dx = playerWorldX - hitbox.centerX();
        float dy = playerWorldY - hitbox.centerY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        updateFaceDirection(dx, dy);

        if (distance <= ATTACK_RANGE) {
            setAnimation(FinalBossAnimation.ATTACK);
        } else if (distance <= 720f && now - lastSpellTime >= SPELL_COOLDOWN) {
            lastSpellTime = now;
            setAnimation(FinalBossAnimation.CAST);
        } else {
            setAnimation(FinalBossAnimation.WALK);
            moveToward(dx, dy, distance, (float) delta, map);
        }
    }

    public void shiftTimers(long pausedDuration) {
        if (pausedDuration <= 0L) return;
        lastFrameTime += pausedDuration;
        lastSpellTime += pausedDuration;
        if (lastDamageTime > 0L) lastDamageTime += pausedDuration;
    }

    private void updateAnimation(long now) {
        if (now - lastFrameTime < getFrameTime()) return;
        lastFrameTime = now;

        if (frame < animation.getFrameCount() - 1) {
            frame++;
        } else if (animation.isLooping()) {
            frame = 0;
        } else if (animation == FinalBossAnimation.DEATH) {
            // Frame cuá»‘i Ä‘Æ°á»£c giá»¯ Ä‘á»§ má»™t nhá»‹p rá»“i má»›i chuyá»ƒn mÃ n.
            deathAnimationFinished = true;
        } else {
            animationFinished = true;
        }
    }

    private boolean isAnimationFinished() {
        return animationFinished;
    }

    private long getFrameTime() {
        return switch (animation) {
            case WALK -> 90L;
            case ATTACK -> 105L;
            case CAST -> 115L;
            case DEATH -> 220L;
            case HURT -> 160L;
            default -> 180L;
        };
    }

    private void moveToward(float dx, float dy, float distance,
                            float delta, GameMap map) {
        if (distance < 1f) return;
        float step = Math.min(12f, MOVE_SPEED * Math.min(delta, 0.05f));
        float moveX = dx / distance * step;
        float moveY = dy / distance * step;

        if (Math.abs(dx) > Math.abs(dy)) {
            if (!tryMove(moveX, 0, map) && !tryMove(0, moveY, map)) {
                moveAroundObstacle(false, step, map);
            }
        } else if (!tryMove(0, moveY, map) && !tryMove(moveX, 0, map)) {
            moveAroundObstacle(true, step, map);
        }
    }

    private boolean tryMove(float x, float y, GameMap map) {
        if (!HelpMethods.CanWalkHere(hitbox, x, y, map)) return false;
        updateFaceDirection(x, y);
        hitbox.offset(x, y);
        return true;
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

    private void moveAroundObstacle(boolean horizontal, float step, GameMap map) {
        float moveX = horizontal ? obstacleDirection * step : 0f;
        float moveY = horizontal ? 0f : obstacleDirection * step;
        if (!tryMove(moveX, moveY, map)) {
            obstacleDirection *= -1;
            tryMove(-moveX, -moveY, map);
        }
    }

    private void damagePlayerIfClose(Player player, float cameraX, float cameraY) {
        float playerX = player.getHitbox().centerX() - cameraX;
        float playerY = player.getHitbox().centerY() - cameraY;
        float dx = playerX - hitbox.centerX();
        float dy = playerY - hitbox.centerY();
        if (dx * dx + dy * dy <= ATTACK_HIT_RANGE * ATTACK_HIT_RANGE) {
            player.damageCharacter(attackDamage);
        }
    }

    private void updateSpells(long now, Player player, float cameraX, float cameraY) {
        Iterator<FinalBossSpell> iterator = spells.iterator();
        while (iterator.hasNext()) {
            FinalBossSpell spell = iterator.next();
            spell.update(now, player, cameraX, cameraY);
            if (!spell.isActive()) iterator.remove();
        }
    }

    public void drawSpells(Canvas canvas, float cameraX, float cameraY) {
        for (FinalBossSpell spell : spells) {
            spell.draw(canvas, cameraX, cameraY);
        }
    }

    public void draw(Canvas canvas, float cameraX, float cameraY) {
        Bitmap image = animation.getFrame(faceDir, frame);
        float anchorX = hitbox.centerX() + cameraX;
        float anchorY = hitbox.bottom + cameraY + 6f;
        float scale = Math.min(210f * DRAW_SCALE / image.getWidth(),
                190f * DRAW_SCALE / image.getHeight());
        float width = image.getWidth() * scale;
        float height = image.getHeight() * scale;
        RectF target = new RectF(
                anchorX - width / 2f,
                anchorY - height,
                anchorX + width / 2f,
                anchorY
        );

        spritePaint.setAlpha(animation == FinalBossAnimation.HURT ? 155 : 255);
        canvas.drawBitmap(image, null, target, spritePaint);
        spritePaint.setAlpha(255);
    }

    private void setAnimation(FinalBossAnimation newAnimation) {
        if (animation == newAnimation) return;
        animation = newAnimation;
        frame = 0;
        animationFinished = false;
        lastFrameTime = System.currentTimeMillis();
        attackChecked = false;
        spellCreated = false;
    }

    public void damage(int damage) {
        long now = System.currentTimeMillis();
        if (!active || now - lastDamageTime < DAMAGE_COOLDOWN) return;
        lastDamageTime = now;

        currentHealth = Math.max(0, currentHealth - damage);
        if (currentHealth == 0) {
            active = false;
            deathAnimationFinished = false;
            setAnimation(FinalBossAnimation.DEATH);
        } else if (animation == FinalBossAnimation.IDLE
                || animation == FinalBossAnimation.WALK) {
            setAnimation(FinalBossAnimation.HURT);
        }
    }

    public RectF getHitbox() {
        return hitbox;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDeathAnimationFinished() {
        return deathAnimationFinished;
    }
}
