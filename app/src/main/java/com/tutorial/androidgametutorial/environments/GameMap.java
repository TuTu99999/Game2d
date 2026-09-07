package com.tutorial.androidgametutorial.environments;


import com.tutorial.androidgametutorial.entities.Building;
import com.tutorial.androidgametutorial.entities.Character;
import com.tutorial.androidgametutorial.entities.GameObject;
import com.tutorial.androidgametutorial.entities.enemies.Boom;
import com.tutorial.androidgametutorial.entities.enemies.Skeleton;
import com.tutorial.androidgametutorial.entities.items.Item;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;


import java.util.ArrayList;

public class GameMap {

    private int[][] spriteIds;
    private Tiles tilesType;
    private ArrayList<Building> buildingArrayList;
    private ArrayList<Doorway> doorwayArrayList;
    private ArrayList<GameObject> gameObjectArrayList;
    private ArrayList<Skeleton> skeletonArrayList;
    private ArrayList<Boom> boomArrayList;

    private ArrayList<Item> itemArrayList;

    public GameMap(int[][] spriteIds, Tiles tilesType, ArrayList<Building> buildingArrayList,
                   ArrayList<GameObject> gameObjectArrayList, ArrayList<Skeleton> skeletonArrayList,
                    ArrayList<Boom> boomArrayList, ArrayList<Item> itemArrayList) {
        this.spriteIds = spriteIds;
        this.tilesType = tilesType;
        this.buildingArrayList = buildingArrayList;
        this.gameObjectArrayList = gameObjectArrayList;
        this.skeletonArrayList = skeletonArrayList;
        this.boomArrayList = boomArrayList;
        this.doorwayArrayList = new ArrayList<>();
        this.itemArrayList = itemArrayList;

        moveEnemiesOutOfBlockedAreas();
    }

    public void moveEnemiesOutOfBlockedAreas() {
        if (skeletonArrayList != null) {
            for (Skeleton skeleton : skeletonArrayList) {
                moveEnemyToWalkablePosition(skeleton);
            }
        }

        if (boomArrayList != null) {
            for (Boom boom : boomArrayList) {
                moveEnemyToWalkablePosition(boom);
            }
        }
    }

    private void moveEnemyToWalkablePosition(Character enemy) {
        if (HelpMethods.CanWalkHere(enemy.getHitbox(), 0, 0, this)) {
            return;
        }

        float margin = GameConstants.Sprite.SIZE;
        float availableWidth = Math.max(1, getMapWidth() - enemy.getHitbox().width() - margin * 2);
        float availableHeight = Math.max(1, getMapHeight() - enemy.getHitbox().height() - margin * 2);

        for (int attempt = 0; attempt < 100; attempt++) {
            float x = margin + (float) Math.random() * availableWidth;
            float y = margin + (float) Math.random() * availableHeight;
            enemy.getHitbox().offsetTo(x, y);

            if (HelpMethods.CanWalkHere(enemy.getHitbox(), 0, 0, this)) {
                return;
            }
        }
    }


    public void addDoorway(Doorway doorway) {
        this.doorwayArrayList.add(doorway);
    }

    public ArrayList<Doorway> getDoorwayArrayList() {
        return doorwayArrayList;
    }

    public ArrayList<Building> getBuildingArrayList() {
        return buildingArrayList;
    }

    public ArrayList<GameObject> getGameObjectArrayList() {
        return gameObjectArrayList;
    }

    public ArrayList<Skeleton> getSkeletonArrayList() {
        return skeletonArrayList;
    }

    public ArrayList<Boom> getBoomArrayList() {
        return boomArrayList;
    }

    public ArrayList<Item> getItemArrayList() {
        return itemArrayList;
    }

    public Tiles getFloorType() {
        return tilesType;
    }

    public int getSpriteID(int xIndex, int yIndex) {
        return spriteIds[yIndex][xIndex];
    }

    public int[][] getSpriteIds() {
        return spriteIds;
    }

    public int getArrayWidth() {
        return spriteIds[0].length;
    }

    public int getArrayHeight() {
        return spriteIds.length;
    }

    public int getMapWidth() {
        return getArrayWidth() * GameConstants.Sprite.SIZE;
    }

    public int getMapHeight() {
        return getArrayHeight() * GameConstants.Sprite.SIZE;
    }


}
