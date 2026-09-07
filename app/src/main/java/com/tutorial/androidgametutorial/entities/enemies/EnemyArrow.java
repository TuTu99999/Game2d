package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.entities.Building;
import com.tutorial.androidgametutorial.entities.GameObject;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.environments.GameMap;

public class EnemyArrow {

    private static final float ATTACK_SCALE = 1.2f;

    private final PointF position;
    private final float velocityX;
    private final float velocityY;
    private final int damage;
    private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean active = true;

    public EnemyArrow(PointF start, PointF target, int damage) {
        position = new PointF(start.x, start.y);
        this.damage = damage;

        float dx = target.x - start.x;
        float dy = target.y - start.y;
        float distance = Math.max(1f, (float) Math.sqrt(dx * dx + dy * dy));
        float speed = 520f;
        velocityX = dx / distance * speed;
        velocityY = dy / distance * speed;

        arrowPaint.setColor(Color.rgb(197, 118, 255));
        arrowPaint.setStrokeWidth(5f * ATTACK_SCALE);
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public boolean update(double delta, Player player, float cameraX, float cameraY,
                          GameMap map) {
        if (!active) return false;

        position.x += velocityX * (float) delta;
        position.y += velocityY * (float) delta;

        RectF arrowHitbox = getHitbox();
        RectF playerWorldHitbox = new RectF(player.getHitbox());
        playerWorldHitbox.offset(-cameraX, -cameraY);

        if (RectF.intersects(arrowHitbox, playerWorldHitbox)) {
            player.damageCharacter(damage);
            active = false;
            return true;
        }

        if (hitsObstacle(arrowHitbox, map)
                || position.x < 0 || position.y < 0
                || position.x >= map.getMapWidth()
                || position.y >= map.getMapHeight()) {
            active = false;
        }

        return false;
    }

    private boolean hitsObstacle(RectF arrowHitbox, GameMap map) {
        if (map.getGameObjectArrayList() != null) {
            for (GameObject object : map.getGameObjectArrayList()) {
                if (RectF.intersects(arrowHitbox, object.getHitbox())) return true;
            }
        }

        if (map.getBuildingArrayList() != null) {
            for (Building building : map.getBuildingArrayList()) {
                if (RectF.intersects(arrowHitbox, building.getHitbox())) return true;
            }
        }
        return false;
    }

    public void draw(Canvas canvas, float cameraX, float cameraY) {
        if (!active) return;

        float length = 25f * ATTACK_SCALE;
        float speed = Math.max(1f,
                (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY));
        float unitX = velocityX / speed;
        float unitY = velocityY / speed;
        float x = position.x + cameraX;
        float y = position.y + cameraY;

        canvas.drawLine(
                x - unitX * length,
                y - unitY * length,
                x + unitX * 5f * ATTACK_SCALE,
                y + unitY * 5f * ATTACK_SCALE,
                arrowPaint
        );
        canvas.drawCircle(
                x + unitX * 5f * ATTACK_SCALE,
                y + unitY * 5f * ATTACK_SCALE,
                5f * ATTACK_SCALE,
                arrowPaint
        );
    }

    private RectF getHitbox() {
        return new RectF(
                position.x - 9f * ATTACK_SCALE,
                position.y - 9f * ATTACK_SCALE,
                position.x + 9f * ATTACK_SCALE,
                position.y + 9f * ATTACK_SCALE
        );
    }

    public boolean isActive() {
        return active;
    }
}
