package com.tutorial.androidgametutorial.entities.enemies;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

import android.graphics.PointF;

import com.tutorial.androidgametutorial.entities.Character;
import com.tutorial.androidgametutorial.entities.GameCharacters;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.entities.BoomSprites;
import com.tutorial.androidgametutorial.environments.GameMap;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;
import com.tutorial.androidgametutorial.main.Game;

import java.util.Random;

public class Boom extends Character {
    private long lastDirChange = System.currentTimeMillis();
    private Random rand = new Random();
    private boolean moving = true, preparingAttack;
    private boolean chasing = false;
    private Player targetPlayer;
    private float cameraX, cameraY;
    private float normalSpeed = 180f; // Giảm tốc độ bình thường
    private float chaseSpeed = 700f;        // Tăng tốc độ khi đuổi người chơi (cao nhất trong 3 loại)
    private float chaseRange = 1000f;       // Tăng phạm vi phát hiện
    private float aggressiveChaseRange = 1500f; // Phạm vi đuổi rất tích cực (cao nhất)
    private int obstacleDirection = rand.nextBoolean() ? 1 : -1;

    // Boom attack system
    private boolean isExploding = false;
    private long explosionStartTime;
    private long explosionDuration = 500; // 0.5 giây
    private int currentExplosionFrame = 0;
    private int[] explosionFrames = {0, 1, 2, 3, 4, 5, 6}; // boom_smile đến boom_bum_6
    private boolean explosionDamageDealt = false;
    private static BoomSprites boomSprites = new BoomSprites();
    private com.tutorial.androidgametutorial.gamestates.Playing playing;

    public Boom(PointF pos) {
        super(pos, GameCharacters.BOOM);
        applyDifficulty(Game.Difficulty.EASY);
    }

    public void applyDifficulty(Game.Difficulty difficulty) {
        setStartHealth(80);
        setDamage(25);
    }

    public void update(double delta, GameMap gameMap) {
        if (moving) {
            updateMove(delta, gameMap);
            updateAnimation();
        }
        if (preparingAttack) {
            checkTimeToAttackTimer();
        }
        if (isExploding) {
            updateExplosion();
        }
    }

    public void update(double delta, GameMap gameMap, Player player, float cameraX, float cameraY) {
        this.targetPlayer = player;
        this.cameraX = cameraX;
        this.cameraY = cameraY;

        float distanceToPlayer = getDistanceToPlayer(player, cameraX, cameraY);

        // Luôn luôn chase người chơi trong phạm vi rất xa, chỉ dừng khi đang nổ
        if (distanceToPlayer <= aggressiveChaseRange && !preparingAttack && !isExploding) {
            chasing = true;
        } else if (distanceToPlayer > aggressiveChaseRange * 2f) {
            chasing = false;
        }

        // Nếu ở gần người chơi thì luôn chase
        if (distanceToPlayer <= chaseRange) {
            chasing = true;
        }

        if (moving) {
            updateMove(delta, gameMap);
            updateAnimation();
        }
        if (preparingAttack) {
            checkTimeToAttackTimer();
        }
        if (isExploding) {
            updateExplosion();
        }
    }

    public void update(double delta, GameMap gameMap, Player player, float cameraX, float cameraY, com.tutorial.androidgametutorial.gamestates.Playing playing) {
        this.targetPlayer = player;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.playing = playing;

        // Điều chỉnh behavior theo độ khó - CHỈ SET STATS MỘT LẦN KHI TẠO
        boolean shouldChase = false;

        if (shouldChase) {
            // Chế độ khó: có chase, stats cao hơn
            normalSpeed = 180f;
            chaseSpeed = 700f;
            chaseRange = 1000f;
            // KHÔNG reset health nữa - chỉ set damage
            setDamage(60); // Tăng sát thương ở chế độ khó (Boom có sát thương cao nhất)
        } else {
            // Chế độ dễ: không chase, stats thấp hơn
            normalSpeed = 140f;
            chaseSpeed = 140f; // Không tăng tốc khi chase
            chaseRange = 0f;   // Không chase
            // KHÔNG reset health nữa - chỉ set damage
            setDamage(25);       // Giảm sát thương ở chế độ dễ
        }

        if (moving) {
            if (shouldChase) {
                updateMoveWithChase(delta, gameMap);
            } else {
                updateMoveNoChase(delta, gameMap);
            }
            updateAnimation();
        }
        if (preparingAttack) {
            checkTimeToAttackTimer();
        }
        if (isExploding) {
            updateExplosion();
        }
    }

    private void updateMove(double delta, GameMap gameMap) {
        float currentSpeed = chasing ? chaseSpeed : normalSpeed;
        float deltaChange = (float) (delta * currentSpeed);

        if (chasing && targetPlayer != null && !isExploding) {
            moveTowardsPlayer(deltaChange, gameMap);
        } else if (!isExploding) {
            moveRandomly(delta, gameMap, deltaChange);
        }
    }

    private void updateMoveWithChase(double delta, GameMap gameMap) {
        float distanceToPlayer = getDistanceToPlayer(targetPlayer, cameraX, cameraY);

        // Luôn luôn chase người chơi trong phạm vi rất xa, chỉ dừng khi đang nổ
        if (!preparingAttack && !isExploding) {
            chasing = true;
        }

        // Nếu ở gần người chơi thì luôn chase
        if (distanceToPlayer <= chaseRange) {
            chasing = true;
        } else if (distanceToPlayer > chaseRange * 1.5f) {
            chasing = false;
        }

        float currentSpeed = chasing ? chaseSpeed : normalSpeed;
        float deltaChange = (float) (delta * currentSpeed);

        if (chasing && targetPlayer != null && !isExploding) {
            moveTowardsPlayer(deltaChange, gameMap);
        } else if (!isExploding) {
            moveRandomly(delta, gameMap, deltaChange);
        }
    }

    private void updateMoveNoChase(double delta, GameMap gameMap) {
        // Chế độ dễ: chỉ di chuyển random, không chase
        chasing = false;
        if (!isExploding) {
            float deltaChange = (float) (delta * normalSpeed);
            moveRandomly(delta, gameMap, deltaChange);
        }
    }

    private void updateExplosion() {
        long elapsed = System.currentTimeMillis() - explosionStartTime;
        
        // Cập nhật frame dựa trên thời gian
        int frameIndex = (int) ((elapsed * explosionFrames.length) / explosionDuration);
        if (frameIndex >= explosionFrames.length) {
            frameIndex = explosionFrames.length - 1;
        }
        currentExplosionFrame = explosionFrames[frameIndex];

        // Gây sát thương một lần duy nhất ở giữa quá trình nổ
        if (!explosionDamageDealt && elapsed >= explosionDuration / 2) {
            dealExplosionDamage();
            explosionDamageDealt = true;
        }

        // Kết thúc nổ
        if (elapsed >= explosionDuration) {
            isExploding = false;
            moving = true;
            explosionDamageDealt = false;
            currentExplosionFrame = 0;
            // Boom chết sau khi nổ
            setBoomInactive();
        }
    }

    private void dealExplosionDamage() {
        if (targetPlayer != null) {
            // Damage depends on the selected difficulty.
            targetPlayer.damageCharacter(getDamage());
        }
        
        // Thêm ExplosionEffect khi Boom nổ
        if (playing != null) {
            playing.addExplosionEffect(
                    new com.tutorial.androidgametutorial.effects.ExplosionEffect(
                            new PointF(hitbox.centerX(), hitbox.centerY()),
                            GameConstants.Sprite.SIZE * 1.2f
                    )
            );
            playing.playBoomExplosionSound(); // Phát âm thanh nổ
        }
    }

    public void prepareAttack(Player player, float cameraX, float cameraY) {
        explosionStartTime = System.currentTimeMillis();
        isExploding = true;
        moving = false;
        preparingAttack = false;
        explosionDamageDealt = false;
        currentExplosionFrame = 0;
        turnTowardsPlayer(player, cameraX, cameraY);
    }

    private void turnTowardsPlayer(Player player, float cameraX, float cameraY) {
        float xDelta = hitbox.left - (player.getHitbox().left - cameraX);
        float yDelta = hitbox.top - (player.getHitbox().top - cameraY);

        if (Math.abs(xDelta) > Math.abs(yDelta)) {
            if (hitbox.left < (player.getHitbox().left - cameraX))
                faceDir = GameConstants.Face_Dir.RIGHT;
            else faceDir = GameConstants.Face_Dir.LEFT;
        } else {
            if (hitbox.top < (player.getHitbox().top - cameraY))
                faceDir = GameConstants.Face_Dir.DOWN;
            else faceDir = GameConstants.Face_Dir.UP;
        }
    }

    private float getDistanceToPlayer(Player player, float cameraX, float cameraY) {
        float playerX = player.getHitbox().left - cameraX;
        float playerY = player.getHitbox().top - cameraY;
        float boomX = hitbox.left;
        float boomY = hitbox.top;

        float deltaX = playerX - boomX;
        float deltaY = playerY - boomY;

        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    private void moveTowardsPlayer(float deltaChange, GameMap gameMap) {
        float playerX = targetPlayer.getHitbox().left - cameraX;
        float playerY = targetPlayer.getHitbox().top - cameraY;
        float boomX = hitbox.left;
        float boomY = hitbox.top;

        float deltaX = playerX - boomX;
        float deltaY = playerY - boomY;

        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (distance > 0) {
            deltaX = (deltaX / distance) * deltaChange;
            deltaY = (deltaY / distance) * deltaChange;
        }

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (!moveHorizontal(deltaX, gameMap)) {
                moveAroundHorizontalObstacle(deltaChange, gameMap);
            }
        } else if (!moveVertical(deltaY, gameMap)) {
            moveAroundVerticalObstacle(deltaChange, gameMap);
        }
    }

    private boolean moveHorizontal(float amount, GameMap gameMap) {
        if (Math.abs(amount) < 0.01f || !canWalkHere(hitbox, amount, 0, gameMap)) {
            return false;
        }

        hitbox.offset(amount, 0);
        faceDir = amount > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT;
        return true;
    }

    private boolean moveVertical(float amount, GameMap gameMap) {
        if (Math.abs(amount) < 0.01f || !canWalkHere(hitbox, 0, amount, gameMap)) {
            return false;
        }

        hitbox.offset(0, amount);
        faceDir = amount > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP;
        return true;
    }

    private void moveAroundHorizontalObstacle(float step, GameMap gameMap) {
        float verticalStep = obstacleDirection * step;
        if (!moveVertical(verticalStep, gameMap)) {
            obstacleDirection *= -1;
            moveVertical(-verticalStep, gameMap);
        }
    }

    private void moveAroundVerticalObstacle(float step, GameMap gameMap) {
        float horizontalStep = obstacleDirection * step;
        if (!moveHorizontal(horizontalStep, gameMap)) {
            obstacleDirection *= -1;
            moveHorizontal(-horizontalStep, gameMap);
        }
    }

    private void moveRandomly(double delta, GameMap gameMap, float deltaChange) {
        if (System.currentTimeMillis() - lastDirChange >= 3000) {
            faceDir = rand.nextInt(4);
            lastDirChange = System.currentTimeMillis();
        }

        switch (faceDir) {
            case GameConstants.Face_Dir.DOWN:
                if (canWalkHere(hitbox, 0, deltaChange, gameMap)) {
                    hitbox.top += deltaChange;
                    hitbox.bottom += deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.UP;
                break;

            case GameConstants.Face_Dir.UP:
                if (canWalkHere(hitbox, 0, -deltaChange, gameMap)) {
                    hitbox.top -= deltaChange;
                    hitbox.bottom -= deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.DOWN;
                break;

            case GameConstants.Face_Dir.RIGHT:
                if (canWalkHere(hitbox, deltaChange, 0, gameMap)) {
                    hitbox.left += deltaChange;
                    hitbox.right += deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.LEFT;
                break;

            case GameConstants.Face_Dir.LEFT:
                if (canWalkHere(hitbox, -deltaChange, 0, gameMap)) {
                    hitbox.left -= deltaChange;
                    hitbox.right -= deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.RIGHT;
                break;
        }
    }

    private boolean canWalkHere(android.graphics.RectF hitbox, float deltaX, float deltaY, GameMap gameMap) {
        return HelpMethods.CanWalkHere(hitbox, deltaX, deltaY, gameMap);
    }

    private void checkTimeToAttackTimer() {
        // Logic đơn giản cho timer attack
        if (System.currentTimeMillis() - explosionStartTime >= 1000) { // 1 giây
            isExploding = true;
            preparingAttack = false;
            explosionStartTime = System.currentTimeMillis();
        }
    }

    public boolean isPreparingAttack() {
        return preparingAttack;
    }

    public void setBoomInactive() {
        active = false;
    }

    public boolean isChasing() {
        return chasing;
    }

    public void setChasing(boolean chasing) {
        this.chasing = chasing;
    }

    public boolean isExploding() {
        return isExploding;
    }

    public int getCurrentExplosionFrame() {
        return currentExplosionFrame;
    }

    @Override
    public int getAniIndex() {
        if (isExploding) {
            return currentExplosionFrame;
        }
        return super.getAniIndex();
    }
    
    // Override để sử dụng BoomSprites thay vì GameCharacters
    public android.graphics.Bitmap getBoomSprite() {
        if (isExploding) {
            return boomSprites.getExplosionSprite(currentExplosionFrame);
        } else {
            return boomSprites.getMoveSprite(faceDir);
        }
    }

    public float getMovementBobOffset() {
        if (!moving || isExploding) return 0f;
        return (aniIndex == 1 || aniIndex == 3) ? -5f : 0f;
    }

    public void setPlaying(com.tutorial.androidgametutorial.gamestates.Playing playing) {
        this.playing = playing;
    }
}
