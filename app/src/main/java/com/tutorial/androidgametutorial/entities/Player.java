package com.tutorial.androidgametutorial.entities;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

import android.graphics.PointF;

import com.tutorial.androidgametutorial.effects.ExplosionEffect;
import com.tutorial.androidgametutorial.gamestates.Playing;
import com.tutorial.androidgametutorial.main.LoadoutManager;

public class Player extends Character {

    private long lastAttackTime;
    private long attackCooldown = 450L;
    private final long[] lastSkillTimes = new long[3];
    private LoadoutManager loadoutManager;
    private float characterSpeedMultiplier = 1.0f;
    private GameCharacters displayCharacter = GameCharacters.PLAYER;

    private int shieldHits;
    private long shieldStartTime;
    private static final long SHIELD_DURATION = 30000L;

    private float itemSpeedMultiplier = 1.0f;
    private long speedBoostStartTime;
    private static final float SPEED_BOOST_MULTIPLIER = 1.2f;
    private static final long SPEED_BOOST_DURATION = 5000L;

    public Player() {
        super(new PointF(GAME_WIDTH / 2f, GAME_HEIGHT / 2f), GameCharacters.PLAYER);
        setStartHealth(600);
    }

    public void update(double delta, boolean movePlayer) {
        if (movePlayer) {
            updateAnimation(5);
        }
        updateAttackState();
        updateWepHitbox();
    }

    public void applyLoadout(LoadoutManager manager) {
        loadoutManager = manager;
        LoadoutManager.CharacterType character = manager.getCharacter();
        LoadoutManager.WeaponType weapon = manager.getWeapon();
        displayCharacter = switch (character) {
            case WARRIOR -> GameCharacters.WARRIOR;
            case ROGUE -> GameCharacters.ROGUE;
            case GUARDIAN -> GameCharacters.GUARDIAN;
            case ADVENTURER -> GameCharacters.ADVENTURER;
        };
        characterSpeedMultiplier = character.getSpeedMultiplier();
        setStartHealth(character.getMaxHealth());
        setDamage(Math.max(1, Math.round(
                weapon.getDamage() * character.getDamageMultiplier()
        )));
        attackCooldown = weapon.getCooldownMs();
    }

    public boolean canAttack() {
        return System.currentTimeMillis() - lastAttackTime >= attackCooldown;
    }

    public void setLastAttackTime() {
        lastAttackTime = System.currentTimeMillis();
    }

    public void setAttackCooldown(long cooldown) {
        attackCooldown = cooldown;
    }

    public float getAttackCooldownRemaining() {
        return getCooldownRemaining(lastAttackTime, attackCooldown);
    }

    public float getSkillCooldownRemaining(int slot) {
        if (loadoutManager == null || slot < 0 || slot >= lastSkillTimes.length) return 0f;
        return getCooldownRemaining(lastSkillTimes[slot],
                loadoutManager.getSkill(slot).getCooldownMs());
    }

    private float getCooldownRemaining(long lastUseTime, long cooldown) {
        long elapsed = System.currentTimeMillis() - lastUseTime;
        if (elapsed >= cooldown) return 0f;
        return 1f - (float) elapsed / cooldown;
    }

    public boolean castSkill(int slot, Playing playing) {
        if (loadoutManager == null || slot < 0 || slot >= lastSkillTimes.length) return false;

        LoadoutManager.SkillType skill = loadoutManager.getSkill(slot);
        long now = System.currentTimeMillis();
        if (now - lastSkillTimes[slot] < skill.getCooldownMs()) return false;
        if (!canSpendHealth(skill.getHealthCost())) return false;

        float worldX = getHitbox().centerX() - playing.getCameraX();
        float worldY = getHitbox().centerY() - playing.getCameraY();
        PointF playerWorldPosition = new PointF(worldX, worldY);

        switch (skill) {
            case CHARGED_BLAST -> {
                PointF target = playing.findNearestEnemyPosition(worldX, worldY, 650f);
                if (target == null) return false;
                playing.addProjectile(new Projectile(
                        playerWorldPosition, target, skill.getDamage(), 520f,
                        Projectile.VisualType.CHARGED, false
                ));
            }
            case SPARK_STORM ->
                    playing.addSparkSkill(new SparkSkill(playerWorldPosition, playing));
            case ARCANE_BURST -> {
                playing.damageEnemiesInRadius(
                        worldX, worldY, 310f, skill.getDamage(), false
                );
                playing.showAreaSkillEffect(worldX, worldY, 310f, skill);
            }
            case FROST_PULSE -> {
                PointF target = playing.findNearestEnemyPosition(worldX, worldY, 700f);
                if (target == null) return false;
                fireFan(playing, playerWorldPosition, target, 3, 18f,
                        skill.getDamage(), 430f, Projectile.VisualType.PULSE, true);
            }
            case SACRIFICE_NOVA -> {
                spendHealth(skill.getHealthCost());
                playing.damageEnemiesInRadius(
                        worldX, worldY, 450f, skill.getDamage(), false
                );
                playing.showAreaSkillEffect(worldX, worldY, 450f, skill);
                playing.addExplosionEffect(new ExplosionEffect(playerWorldPosition, 300f));
            }
            case FLAME_RIFT -> {
                playing.damageEnemiesInRadius(
                        worldX, worldY, 350f, skill.getDamage(), false
                );
                playing.showAreaSkillEffect(worldX, worldY, 350f, skill);
                playing.addExplosionEffect(new ExplosionEffect(playerWorldPosition, 260f));
            }
            case VOID_DRAIN -> {
                playing.damageEnemiesInRadius(
                        worldX, worldY, 300f, skill.getDamage(), false
                );
                playing.showAreaSkillEffect(worldX, worldY, 300f, skill);
                healCharacter(60);
            }
            case GLACIAL_RING -> {
                fireRadial(playing, playerWorldPosition, 8, skill.getDamage(),
                        460f, Projectile.VisualType.PULSE, true);
            }
            case COMET_SHOT -> {
                PointF target = playing.findNearestEnemyPosition(worldX, worldY, 800f);
                if (target == null) return false;
                fireFan(playing, playerWorldPosition, target, 4, 8f,
                        skill.getDamage(), 620f, Projectile.VisualType.COMET, false);
            }
            case OVERLOAD -> {
                PointF target = playing.findNearestEnemyPosition(worldX, worldY, 850f);
                if (target == null) return false;
                spendHealth(skill.getHealthCost());
                fireBurst(playing, playerWorldPosition, target, 6,
                        skill.getDamage(), 760f, 90L);
            }
        }

        lastSkillTimes[slot] = now;
        playing.playSkillSound(skill);
        return true;
    }

    private void fireFan(Playing playing, PointF start, PointF target,
                         int count, float angleStep, int damage, float speed,
                         Projectile.VisualType visualType, boolean slow) {
        float baseAngle = (float) Math.atan2(target.y - start.y, target.x - start.x);
        float firstOffset = -angleStep * (count - 1) / 2f;

        for (int i = 0; i < count; i++) {
            float angle = baseAngle + (float) Math.toRadians(firstOffset + i * angleStep);
            PointF projectileTarget = new PointF(
                    start.x + (float) Math.cos(angle) * 900f,
                    start.y + (float) Math.sin(angle) * 900f
            );
            playing.addProjectile(new Projectile(
                    start, projectileTarget, damage, speed, visualType, slow
            ));
        }
    }

    private void fireRadial(Playing playing, PointF start, int count,
                            int damage, float speed, Projectile.VisualType visualType,
                            boolean slow) {
        for (int i = 0; i < count; i++) {
            float angle = (float) (Math.PI * 2 * i / count);
            PointF projectileTarget = new PointF(
                    start.x + (float) Math.cos(angle) * 850f,
                    start.y + (float) Math.sin(angle) * 850f
            );
            playing.addProjectile(new Projectile(
                    start, projectileTarget, damage, speed, visualType, slow
            ));
        }
    }

    private void fireBurst(Playing playing, PointF start, PointF target,
                           int count, int totalDamage, float speed, long shotDelayMs) {
        int damagePerShot = Math.max(1, Math.round((float) totalDamage / count));
        for (int i = 0; i < count; i++) {
            playing.addProjectile(new Projectile(
                    start, target, damagePerShot, speed,
                    Projectile.VisualType.OVERLOAD, false, i * shotDelayMs
            ));
        }
    }

    public void resetCooldowns() {
        lastAttackTime = 0L;
        for (int i = 0; i < lastSkillTimes.length; i++) {
            lastSkillTimes[i] = 0L;
        }
    }

    public void shiftTimers(long pausedDuration) {
        if (pausedDuration <= 0L) return;
        if (lastAttackTime > 0L) lastAttackTime += pausedDuration;
        for (int i = 0; i < lastSkillTimes.length; i++) {
            if (lastSkillTimes[i] > 0L) lastSkillTimes[i] += pausedDuration;
        }
        if (shieldHits > 0) shieldStartTime += pausedDuration;
        if (itemSpeedMultiplier > 1.0f) speedBoostStartTime += pausedDuration;
    }

    public void useMedipack() {
        healCharacter((int) (getMaxHealth() * 0.3f));
    }

    public void useFish() {
        itemSpeedMultiplier = SPEED_BOOST_MULTIPLIER;
        speedBoostStartTime = System.currentTimeMillis();
    }

    public void useEmptyPot() {
        shieldHits = 3;
        shieldStartTime = System.currentTimeMillis();
    }

    public void updateEffects() {
        long now = System.currentTimeMillis();
        if (shieldHits > 0 && now - shieldStartTime >= SHIELD_DURATION) {
            shieldHits = 0;
        }
        if (itemSpeedMultiplier > 1.0f
                && now - speedBoostStartTime >= SPEED_BOOST_DURATION) {
            itemSpeedMultiplier = 1.0f;
        }
    }

    @Override
    public void damageCharacter(int damage) {
        if (shieldHits > 0) {
            shieldHits--;
            return;
        }
        super.damageCharacter(damage);
    }

    public boolean hasSpeedBoost() {
        return itemSpeedMultiplier > 1.0f;
    }

    public boolean hasShield() {
        return shieldHits > 0;
    }

    public int getShieldHits() {
        return shieldHits;
    }

    public long getSpeedBoostTimeLeft() {
        if (!hasSpeedBoost()) return 0L;
        return Math.max(0L, SPEED_BOOST_DURATION
                - (System.currentTimeMillis() - speedBoostStartTime));
    }

    public float getSpeedMultiplier() {
        return characterSpeedMultiplier * itemSpeedMultiplier;
    }

    public GameCharacters getDisplayCharacter() {
        return displayCharacter;
    }

    public int getDisplayAnimationIndex() {
        if (isAttacking()) {
            int attackFrame = Math.min(2,
                    (int) (getAttackAnimationProgress() * 3f));
            return 4 + attackFrame;
        }
        return displayCharacter.getMovementFrame(getAniIndex());
    }

    public void resetPosition(float x, float y) {
        getHitbox().offsetTo(
                x - getHitbox().width() / 2f,
                y - getHitbox().height() / 2f
        );
    }
}
