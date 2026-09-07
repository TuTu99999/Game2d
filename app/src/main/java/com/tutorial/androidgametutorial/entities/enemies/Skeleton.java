package com.tutorial.androidgametutorial.entities.enemies;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

import android.graphics.PointF;

import com.tutorial.androidgametutorial.entities.Character;
import com.tutorial.androidgametutorial.entities.GameCharacters;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.environments.GameMap;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;
import com.tutorial.androidgametutorial.main.Game;

import java.util.Random;

public class Skeleton extends Character {
    private long lastDirChange = System.currentTimeMillis();
    private Random rand = new Random();
    private boolean moving = true, preparingAttack;
    private boolean chasing = false;
    private Player targetPlayer;
    private float cameraX, cameraY;
    private float normalSpeed = 200f;  // Giảm tốc độ bình thường
    private float chaseSpeed = 600f;   // Tăng tốc độ khi đuổi người chơi
    private float chaseRange = 800f;   // Tăng phạm vi phát hiện người chơi
    private float aggressiveChaseRange = 1200f; // Phạm vi đuổi rất tích cực
    private int obstacleDirection = rand.nextBoolean() ? 1 : -1;

    private long timerBeforeAttack, timerAttackDuration;
    private long timeToAttack = 500, timeForAttackDuration = 250;
    private boolean hasDroppedItem = false;


    public Skeleton(PointF pos, GameCharacters type) {
        super(pos, type);
        applyDifficulty(Game.Difficulty.EASY);
    }

    public void applyDifficulty(Game.Difficulty difficulty) {
        setStartHealth(100);
        setDamage(15);
    }

    public void update(double delta, GameMap gameMap) {
        if (moving) {
            updateMove(delta, gameMap);
            updateAnimation();
        }
        if (preparingAttack) {
            checkTimeToAttackTimer();
        }
        if (attacking) {
            updateAttackTimer();
        }
    }

    public void prepareAttack(Player player, float cameraX, float cameraY) {
        timerBeforeAttack = System.currentTimeMillis();
        preparingAttack = true;
        moving = false;
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

    private void updateAttackTimer() {
        if (timerAttackDuration + timeForAttackDuration < System.currentTimeMillis()) {
            setAttacking(false);
            resetAnimation();
            moving = true;
        }
    }

    private void checkTimeToAttackTimer() {
        if (timerBeforeAttack + timeToAttack < System.currentTimeMillis()) {
            setAttacking(true);
            preparingAttack = false;
            timerAttackDuration = System.currentTimeMillis();
        }
    }

    public void update(double delta, GameMap gameMap, Player player, float cameraX, float cameraY, com.tutorial.androidgametutorial.gamestates.Playing playing) {
        this.targetPlayer = player;
        this.cameraX = cameraX;
        this.cameraY = cameraY;

        // Điều chỉnh behavior theo độ khó - CHỈ SET STATS MỘT LẦN KHI TẠO
        boolean shouldChase = false;

        if (shouldChase) {
            // Chế độ khó: có chase, stats cao hơn
            normalSpeed = 200f;
            chaseSpeed = 600f;
            chaseRange = 800f;
            // KHÔNG reset health nữa - chỉ set damage
            setDamage(25); // Tăng sát thương ở chế độ khó
        } else {
            // Chế độ dễ: không chase, stats thấp hơn
            normalSpeed = 150f;
            chaseSpeed = 150f; // Không tăng tốc khi chase
            chaseRange = 0f;   // Không chase
            // KHÔNG reset health nữa - chỉ set damage
            setDamage(15);       // Giảm sát thương ở chế độ dễ
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
        if (attacking) {
            updateAttackTimer();
        }
    }

    private float getDistanceToPlayer(Player player, float cameraX, float cameraY) {
        float playerX = player.getHitbox().left - cameraX;
        float playerY = player.getHitbox().top - cameraY;
        float skeletonX = hitbox.left;
        float skeletonY = hitbox.top;

        float deltaX = playerX - skeletonX;
        float deltaY = playerY - skeletonY;

        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    private void updateMove(double delta, GameMap gameMap) {
        float currentSpeed = chasing ? chaseSpeed : normalSpeed;
        float deltaChange = (float) (delta * currentSpeed);

        if (chasing && targetPlayer != null) {
            moveTowardsPlayer(deltaChange, gameMap);
        } else {
            moveRandomly(delta, gameMap, deltaChange);
        }
    }

    private void moveTowardsPlayer(float deltaChange, GameMap gameMap) {
        float playerX = targetPlayer.getHitbox().left - cameraX;
        float playerY = targetPlayer.getHitbox().top - cameraY;
        float skeletonX = hitbox.left;
        float skeletonY = hitbox.top;

        float deltaX = playerX - skeletonX;
        float deltaY = playerY - skeletonY;

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
        if (Math.abs(amount) < 0.01f || !HelpMethods.CanWalkHere(hitbox, amount, 0, gameMap)) {
            return false;
        }

        hitbox.offset(amount, 0);
        faceDir = amount > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT;
        return true;
    }

    private boolean moveVertical(float amount, GameMap gameMap) {
        if (Math.abs(amount) < 0.01f || !HelpMethods.CanWalkHere(hitbox, 0, amount, gameMap)) {
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
                if (HelpMethods.CanWalkHere(hitbox, 0, deltaChange, gameMap)) {
                    hitbox.top += deltaChange;
                    hitbox.bottom += deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.UP;
                break;

            case GameConstants.Face_Dir.UP:
                if (HelpMethods.CanWalkHere(hitbox, 0, -deltaChange, gameMap)) {
                    hitbox.top -= deltaChange;
                    hitbox.bottom -= deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.DOWN;
                break;

            case GameConstants.Face_Dir.RIGHT:
                if (HelpMethods.CanWalkHere(hitbox, deltaChange, 0, gameMap)) {
                    hitbox.left += deltaChange;
                    hitbox.right += deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.LEFT;
                break;

            case GameConstants.Face_Dir.LEFT:
                if (HelpMethods.CanWalkHere(hitbox, -deltaChange, 0, gameMap)) {
                    hitbox.left -= deltaChange;
                    hitbox.right -= deltaChange;
                } else
                    faceDir = GameConstants.Face_Dir.RIGHT;
                break;
        }
    }

    private void updateMoveWithChase(double delta, GameMap gameMap) {
        float distanceToPlayer = getDistanceToPlayer(targetPlayer, cameraX, cameraY);

        // Luôn luôn chase người chơi trong phạm vi rất xa, chỉ dừng khi đang tấn công
        if (!preparingAttack && !attacking) {
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

        if (chasing && targetPlayer != null) {
            moveTowardsPlayer(deltaChange, gameMap);
        } else {
            moveRandomly(delta, gameMap, deltaChange);
        }
    }

    private void updateMoveNoChase(double delta, GameMap gameMap) {
        // Chế độ dễ: chỉ di chuyển random, không chase
        chasing = false;
        float deltaChange = (float) (delta * normalSpeed);
        moveRandomly(delta, gameMap, deltaChange);
    }

    public boolean isPreparingAttack() {
        return preparingAttack;
    }

    public void setSkeletonInactive() {
        active = false;
    }

    public boolean isChasing() {
        return chasing;
    }

    public void setChasing(boolean chasing) {
        this.chasing = chasing;
    }

    public boolean hasDroppedItem() {
        return hasDroppedItem;
    }

    public void setHasDroppedItem(boolean hasDroppedItem) {
        this.hasDroppedItem = hasDroppedItem;
    }
}
