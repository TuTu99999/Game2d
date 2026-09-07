package com.tutorial.androidgametutorial.environments;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.entities.Building;
import com.tutorial.androidgametutorial.entities.Buildings;
import com.tutorial.androidgametutorial.entities.GameCharacters;
import com.tutorial.androidgametutorial.entities.GameObject;
import com.tutorial.androidgametutorial.entities.GameObjects;
import com.tutorial.androidgametutorial.entities.enemies.Boom;
import com.tutorial.androidgametutorial.entities.enemies.ShadowWraith;
import com.tutorial.androidgametutorial.entities.enemies.SkeletonArcher;
import com.tutorial.androidgametutorial.entities.enemies.Skeleton;
import com.tutorial.androidgametutorial.entities.items.Item;
import com.tutorial.androidgametutorial.entities.items.Items;
import com.tutorial.androidgametutorial.gamestates.Playing;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.main.MainActivity;

import java.util.ArrayList;

public class MapManager {

    private GameMap currentMap;
    private GameMap map1; // Original outdoor map
    private GameMap map2; // Snow map
    private GameMap map3; // Desert map
    private GameMap map4; // Hard-only Shadow Realm
    private float cameraX, cameraY;
    private Playing playing;
    private int currentMapLevel = 1;
    private volatile Bitmap shadowFloorTexture;
    private final Paint shadowFloorPaint = new Paint();

    public MapManager(Playing playing) {
        this(playing, false);
    }

    public MapManager(Playing playing, boolean tutorialMode) {
        this.playing = playing;
        if (tutorialMode) {
            initTutorialMap();
        } else {
            initMaps();
        }
    }

    private void initTutorialMap() {
        int rows = 14;
        int columns = 22;
        int[][] tutorialTiles = new int[rows][columns];

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                tutorialTiles[row][column] = 275;
            }
        }

        ArrayList<Building> buildings = new ArrayList<>();
        buildings.add(new Building(new PointF(1500, 300), Buildings.HOUSE_ONE));

        ArrayList<GameObject> objects = new ArrayList<>();
        objects.add(new GameObject(new PointF(650, 350), GameObjects.STATUE_ANGRY_YELLOW));
        objects.add(new GameObject(new PointF(900, 900), GameObjects.BASKET_FULL_RED_FRUIT));
        objects.add(new GameObject(new PointF(1250, 850), GameObjects.OVEN_SNOW_YELLOW));

        ArrayList<Skeleton> skeletons = new ArrayList<>();
        Skeleton tutorialEnemy = new Skeleton(new PointF(1400, 650), GameCharacters.SKELETON);
        tutorialEnemy.applyDifficulty(Game.Difficulty.EASY);
        skeletons.add(tutorialEnemy);

        ArrayList<Boom> booms = new ArrayList<>();
        ArrayList<Item> items = createTutorialItems();

        map1 = new GameMap(
                tutorialTiles,
                Tiles.OUTSIDE,
                buildings,
                objects,
                skeletons,
                booms,
                items
        );
        currentMap = map1;
        currentMapLevel = 0;
    }

    public void resetTutorialMap() {
        if (currentMapLevel != 0) return;

        currentMap = map1;
        currentMap.getSkeletonArrayList().clear();

        Skeleton tutorialEnemy = new Skeleton(new PointF(1400, 650), GameCharacters.SKELETON);
        tutorialEnemy.applyDifficulty(Game.Difficulty.EASY);
        currentMap.getSkeletonArrayList().add(tutorialEnemy);

        currentMap.getBoomArrayList().clear();
        currentMap.getItemArrayList().clear();
        currentMap.getItemArrayList().addAll(createTutorialItems());
    }

    private ArrayList<Item> createTutorialItems() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item(Items.MEDIPACK, new PointF(760, 650)));
        items.add(new Item(Items.FISH, new PointF(1050, 950)));
        return items;
    }

    public void setCameraValues(float cameraX, float cameraY) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
    }

    public boolean canMoveHere(float x, float y) {
        if (x < 0 || y < 0)
            return false;

        if (x >= getMaxWidthCurrentMap() || y >= getMaxHeightCurrentMap())
            return false;

        return true;
    }

    public int getMaxWidthCurrentMap() {
        return currentMap.getArrayWidth() * GameConstants.Sprite.SIZE;
    }

    public int getMaxHeightCurrentMap() {
        return currentMap.getArrayHeight() * GameConstants.Sprite.SIZE;
    }

    public void drawObject(Canvas c, GameObject go) {
        c.drawBitmap(go.getObjectType().getObjectImg(),
                go.getHitbox().left + cameraX,
                go.getHitbox().top - go.getObjectType().getHitboxRoof() + cameraY,
                null);
    }

    public void drawBuilding(Canvas c, Building b) {
        c.drawBitmap(b.getBuildingType().getHouseImg(),
                b.getPos().x + cameraX,
                b.getPos().y - b.getBuildingType().getHitboxRoof() + cameraY,
                null);
    }

    public void drawTiles(Canvas c) {
        int tileSize = GameConstants.Sprite.SIZE;

        if (currentMap.getFloorType() == Tiles.SHADOW) {
            ensureShadowFloorLoaded();
            if (shadowFloorTexture == null) return;
            RectF mapArea = new RectF(
                    cameraX,
                    cameraY,
                    cameraX + currentMap.getMapWidth(),
                    cameraY + currentMap.getMapHeight()
            );
            c.drawBitmap(shadowFloorTexture, null, mapArea, shadowFloorPaint);
            return;
        }

        // Convert the visible screen edges to tile indexes, then clamp them
        // to the map. Tiles outside the camera are skipped completely.
        int firstColumn = Math.max(0, (int) Math.floor(-cameraX / tileSize));
        int lastColumn = Math.min(
                currentMap.getArrayWidth() - 1,
                (int) Math.floor((c.getWidth() - 1 - cameraX) / tileSize)
        );
        int firstRow = Math.max(0, (int) Math.floor(-cameraY / tileSize));
        int lastRow = Math.min(
                currentMap.getArrayHeight() - 1,
                (int) Math.floor((c.getHeight() - 1 - cameraY) / tileSize)
        );

        for (int j = firstRow; j <= lastRow; j++)
            for (int i = firstColumn; i <= lastColumn; i++)
                c.drawBitmap(currentMap.getFloorType().getSprite(currentMap.getSpriteID(i, j)),
                        i * tileSize + cameraX,
                        j * tileSize + cameraY,
                        null);
    }

    public void drawItem(Canvas c, Item item) {
        c.drawBitmap(item.getItemType().getImage(),
                item.getHitbox().left + cameraX,
                item.getHitbox().top + cameraY,
                null);
    }

    public Doorway isPlayerOnDoorway(RectF playerHitbox) {
        for (Doorway doorway : currentMap.getDoorwayArrayList())
            if (doorway.isPlayerInsideDoorway(playerHitbox, cameraX, cameraY))
                return doorway;

        return null;
    }

    public void changeMap(Doorway doorwayTarget) {
        this.currentMap = doorwayTarget.getGameMapLocatedIn();

        float cX = MainActivity.GAME_WIDTH / 2f - doorwayTarget.getPosOfDoorway().x + GameConstants.Sprite.HITBOX_SIZE / 2f;
        float cY = MainActivity.GAME_HEIGHT / 2f - doorwayTarget.getPosOfDoorway().y + GameConstants.Sprite.HITBOX_SIZE / 2f;

        playing.setCameraValues(new PointF(cX, cY));
        cameraX = cX;
        cameraY = cY;

        playing.setDoorwayJustPassed(true);
    }

    public GameMap getCurrentMap() {
        return currentMap;
    }

    /** Loads only immutable render assets; it never changes the active map. */
    public void preloadMapResources(int mapLevel) {
        GameMap targetMap = switch (mapLevel) {
            case 1 -> map1;
            case 2 -> map2;
            case 3 -> map3;
            case 4 -> map4;
            default -> null;
        };
        if (targetMap == null) return;

        if (targetMap.getFloorType() == Tiles.SHADOW) {
            ensureShadowFloorLoaded();
        } else {
            targetMap.getFloorType().preload();
        }

        if (targetMap.getBuildingArrayList() != null) {
            for (Building building : targetMap.getBuildingArrayList()) {
                building.getBuildingType().getHouseImg();
            }
        }
        if (targetMap.getGameObjectArrayList() != null) {
            for (GameObject object : targetMap.getGameObjectArrayList()) {
                object.getObjectType().getObjectImg();
            }
        }
        // Item icons are tiny shared assets and are already resolved when an
        // Item creates its hitbox. Avoid iterating this mutable gameplay list
        // from the preload thread while a run may be reset.
    }

    public int getCurrentMapLevel() {
        return currentMapLevel;
    }

    public void progressToNextMap() {
        if (currentMapLevel >= 1 && currentMapLevel <= 3) {
            // Không mang đạn/skill còn tồn tại của map cũ sang map mới.
            playing.clearTemporaryAttacks();
        }

        if (currentMapLevel == 1) {
            // Move to snow map (map 2)
            currentMapLevel = 2;
            currentMap = map2;
            System.out.println("🏔️ Chuyển sang Map 2 - Snow World!");

            // Reset camera to center of new map
            float cX = MainActivity.GAME_WIDTH / 2f - (currentMap.getMapWidth() / 2f);
            float cY = MainActivity.GAME_HEIGHT / 2f - (currentMap.getMapHeight() / 2f);
            playing.setCameraValues(new PointF(cX, cY));
            cameraX = cX;
            cameraY = cY;
        } else if (currentMapLevel == 2) {
            // Move to desert map (map 3)
            currentMapLevel = 3;
            currentMap = map3;
            System.out.println("🏜️ Chuyển sang Map 3 - Desert World!");

            // Reset camera to center of new map
            float cX = MainActivity.GAME_WIDTH / 2f - (currentMap.getMapWidth() / 2f);
            float cY = MainActivity.GAME_HEIGHT / 2f - (currentMap.getMapHeight() / 2f);
            playing.setCameraValues(new PointF(cX, cY));
            cameraX = cX;
            cameraY = cY;
            playing.spawnBoss();
        } else if (currentMapLevel == 3) {
            currentMapLevel = 4;
            currentMap = map4;

            float cX = MainActivity.GAME_WIDTH / 2f - (currentMap.getMapWidth() / 2f);
            float cY = MainActivity.GAME_HEIGHT / 2f - (currentMap.getMapHeight() / 2f);
            playing.setCameraValues(new PointF(cX, cY));
            cameraX = cX;
            cameraY = cY;
            playing.spawnFinalBoss();
        }
    }

    // Method to reset map progression back to Map 1
    public void resetToMap1() {
        currentMapLevel = 1;
        currentMap = map1;
        System.out.println("🗺️ Reset về Map 1");
    }

    public void resetAllMaps() {
        resetMap(map1, 5, 3, createMap1Items());
        resetMap(map2, 8, 5, createMap2Items());
        resetMap(map3, 12, 8, createMap3Items());
        resetMap4();
    }

    private void resetMap4() {
        if (map4 == null) return;

        map4.getSkeletonArrayList().clear();
        map4.getSkeletonArrayList().addAll(createMap4Enemies());
        map4.getBoomArrayList().clear();
        map4.getItemArrayList().clear();
        map4.getItemArrayList().addAll(createMap4Items());
        map4.moveEnemiesOutOfBlockedAreas();
    }

    private void resetMap(GameMap map, int skeletonCount, int boomCount, ArrayList<Item> items) {
        if (map.getSkeletonArrayList() != null) {
            map.getSkeletonArrayList().clear();
            map.getSkeletonArrayList().addAll(
                    HelpMethods.GetSkeletonsRandomized(skeletonCount, map.getSpriteIds())
            );
        }

        if (map.getBoomArrayList() != null) {
            map.getBoomArrayList().clear();
            map.getBoomArrayList().addAll(
                    HelpMethods.GetBoomsRandomized(boomCount, map.getSpriteIds())
            );
        }

        if (map.getItemArrayList() != null) {
            map.getItemArrayList().clear();
            map.getItemArrayList().addAll(items);
        }

        applyDifficultyToEnemies(map.getSkeletonArrayList(), map.getBoomArrayList());
        map.moveEnemiesOutOfBlockedAreas();
    }

    private void applyDifficultyToEnemies(ArrayList<Skeleton> skeletons, ArrayList<Boom> booms) {
        Game.Difficulty difficulty = playing.getCurrentDifficulty();

        if (skeletons != null) {
            for (Skeleton skeleton : skeletons) {
                skeleton.applyDifficulty(difficulty);
            }
        }

        if (booms != null) {
            for (Boom boom : booms) {
                boom.applyDifficulty(difficulty);
                boom.setPlaying(playing);
            }
        }
    }

    private ArrayList<Item> createMap1Items() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item(Items.FISH, new PointF(560, 560)));
        items.add(new Item(Items.MEDIPACK, new PointF(200, 700)));
        items.add(new Item(Items.EMPTY_POT, new PointF(300, 150)));
        return items;
    }

    private ArrayList<Item> createMap2Items() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item(Items.MEDIPACK, new PointF(400, 400)));
        items.add(new Item(Items.FISH, new PointF(800, 600)));
        items.add(new Item(Items.EMPTY_POT, new PointF(600, 200)));
        items.add(new Item(Items.MEDIPACK, new PointF(1000, 700)));
        return items;
    }

    private ArrayList<Item> createMap3Items() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item(Items.MEDIPACK, new PointF(350, 300)));
        items.add(new Item(Items.FISH, new PointF(600, 500)));
        items.add(new Item(Items.EMPTY_POT, new PointF(900, 200)));
        items.add(new Item(Items.MEDIPACK, new PointF(1100, 800)));
        items.add(new Item(Items.FISH, new PointF(200, 900)));
        return items;
    }

    private ArrayList<Item> createMap4Items() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Item(Items.MEDIPACK, new PointF(500, 730)));
        items.add(new Item(Items.FISH, new PointF(1680, 760)));
        items.add(new Item(Items.MEDIPACK, new PointF(1080, 1220)));
        return items;
    }

    private ArrayList<Skeleton> createMap4Enemies() {
        ArrayList<Skeleton> enemies = new ArrayList<>();
        enemies.add(new ShadowWraith(new PointF(330, 410)));
        enemies.add(new ShadowWraith(new PointF(1780, 430)));
        enemies.add(new ShadowWraith(new PointF(360, 1040)));
        enemies.add(new ShadowWraith(new PointF(1770, 1050)));
        enemies.add(new ShadowWraith(new PointF(700, 520)));
        enemies.add(new ShadowWraith(new PointF(1440, 1060)));
        enemies.add(new SkeletonArcher(new PointF(430, 740)));
        enemies.add(new SkeletonArcher(new PointF(1740, 740)));
        enemies.add(new SkeletonArcher(new PointF(1050, 1240)));
        return enemies;
    }

    public void resetMapToInitialState() {
        // Clear tất cả enemies hiện có trên map
        if (currentMap.getSkeletonArrayList() != null) {
            currentMap.getSkeletonArrayList().clear();
        }

        if (currentMap.getBoomArrayList() != null) {
            currentMap.getBoomArrayList().clear();
        }
        if (currentMap.getItemArrayList() != null) {
            currentMap.getItemArrayList().clear();
        }

        // Khôi phục lại số lượng quái vật BAN ĐẦU như khi vào game lần đầu
        // Tạo lại 5 Skeleton ngẫu nhiên (như trong initTestMap)
        ArrayList<Skeleton> initialSkeletons = HelpMethods.GetSkeletonsRandomized(5, getCurrentMapArray());
        if (currentMap.getSkeletonArrayList() != null) {
            currentMap.getSkeletonArrayList().addAll(initialSkeletons);
        }

        // Tạo lại 3 Boom ngẫu nhiên (như trong initTestMap)
        ArrayList<Boom> initialBooms = HelpMethods.GetBoomsRandomized(3, getCurrentMapArray());
        if (currentMap.getBoomArrayList() != null) {
            currentMap.getBoomArrayList().addAll(initialBooms);
            // Set playing reference cho các Boom mới
            for (Boom boom : initialBooms) {
                boom.setPlaying(playing);
            }
        }

        // Monster ban đầu = 0 (như trong initTestMap), nên không cần tạo

        // Khôi phục lại items ban đầu
        ArrayList<Item> initialItems = new ArrayList<>();
        initialItems.add(new Item(Items.FISH, new PointF(560, 560)));
        initialItems.add(new Item(Items.MEDIPACK, new PointF(200, 700)));
        initialItems.add(new Item(Items.EMPTY_POT, new PointF(300, 150)));

        if (currentMap.getItemArrayList() != null) {
            currentMap.getItemArrayList().addAll(initialItems);
        }

        currentMap.moveEnemiesOutOfBlockedAreas();

        System.out.println("🔄 Map đã được reset về trạng thái ban đầu:");
        System.out.println("👹 5 Skeletons được tạo lại");
        System.out.println("💥 3 Booms được tạo lại");
        System.out.println("🎁 3 Items ban đầu được tạo lại");
        System.out.println("🚫 0 Monsters (như ban đầu)");
    }

    private int[][] getCurrentMapArray() {
        // Trả về mảng map hiện tại để spawn enemies
        // Giả sử đây là outside map array (có thể cần điều chỉnh)
        return new int[][]{
                {188, 189, 279, 275, 187, 189, 279, 275, 279, 276, 275, 279, 275, 275, 279, 275, 278, 276, 275, 278, 275, 279, 275},
                {188, 189, 275, 279, 187, 189, 276, 275, 279, 275, 277, 275, 275, 277, 276, 275, 279, 278, 278, 275, 275, 279, 275},
                {188, 189, 275, 276, 187, 189, 276, 279, 275, 278, 279, 279, 275, 275, 278, 278, 275, 275, 275, 276, 275, 279, 275},
                {254, 189, 275, 279, 187, 214, 166, 166, 166, 166, 166, 166, 166, 167, 275, 276, 275, 276, 279, 277, 275, 279, 275},
                {188, 189, 275, 275, 209, 210, 210, 210, 210, 195, 210, 210, 193, 189, 275, 277, 168, 275, 278, 275, 275, 276, 275},
                {188, 189, 279, 276, 279, 275, 276, 275, 277, 190, 275, 279, 187, 189, 275, 279, 190, 275, 279, 275, 275, 279, 275},
                {188, 189, 275, 275, 275, 279, 278, 275, 275, 190, 276, 277, 187, 258, 232, 232, 239, 232, 232, 232, 232, 233, 275},
                {188, 189, 275, 279, 275, 275, 231, 232, 232, 238, 275, 275, 187, 189, 275, 275, 275, 275, 275, 275, 275, 275, 275},
                {188, 189, 276, 279, 278, 275, 276, 275, 275, 275, 275, 276, 187, 189, 276, 275, 277, 275, 279, 275, 279, 275, 276},
                {188, 189, 275, 275, 279, 275, 279, 275, 276, 275, 275, 277, 187, 189, 279, 275, 275, 275, 275, 275, 275, 275, 275},
                {188, 214, 167, 276, 275, 277, 275, 275, 278, 275, 276, 275, 187, 189, 275, 275, 278, 275, 275, 276, 275, 277, 275},
                {254, 188, 214, 167, 275, 278, 275, 275, 275, 275, 279, 275, 187, 189, 275, 275, 275, 168, 275, 275, 275, 275, 278},
                {188, 188, 188, 214, 167, 279, 275, 277, 275, 277, 276, 275, 187, 258, 232, 232, 232, 238, 275, 279, 275, 275, 279},
                {188, 188, 188, 253, 214, 167, 275, 277, 168, 275, 275, 275, 187, 189, 275, 275, 275, 275, 275, 279, 275, 275, 275},
                {253, 188, 188, 188, 256, 214, 167, 275, 235, 232, 232, 232, 259, 189, 279, 275, 275, 277, 275, 275, 275, 279, 275},
                {188, 188, 188, 254, 188, 256, 214, 167, 275, 275, 277, 275, 187, 189, 275, 278, 275, 275, 279, 275, 279, 278, 275}
        };
    }

    private void initTestMap() {

        int[][] outsideArray = {
                {188, 189, 279, 275, 187, 189, 279, 275, 279, 276, 275, 279, 275, 275, 279, 275, 278, 276, 275, 278, 275, 279, 275},
                {188, 189, 275, 279, 187, 189, 276, 275, 279, 275, 277, 275, 275, 277, 276, 275, 279, 278, 278, 275, 275, 279, 275},
                {188, 189, 275, 276, 187, 189, 276, 279, 275, 278, 279, 279, 275, 275, 278, 278, 275, 275, 275, 276, 275, 279, 275},
                {254, 189, 275, 279, 187, 214, 166, 166, 166, 166, 166, 166, 166, 167, 275, 276, 275, 276, 279, 277, 275, 279, 275},
                {188, 189, 275, 275, 209, 210, 210, 210, 210, 195, 210, 210, 193, 189, 275, 277, 168, 275, 278, 275, 275, 276, 275},
                {188, 189, 279, 276, 279, 275, 276, 275, 277, 190, 275, 279, 187, 189, 275, 279, 190, 275, 279, 275, 275, 279, 275},
                {188, 189, 275, 275, 275, 279, 278, 275, 275, 190, 276, 277, 187, 258, 232, 232, 239, 232, 232, 232, 232, 233, 275},
                {188, 189, 275, 279, 275, 275, 231, 232, 232, 238, 275, 275, 187, 189, 275, 275, 275, 275, 275, 275, 275, 275, 275},
                {188, 189, 276, 279, 278, 275, 276, 275, 275, 275, 275, 276, 187, 189, 276, 275, 277, 275, 279, 275, 279, 275, 276},
                {188, 189, 275, 275, 279, 275, 279, 275, 276, 275, 275, 277, 187, 189, 279, 275, 275, 275, 275, 275, 275, 275, 275},
                {188, 214, 167, 276, 275, 277, 275, 275, 278, 275, 276, 275, 187, 189, 275, 275, 278, 275, 275, 276, 275, 277, 275},
                {254, 188, 214, 167, 275, 278, 275, 275, 275, 275, 279, 275, 187, 189, 275, 275, 275, 168, 275, 275, 275, 275, 278},
                {188, 188, 188, 214, 167, 279, 275, 277, 275, 277, 276, 275, 187, 258, 232, 232, 232, 238, 275, 279, 275, 275, 279},
                {188, 188, 188, 253, 214, 167, 275, 277, 168, 275, 275, 275, 187, 189, 275, 275, 275, 275, 275, 279, 275, 275, 275},
                {253, 188, 188, 188, 256, 214, 167, 275, 235, 232, 232, 232, 259, 189, 279, 275, 275, 277, 275, 275, 275, 279, 275},
                {188, 188, 188, 254, 188, 256, 214, 167, 275, 275, 277, 275, 187, 189, 275, 278, 275, 275, 279, 275, 279, 278, 275}
        };

        int[][] insideArray = {
                {374, 377, 377, 377, 377, 377, 378},
                {396, 0, 1, 1, 1, 2, 400},
                {396, 22, 23, 23, 23, 24, 400},
                {396, 22, 23, 23, 23, 24, 400},
                {396, 22, 23, 23, 23, 24, 400},
                {396, 44, 45, 45, 45, 46, 400},
                {462, 465, 463, 394, 464, 465, 466}
        };

        int[][] insideFlatHouseArray = {
                {389, 392, 392, 392, 392, 392, 393},
                {411, 143, 144, 144, 144, 145, 415},
                {411, 165, 166, 166, 166, 167, 415},
                {411, 165, 166, 166, 166, 167, 415},
                {411, 165, 166, 166, 166, 167, 415},
                {411, 187, 188, 188, 188, 189, 415},
                {477, 480, 478, 394, 479, 480, 481}
        };

        int[][] insideGreenRoofHouseArr = {
                {384, 387, 387, 387, 387, 387, 388},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {472, 475, 473, 394, 474, 475, 476}
        };
        // Khởi tạo buildings
        ArrayList<Building> buildingArrayList = new ArrayList<>();
        buildingArrayList.add(new Building(new PointF(1440, 160), Buildings.HOUSE_ONE));
        buildingArrayList.add(new Building(new PointF(1540, 880), Buildings.HOUSE_TWO));
        buildingArrayList.add(new Building(new PointF(575, 1000), Buildings.HOUSE_SIX));

        // Khởi tạo game objects
        ArrayList<GameObject> gameObjectArrayList = new ArrayList<>();
        gameObjectArrayList.add(new GameObject(new PointF(190, 70), GameObjects.STATUE_ANGRY_YELLOW));
        gameObjectArrayList.add(new GameObject(new PointF(580, 70), GameObjects.STATUE_ANGRY_YELLOW));
        gameObjectArrayList.add(new GameObject(new PointF(1000, 550), GameObjects.BASKET_FULL_RED_FRUIT));
        gameObjectArrayList.add(new GameObject(new PointF(620, 520), GameObjects.OVEN_SNOW_YELLOW));

        // Khởi tạo items ngoài trời
        ArrayList<Item> outsideItemArrayList = new ArrayList<>();
        outsideItemArrayList.add(new Item(Items.FISH, new PointF(560, 560)));
        outsideItemArrayList.add(new Item(Items.MEDIPACK, new PointF(200, 700)));
        outsideItemArrayList.add(new Item(Items.EMPTY_POT, new PointF(300, 150)));

        // Khởi tạo quái Skeleton, Monster và Boom
// spawn skeletons, monsters & booms
        ArrayList<Skeleton> skeletonsOutside = HelpMethods.GetSkeletonsRandomized(5, outsideArray);
        ArrayList<Boom> boomsOutside = HelpMethods.GetBoomsRandomized(3, outsideArray);

// inside maps (skeletons only)
        GameMap insideMap = new GameMap(
                insideArray,
                Tiles.INSIDE,
                null,
                null,
                HelpMethods.GetSkeletonsRandomized(2, insideArray), // skeletons
                null, // booms
                null  // items
        );

        GameMap insideFlatRoofHouseMap = new GameMap(
                insideFlatHouseArray,
                Tiles.INSIDE,
                null,
                null,
                null, // skeletons
                null, // booms
                null  // items
        );

        GameMap insideGreenRoofHouseMap = new GameMap(
                insideGreenRoofHouseArr,
                Tiles.INSIDE,
                null,
                null,
                null, // skeletons
                null, // booms
                null  // items
        );

// outside map: buildings, objects, skeletons, monsters, booms, items
        GameMap outsideMap = new GameMap(
                outsideArray,
                Tiles.OUTSIDE,
                buildingArrayList,
                gameObjectArrayList,
                skeletonsOutside,
                boomsOutside,
                outsideItemArrayList
        );

        // Nối các doorway
        HelpMethods.ConnectTwoDoorways(outsideMap,
                HelpMethods.CreatePointForDoorway(outsideMap, 0),
                insideMap,
                HelpMethods.CreatePointForDoorway(3, 6));

        HelpMethods.ConnectTwoDoorways(outsideMap,
                HelpMethods.CreatePointForDoorway(outsideMap, 1),
                insideFlatRoofHouseMap,
                HelpMethods.CreatePointForDoorway(3, 6));

        HelpMethods.ConnectTwoDoorways(outsideMap,
                HelpMethods.CreatePointForDoorway(outsideMap, 2),
                insideGreenRoofHouseMap,
                HelpMethods.CreatePointForDoorway(3, 6));

        currentMap = outsideMap;
    }

    private void initMaps() {
        shadowFloorPaint.setFilterBitmap(false);

        // Map 1 - Original outdoor map
        int[][] outsideArray = {
                {188, 189, 279, 275, 187, 189, 279, 275, 279, 276, 275, 279, 275, 275, 279, 275, 278, 276, 275, 278, 275, 279, 275},
                {188, 189, 275, 279, 187, 189, 276, 275, 279, 275, 277, 275, 275, 277, 276, 275, 279, 278, 278, 275, 275, 279, 275},
                {188, 189, 275, 276, 187, 189, 276, 279, 275, 278, 279, 279, 275, 275, 278, 278, 275, 275, 275, 276, 275, 279, 275},
                {254, 189, 275, 279, 187, 214, 166, 166, 166, 166, 166, 166, 166, 167, 275, 276, 275, 276, 279, 277, 275, 279, 275},
                {188, 189, 275, 275, 209, 210, 210, 210, 210, 195, 210, 210, 193, 189, 275, 277, 168, 275, 278, 275, 275, 276, 275},
                {188, 189, 279, 276, 279, 275, 276, 275, 277, 190, 275, 279, 187, 189, 275, 279, 190, 275, 279, 275, 275, 279, 275},
                {188, 189, 275, 275, 275, 279, 278, 275, 275, 190, 276, 277, 187, 258, 232, 232, 239, 232, 232, 232, 232, 233, 275},
                {188, 189, 275, 279, 275, 275, 231, 232, 232, 238, 275, 275, 187, 189, 275, 275, 275, 275, 275, 275, 275, 275, 275},
                {188, 189, 276, 279, 278, 275, 276, 275, 275, 275, 275, 276, 187, 189, 276, 275, 277, 275, 279, 275, 279, 275, 276},
                {188, 189, 275, 275, 279, 275, 279, 275, 276, 275, 275, 277, 187, 189, 279, 275, 275, 275, 275, 275, 275, 275, 275},
                {188, 214, 167, 276, 275, 277, 275, 275, 278, 275, 276, 275, 187, 189, 275, 275, 278, 275, 275, 276, 275, 277, 275},
                {254, 188, 214, 167, 275, 278, 275, 275, 275, 275, 279, 275, 187, 189, 275, 275, 275, 168, 275, 275, 275, 275, 278},
                {188, 188, 188, 214, 167, 279, 275, 277, 275, 277, 276, 275, 187, 258, 232, 232, 232, 238, 275, 279, 275, 275, 279},
                {188, 188, 188, 253, 214, 167, 275, 277, 168, 275, 275, 275, 187, 189, 275, 275, 275, 275, 275, 279, 275, 275, 275},
                {253, 188, 188, 188, 256, 214, 167, 275, 235, 232, 232, 232, 259, 189, 279, 275, 275, 277, 275, 275, 275, 279, 275},
                {188, 188, 188, 254, 188, 256, 214, 167, 275, 275, 277, 275, 187, 189, 275, 278, 275, 275, 279, 275, 279, 278, 275}
        };

        // Map 2 - Snow-themed map with VALID tile indices (reusing existing tiles with slight variations)
        int[][] snowArray = {
                {333, 418, 422, 422, 418, 418, 422, 418, 422, 418, 422, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 330, 331},
                {333, 422, 418, 422, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 330, 331},
                {333, 418, 308, 309, 310, 422, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 330, 331},
                {333, 418, 330, 331, 332, 422, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 330, 331},
                {333, 418, 352, 353, 354, 422, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 333, 418, 422, 419, 418, 330, 331},
                {333, 418, 422, 422, 419, 418, 418, 422, 419, 308, 309, 309, 309, 309, 310, 422, 333, 418, 418, 422, 419, 330, 331},
                {333, 418, 418, 418, 422, 419, 418, 418, 422, 330, 462, 463, 463, 464, 332, 418, 378, 375, 375, 375, 376, 330, 331},
                {333, 418, 422, 419, 418, 418, 422, 419, 418, 330, 484, 485, 485, 486, 332, 418, 418, 418, 422, 419, 418, 330, 331},
                {333, 418, 418, 422, 419, 418, 418, 422, 419, 330, 484, 485, 485, 486, 332, 422, 419, 418, 418, 422, 419, 330, 331},
                {333, 418, 418, 418, 422, 419, 418, 418, 422, 330, 506, 507, 507, 508, 332, 418, 422, 419, 418, 418, 422, 330, 331},
                {333, 418, 422, 419, 418, 418, 422, 419, 418, 352, 353, 353, 353, 353, 354, 418, 418, 418, 422, 419, 418, 330, 331},
                {333, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 330, 331},
                {333, 418, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 330, 331},
                {333, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 330, 331},
                {333, 418, 422, 419, 418, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 330, 331},
                {333, 418, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 330, 331},
                {333, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 330, 331},
                {333, 418, 422, 419, 418, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 330, 331},
                {333, 418, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 419, 418, 418, 422, 330, 331}
        };

        // Map 3 - Desert-themed map (sa mạc) with different tile patterns
        int[][] desertArray = {
                {110, 114, 110, 114, 111, 110, 114, 110, 112, 114, 110, 113, 110, 114, 110, 111, 114, 110, 112, 114, 110, 113, 114},
                {114, 110, 112, 110, 114, 111, 110, 114, 110, 113, 114, 110, 112, 110, 114, 110, 111, 114, 110, 112, 114, 110, 113},
                {110, 113, 114, 110, 112, 114, 110, 111, 114, 110, 112, 114, 110, 113, 110, 114, 111, 110, 114, 112, 110, 114, 110},
                {114, 110, 111, 114, 110, 112, 114, 110, 113, 110, 114, 111, 110, 114, 112, 110, 113, 114, 110, 111, 114, 110, 112},
                {110, 112, 0, 1, 2, 110, 114, 111, 110, 114, 112, 110, 113, 114, 110, 111, 114, 110, 112, 114, 110, 113, 110},
                {114, 110, 22, 23, 24, 111, 110, 110, 110, 110, 110, 113, 114, 110, 112, 114, 110, 111, 114, 110, 113, 110, 114},
                {110, 113, 44, 45, 46, 114, 110, 111, 114, 110, 112, 114, 110, 113, 110, 114, 111, 110, 114, 112, 110, 114, 110},
                {114, 110, 111, 114, 110, 112, 114, 110, 113, 110, 114, 111, 110, 114, 112, 110, 113, 114, 110, 111, 114, 110, 112},
                {110, 112, 114, 110, 113, 110, 114, 111, 110, 114, 112, 110, 113, 114, 110, 111, 114, 110, 112, 114, 110, 113, 110},
                {114, 110, 112, 114, 110, 111, 114, 110, 112, 110, 114, 113, 110, 111, 114, 110, 112, 114, 110, 113, 110, 114, 111},
                {110, 113, 114, 110, 112, 114, 110, 111, 114, 110, 112, 114, 110, 113, 110, 114, 111, 110, 114, 112, 110, 114, 110},
                {114, 110, 111, 114, 110, 112, 114, 110, 113, 110, 114, 111, 110, 114, 112, 110, 113, 114, 110, 111, 114, 110, 112},
                {110, 112, 114, 110, 113, 110, 114, 111, 110, 114, 112, 110, 113, 114, 110, 111, 114, 110, 112, 114, 110, 113, 110},
                {114, 110, 112, 114, 110, 111, 114, 110, 112, 110, 114, 113, 110, 111, 114, 110, 112, 114, 110, 113, 110, 114, 111},
                {110, 113, 114, 110, 112, 114, 110, 111, 114, 110, 112, 114, 110, 113, 110, 114, 111, 110, 114, 112, 110, 114, 110},
                {114, 110, 111, 114, 110, 112, 114, 110, 113, 110, 114, 111, 110, 114, 112, 110, 113, 114, 110, 111, 114, 110, 112}
        };

        // Map 4 uses one large seamless Shadow Realm floor texture.
        int[][] shadowArray = new int[16][23];

        // Buildings for Map 1
        ArrayList<Building> buildingArrayList1 = new ArrayList<>();
        buildingArrayList1.add(new Building(new PointF(1440, 160), Buildings.HOUSE_ONE));
        buildingArrayList1.add(new Building(new PointF(1540, 880), Buildings.HOUSE_TWO));
        buildingArrayList1.add(new Building(new PointF(575, 1000), Buildings.HOUSE_SIX));

        // Buildings for Map 2 (Snow map) - different positions
        ArrayList<Building> buildingArrayList2 = new ArrayList<>();
        buildingArrayList2.add(new Building(new PointF(1440, 160), Buildings.HOUSE_ONE));
        buildingArrayList2.add(new Building(new PointF(1540, 880), Buildings.HOUSE_TWO));
        buildingArrayList2.add(new Building(new PointF(575, 1000), Buildings.HOUSE_SIX));

        // Buildings for Map 3 (Desert map) - different positions
        ArrayList<Building> buildingArrayList3 = new ArrayList<>();
        buildingArrayList3.add(new Building(new PointF(1440, 160), Buildings.HOUSE_ONE));
        buildingArrayList3.add(new Building(new PointF(1540, 880), Buildings.HOUSE_TWO));
        buildingArrayList3.add(new Building(new PointF(575, 1000), Buildings.HOUSE_SIX));

        ArrayList<Building> buildingArrayList4 = new ArrayList<>();
        buildingArrayList4.add(new Building(new PointF(919, 70), Buildings.SHADOW_SHRINE));

        // Game objects for Map 1
        ArrayList<GameObject> gameObjectArrayList1 = new ArrayList<>();
        gameObjectArrayList1.add(new GameObject(new PointF(190, 70), GameObjects.STATUE_ANGRY_YELLOW));
        gameObjectArrayList1.add(new GameObject(new PointF(580, 70), GameObjects.STATUE_ANGRY_YELLOW));
        gameObjectArrayList1.add(new GameObject(new PointF(1000, 550), GameObjects.BASKET_FULL_RED_FRUIT));
        gameObjectArrayList1.add(new GameObject(new PointF(620, 520), GameObjects.OVEN_SNOW_YELLOW));

        // Game objects for Map 2 (Snow map) - different positions and more snow-themed
        ArrayList<GameObject> gameObjectArrayList2 = new ArrayList<>();
        gameObjectArrayList2.add(new GameObject(new PointF(190, 70), GameObjects.OVEN_SNOW_YELLOW));
        gameObjectArrayList2.add(new GameObject(new PointF(580, 70), GameObjects.OVEN_SNOW_YELLOW));
        gameObjectArrayList2.add(new GameObject(new PointF(1000, 350), GameObjects.BASKET_FULL_RED_FRUIT));
        gameObjectArrayList2.add(new GameObject(new PointF(620, 520), GameObjects.STATUE_ANGRY_YELLOW));

        // Game objects for Map 3 (Desert map) - desert-themed objects
        ArrayList<GameObject> gameObjectArrayList3 = new ArrayList<>();
        gameObjectArrayList3.add(new GameObject(new PointF(190, 70), GameObjects.OVEN_SNOW_YELLOW));
        gameObjectArrayList3.add(new GameObject(new PointF(580, 70), GameObjects.OVEN_SNOW_YELLOW));
        gameObjectArrayList3.add(new GameObject(new PointF(1000, 350), GameObjects.BASKET_FULL_RED_FRUIT));
        gameObjectArrayList3.add(new GameObject(new PointF(620, 520), GameObjects.STATUE_ANGRY_YELLOW));

        ArrayList<GameObject> gameObjectArrayList4 = new ArrayList<>();
        gameObjectArrayList4.add(new GameObject(new PointF(120, 170), GameObjects.BROKEN_SHADOW_PILLAR));
        gameObjectArrayList4.add(new GameObject(new PointF(1950, 180), GameObjects.BROKEN_SHADOW_PILLAR));
        gameObjectArrayList4.add(new GameObject(new PointF(140, 1160), GameObjects.BROKEN_SHADOW_PILLAR));
        gameObjectArrayList4.add(new GameObject(new PointF(1940, 1140), GameObjects.BROKEN_SHADOW_PILLAR));
        gameObjectArrayList4.add(new GameObject(new PointF(500, 250), GameObjects.SHADOW_ROCK));
        gameObjectArrayList4.add(new GameObject(new PointF(1570, 270), GameObjects.SHADOW_ROCK));
        gameObjectArrayList4.add(new GameObject(new PointF(520, 1180), GameObjects.SHADOW_ROCK));
        gameObjectArrayList4.add(new GameObject(new PointF(1590, 1170), GameObjects.SHADOW_ROCK));

        // Items for Map 1
        ArrayList<Item> outsideItemArrayList1 = createMap1Items();

        // Items for Map 2 (Snow map) - different positions
        ArrayList<Item> outsideItemArrayList2 = createMap2Items();

        // Items for Map 3 (Desert map) - more items for hardest map
        ArrayList<Item> outsideItemArrayList3 = createMap3Items();
        ArrayList<Item> outsideItemArrayList4 = createMap4Items();

        // Enemies for Map 1 (easier)
        ArrayList<Skeleton> skeletonsOutside1 = HelpMethods.GetSkeletonsRandomized(5, outsideArray);
        ArrayList<Boom> boomsOutside1 = HelpMethods.GetBoomsRandomized(3, outsideArray);

        // Enemies for Map 2 (harder - more enemies)
        ArrayList<Skeleton> skeletonsOutside2 = HelpMethods.GetSkeletonsRandomized(8, snowArray); // More skeletons
        ArrayList<Boom> boomsOutside2 = HelpMethods.GetBoomsRandomized(5, snowArray); // More booms

        // Enemies for Map 3 (hardest - most enemies)
        ArrayList<Skeleton> skeletonsOutside3 = HelpMethods.GetSkeletonsRandomized(12, desertArray); // Most skeletons
        ArrayList<Boom> boomsOutside3 = HelpMethods.GetBoomsRandomized(8, desertArray); // Most booms
        ArrayList<Skeleton> shadowEnemies = createMap4Enemies();
        ArrayList<Boom> shadowBooms = new ArrayList<>();

        applyDifficultyToEnemies(skeletonsOutside1, boomsOutside1);
        applyDifficultyToEnemies(skeletonsOutside2, boomsOutside2);
        applyDifficultyToEnemies(skeletonsOutside3, boomsOutside3);

        // Create Map 1 (Original outdoor map)
        map1 = new GameMap(
                outsideArray,
                Tiles.OUTSIDE,
                buildingArrayList1,
                gameObjectArrayList1,
                skeletonsOutside1,
                boomsOutside1,
                outsideItemArrayList1
        );

        // Create Map 2 (Snow map)
        map2 = new GameMap(
                snowArray,
                Tiles.SNOW,
                buildingArrayList2,
                gameObjectArrayList2,
                skeletonsOutside2,
                boomsOutside2,
                outsideItemArrayList2
        );

        // Create Map 3 (Desert map)
        map3 = new GameMap(
                desertArray,
                Tiles.OUTSIDE, // Reuse OUTSIDE tiles for desert theme
                buildingArrayList3,
                gameObjectArrayList3,
                skeletonsOutside3,
                boomsOutside3,
                outsideItemArrayList3
        );

        map4 = new GameMap(
                shadowArray,
                Tiles.SHADOW,
                buildingArrayList4,
                gameObjectArrayList4,
                shadowEnemies,
                shadowBooms,
                outsideItemArrayList4
        );

        // Start with Map 1
        currentMap = map1;

        // Create inside maps for map1 (keeping existing functionality)
        int[][] insideArray = {
                {374, 377, 377, 377, 377, 377, 378},
                {396, 0, 1, 1, 1, 2, 400},
                {396, 22, 23, 23, 23, 24, 400},
                {396, 22, 23, 23, 23, 24, 400},
                {396, 22, 23, 23, 23, 24, 400},
                {396, 44, 45, 45, 45, 46, 400},
                {462, 465, 463, 394, 464, 465, 466}
        };

        int[][] insideFlatHouseArray = {
                {389, 392, 392, 392, 392, 392, 393},
                {411, 143, 144, 144, 144, 145, 415},
                {411, 165, 166, 166, 166, 167, 415},
                {411, 165, 166, 166, 166, 167, 415},
                {411, 165, 166, 166, 166, 167, 415},
                {411, 187, 188, 188, 188, 189, 415},
                {477, 480, 478, 394, 479, 480, 481}
        };

        int[][] insideGreenRoofHouseArr = {
                {384, 387, 387, 387, 387, 387, 388},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {406, 298, 298, 298, 298, 298, 410},
                {472, 475, 473, 394, 474, 475, 476}
        };

        // Inside maps (only for map1)
        GameMap insideMap = new GameMap(
                insideArray,
                Tiles.INSIDE,
                null,
                null,
                HelpMethods.GetSkeletonsRandomized(2, insideArray),
                null,
                null
        );

        GameMap insideFlatRoofHouseMap = new GameMap(
                insideFlatHouseArray,
                Tiles.INSIDE,
                null,
                null,
                null,
                null,
                null
        );

        GameMap insideGreenRoofHouseMap = new GameMap(
                insideGreenRoofHouseArr,
                Tiles.INSIDE,
                null,
                null,
                null,
                null,
                null
        );

        // Connect doorways for map1
        HelpMethods.ConnectTwoDoorways(map1,
                HelpMethods.CreatePointForDoorway(map1, 0),
                insideMap,
                HelpMethods.CreatePointForDoorway(3, 6));

        HelpMethods.ConnectTwoDoorways(map1,
                HelpMethods.CreatePointForDoorway(map1, 1),
                insideFlatRoofHouseMap,
                HelpMethods.CreatePointForDoorway(3, 6));

        HelpMethods.ConnectTwoDoorways(map1,
                HelpMethods.CreatePointForDoorway(map1, 2),
                insideGreenRoofHouseMap,
                HelpMethods.CreatePointForDoorway(3, 6));
    }

    private synchronized void ensureShadowFloorLoaded() {
        if (shadowFloorTexture != null && !shadowFloorTexture.isRecycled()) return;
        shadowFloorTexture = BitmapCache.get(
                MainActivity.getGameContext(),
                R.drawable.shadow_floor,
                Bitmap.Config.RGB_565
        );
    }
}
