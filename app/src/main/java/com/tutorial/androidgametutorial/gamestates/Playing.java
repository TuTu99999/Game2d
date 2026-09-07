package com.tutorial.androidgametutorial.gamestates;

import com.tutorial.androidgametutorial.R;
import static com.tutorial.androidgametutorial.helpers.GameConstants.Sprite.X_DRAW_OFFSET;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Process;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.entities.Building;
import com.tutorial.androidgametutorial.entities.Character;
import com.tutorial.androidgametutorial.entities.Entity;
import com.tutorial.androidgametutorial.entities.GameCharacters;
import com.tutorial.androidgametutorial.entities.GameObject;
import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.entities.Projectile;
import com.tutorial.androidgametutorial.entities.Weapons;
import com.tutorial.androidgametutorial.entities.EffectExplosion;
import com.tutorial.androidgametutorial.entities.SparkSkill;
import com.tutorial.androidgametutorial.entities.enemies.Boom;
import com.tutorial.androidgametutorial.entities.enemies.Boss;
import com.tutorial.androidgametutorial.entities.enemies.BossAnimation;
import com.tutorial.androidgametutorial.entities.enemies.BossState;
import com.tutorial.androidgametutorial.entities.enemies.EnemyArrow;
import com.tutorial.androidgametutorial.entities.enemies.FinalBoss;
import com.tutorial.androidgametutorial.entities.enemies.FinalBossAnimation;
import com.tutorial.androidgametutorial.entities.enemies.ShadowWraith;
import com.tutorial.androidgametutorial.entities.enemies.SkeletonArcher;
import com.tutorial.androidgametutorial.entities.enemies.Skeleton;
import com.tutorial.androidgametutorial.entities.items.Item;
import com.tutorial.androidgametutorial.environments.Doorway;
import com.tutorial.androidgametutorial.environments.AnimatedMapBackground;
import com.tutorial.androidgametutorial.environments.GameMap;
import com.tutorial.androidgametutorial.environments.MapManager;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.HelpMethods;
import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.main.LoadoutManager;
import com.tutorial.androidgametutorial.ui.PlayingUI;
import com.tutorial.androidgametutorial.ui.LoadoutIcons;
import com.tutorial.androidgametutorial.effects.ExplosionEffect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Playing extends BaseState implements GameStateInterface {
    private static final float ENEMY_DRAW_SCALE = 1.3f;
    private float cameraX, cameraY;
    private boolean movePlayer;
    private PointF lastTouchDiff;
    private MapManager mapManager;
    private final AnimatedMapBackground animatedBackground;
    private Player player;
    private Boss boss;
    private FinalBoss finalBoss;
    private PlayingUI playingUI;
    private final Paint redPaint, healthBarRed, healthBarBlack;
    private final Paint tutorialPanelPaint, tutorialTitlePaint, tutorialTextPaint;
    private final boolean tutorialMode;
    private int tutorialStep = 0;

    // Difficulty system
    private Game.Difficulty currentDifficulty = Game.Difficulty.EASY;

    private boolean doorwayJustPassed;
    private final ArrayList<Entity> listOfDrawables = new ArrayList<>();
    private boolean listOfEntitiesMade;

    // thêm
    private SoundPool soundPool;
    private int swordHitSoundId;
    private int playerHitWallSoundId;
    private int boomExplosionSoundId;
    private int skillWhooshSoundId;
    private int sparkSkillSoundId;
    private int explosionSkillSoundId;
    private long lastWallSoundTime = 0;
    private boolean isSwordSoundEnabled = true; // Add this line


    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private final ArrayList<EnemyArrow> enemyArrows = new ArrayList<>();
    private final Paint projectilePaint = new Paint();
    // thời điểm gây sát thương (ms)
    private ArrayList<ExplosionEffect> explosionEffects = new ArrayList<>();
    private ArrayList<EffectExplosion> effectExplosions = new ArrayList<>();
    private ArrayList<SparkSkill> sparkSkills = new ArrayList<>();
    private long slowEnemiesUntil;
    private long skillPulseStarted;
    private float skillPulseX;
    private float skillPulseY;
    private float skillPulseRadius;
    private int skillPulseColor = Color.CYAN;
    private int skillPulseIconRes;
    private final Paint skillPulsePaint = new Paint();
    private final Paint skillPulseFillPaint = new Paint();
    private final Paint skillParticlePaint = new Paint();
    private final Paint skillIconPaint = new Paint();
    private final Paint playerSpritePaint = new Paint();
    private final Paint playerWeaponPaint = new Paint();

    // Spawn enemies
    private long lastSpawnTime = 0;

    // Tracking game stats for victory screen
    private long gameStartTime = 0;
    private int killCount = 0;
    private static final long VICTORY_TIME = 20000; // 20 seconds in milliseconds
    private static final long MAP_PRELOAD_DELAY = 3000L;
    private volatile boolean pauseMenuOpen;
    private long pauseStartedAt;
    private final Object preloadLock = new Object();
    private final boolean[] preloadScheduled = new boolean[5];
    private ExecutorService mapPreloadExecutor;
    private volatile boolean disposed;

    public Playing(Game game) {
        this(game, false);
    }

    public Playing(Game game, boolean tutorialMode) {
        super(game);
        this.tutorialMode = tutorialMode;

        mapManager = new MapManager(this, tutorialMode);
        animatedBackground = new AnimatedMapBackground(game.getContext());
        calcStartCameraValues();

        player = new Player();
        player.applyLoadout(game.getLoadoutManager());

        // THAY ĐỔI: Khởi tạo boss là null. Boss sẽ được tạo sau khi vào map 3.
        boss = null;
        finalBoss = null;

        playingUI = new PlayingUI(this);

        // Initialize game start time
        gameStartTime = System.currentTimeMillis();
        killCount = 0;

        redPaint = new Paint();
        redPaint.setStrokeWidth(1);
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setColor(Color.RED);

        healthBarRed = new Paint();
        healthBarBlack = new Paint();

        float uiScale = Math.min(GAME_WIDTH / 1920f, GAME_HEIGHT / 1080f);
        tutorialPanelPaint = new Paint();
        tutorialPanelPaint.setColor(Color.argb(215, 10, 16, 35));
        tutorialPanelPaint.setAntiAlias(true);

        tutorialTitlePaint = new Paint();
        tutorialTitlePaint.setColor(Color.rgb(255, 205, 75));
        tutorialTitlePaint.setTextSize(30 * uiScale);
        tutorialTitlePaint.setFakeBoldText(true);
        tutorialTitlePaint.setTextAlign(Paint.Align.CENTER);
        tutorialTitlePaint.setAntiAlias(true);

        tutorialTextPaint = new Paint();
        tutorialTextPaint.setColor(Color.WHITE);
        tutorialTextPaint.setTextSize(23 * uiScale);
        tutorialTextPaint.setTextAlign(Paint.Align.CENTER);
        tutorialTextPaint.setAntiAlias(true);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();
        projectilePaint.setColor(Color.CYAN);
        projectilePaint.setStyle(Paint.Style.FILL);

        swordHitSoundId = soundPool.load(game.getContext(), R.raw.sword_slice, 1);
        playerHitWallSoundId = soundPool.load(game.getContext(), R.raw.wall_hit, 1);
        boomExplosionSoundId = soundPool.load(game.getContext(), R.raw.explosion_boom, 1);
        skillWhooshSoundId = soundPool.load(game.getContext(), R.raw.fast_whoosh, 1);
        sparkSkillSoundId = soundPool.load(game.getContext(), R.raw.spark_voice, 1);
        explosionSkillSoundId = soundPool.load(game.getContext(), R.raw.explosion, 1);

        skillPulsePaint.setStyle(Paint.Style.STROKE);
        skillPulsePaint.setStrokeWidth(12f);
        skillPulsePaint.setAntiAlias(true);
        skillPulseFillPaint.setStyle(Paint.Style.FILL);
        skillPulseFillPaint.setAntiAlias(true);
        skillParticlePaint.setStyle(Paint.Style.FILL);
        skillParticlePaint.setAntiAlias(true);
        skillIconPaint.setFilterBitmap(false);
        playerSpritePaint.setFilterBitmap(false);
        playerWeaponPaint.setFilterBitmap(false);
        updatePlayerAppearance();

        initHealthBars();
    }

    // BỔ SUNG: Hàm để tạo Boss. Sẽ được gọi bởi MapManager
    public void spawnBoss() {
        if (mapManager.getCurrentMapLevel() != 3 || boss != null) {
            return;
        }

        // Tạo boss ở giữa map hiện tại (map 3)
        float bossX = mapManager.getMaxWidthCurrentMap() / 2f + 240f
                - GameConstants.Sprite.SIZE / 2f;
        float bossY = mapManager.getMaxHeightCurrentMap() / 2f
                - GameConstants.Sprite.SIZE / 2f;
        boss = new Boss(new PointF(bossX, bossY));
        boss.applyDifficulty(currentDifficulty == Game.Difficulty.HARD);
        System.out.println("🔥 BOSS ĐÃ XUẤT HIỆN TẠI MAP 3! 🔥");
    }


    public void spawnFinalBoss() {
        if (mapManager.getCurrentMapLevel() != 4 || finalBoss != null) {
            return;
        }

        boss = null;
        float bossX = mapManager.getMaxWidthCurrentMap() / 2f + 300f;
        float bossY = mapManager.getMaxHeightCurrentMap() / 2f - 41f;
        finalBoss = new FinalBoss(new PointF(bossX, bossY));
    }

    // Helper to get direction to target
    private int getDirectionToTarget(float dx, float dy) {
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT;
        } else {
            return dy > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP;
        }
    }

    private void initHealthBars() {
        healthBarRed.setStrokeWidth(10);
        healthBarRed.setStyle(Paint.Style.STROKE);
        healthBarRed.setColor(Color.RED);
        healthBarBlack.setStrokeWidth(14);
        healthBarBlack.setStyle(Paint.Style.STROKE);
        healthBarBlack.setColor(Color.BLACK);

    }

    private void calcStartCameraValues() {
        cameraX = GAME_WIDTH / 2f - mapManager.getMaxWidthCurrentMap() / 2f;
        cameraY = GAME_HEIGHT / 2f - mapManager.getMaxHeightCurrentMap() / 2f;
    }


    @Override
    public void update(double delta) {
        if (pauseMenuOpen) return;

        animatedBackground.update(delta, mapManager.getCurrentMapLevel());
        maybeScheduleNextMapPreload();

        // Phòng trường hợp luồng chuyển map bị bỏ lỡ lệnh spawn.
        if (!tutorialMode && mapManager.getCurrentMapLevel() == 3 && boss == null) {
            spawnBoss();
        }
        if (!tutorialMode && mapManager.getCurrentMapLevel() == 4 && finalBoss == null) {
            spawnFinalBoss();
        }

        if (!tutorialMode) {
            checkVictoryCondition();
        }

        removeDefeatedEntities();
        buildEntityList();
        updatePlayerMove(delta);
        player.update(delta, movePlayer);
        mapManager.setCameraValues(cameraX, cameraY);
        checkForDoorway();

        if (player.isAttacking()) {
            if (!player.isAttackChecked()
                    && player.getAttackAnimationProgress() >= 0.35f) {
                checkPlayerAttack();
            }
        }

        boolean pauseForFrost = System.currentTimeMillis() < slowEnemiesUntil
                && (System.currentTimeMillis() / 120L) % 2L == 0L;

        if (mapManager.getCurrentMap().getSkeletonArrayList() != null)
            for (Skeleton skeleton : mapManager.getCurrentMap().getSkeletonArrayList())
                if (skeleton.isActive()) {
                    if (pauseForFrost) continue;
                    skeleton.update(delta, mapManager.getCurrentMap(), player, cameraX, cameraY, this);
                    if (skeleton.isAttacking()) {
                        if (!skeleton.isAttackChecked()) {
                            checkEnemyAttack(skeleton);
                        }
                    } else if (!skeleton.isPreparingAttack()) {
                        if (HelpMethods.IsPlayerCloseForAttack(skeleton, player, cameraY, cameraX)) {
                            skeleton.prepareAttack(player, cameraX, cameraY);
                        }
                    }
                }

        if (mapManager.getCurrentMap().getBoomArrayList() != null)
            for (Boom boom : mapManager.getCurrentMap().getBoomArrayList())
                if (boom.isActive()) {
                    if (pauseForFrost) continue;
                    boom.update(delta, mapManager.getCurrentMap(), player, cameraX, cameraY, this);
                    if (boom.isExploding()) {
                        // Boom tự động gây sát thương khi exploding, không cần check attack
                    } else if (!boom.isPreparingAttack()) {
                        if (HelpMethods.IsPlayerCloseForAttack(boom, player, cameraY, cameraX)) {
                            boom.prepareAttack(player, cameraX, cameraY);
                        }
                    }
                }

        // Cập nhật trạng thái và logic của Boss (chỉ khi boss đã tồn tại)
        if (boss != null && mapManager.getCurrentMapLevel() == 3) {
            if (boss.isActive()) {
                float playerScreenX = player.getHitbox().centerX();
                float playerScreenY = player.getHitbox().centerY();

                float bossScreenX = boss.getHitbox().centerX() + cameraX;
                float bossScreenY = boss.getHitbox().centerY() + cameraY;

                float dx = playerScreenX - bossScreenX;
                float dy = playerScreenY - bossScreenY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                boolean bossCanMove = boss.getState() == BossState.IDLE
                        || boss.getState() == BossState.WALK;

                if (bossCanMove && distance <= Boss.ATTACK_START_RANGE) {
                    boss.startAttackToward(
                            playerScreenX - cameraX,
                            playerScreenY - cameraY
                    );
                } else if (bossCanMove) {
                    boss.moveToward(
                            playerScreenX - cameraX,
                            playerScreenY - cameraY,
                            mapManager.getCurrentMap()
                    );
                }
            }

            boss.update(System.currentTimeMillis(), player, cameraX, cameraY);
            if (boss.isActive()) {
                checkPlayerDead();
            }
        }

        if (finalBoss != null && mapManager.getCurrentMapLevel() == 4) {
            finalBoss.update(
                    delta,
                    System.currentTimeMillis(),
                    player,
                    cameraX,
                    cameraY,
                    mapManager.getCurrentMap()
            );
            checkPlayerDead();
        }

        updateEnemyArrows(delta);
        sortArray();
        updateProjectiles(delta);
        updateEffectExplosions(delta);
        updateSparkSkills(delta);
        updateItems(delta);
        player.updateEffects();
        if (tutorialMode) {
            updateTutorialProgress();
        } else {
            spawnEnemies();
        }
    }

    private void updateTutorialProgress() {
        if (tutorialStep == 0 && movePlayer) {
            tutorialStep = 1;
        }

        if (tutorialStep <= 1 && player.isAttacking()) {
            tutorialStep = 2;
        }

        boolean enemyAlive = false;
        if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
            for (Skeleton skeleton : mapManager.getCurrentMap().getSkeletonArrayList()) {
                if (skeleton.isActive()) {
                    enemyAlive = true;
                    break;
                }
            }
        }

        if (!enemyAlive) {
            tutorialStep = 4;
        }
    }

    private void buildEntityList() {
        listOfDrawables.clear();

        if (mapManager.getCurrentMap().getBuildingArrayList() != null)
            listOfDrawables.addAll(mapManager.getCurrentMap().getBuildingArrayList());
        if (mapManager.getCurrentMap().getSkeletonArrayList() != null)
            listOfDrawables.addAll(mapManager.getCurrentMap().getSkeletonArrayList());
        if (mapManager.getCurrentMap().getGameObjectArrayList() != null)
            listOfDrawables.addAll(mapManager.getCurrentMap().getGameObjectArrayList());
        if (mapManager.getCurrentMap().getItemArrayList() != null)
            listOfDrawables.addAll(mapManager.getCurrentMap().getItemArrayList());
        if (mapManager.getCurrentMap().getBoomArrayList() != null)
            listOfDrawables.addAll(mapManager.getCurrentMap().getBoomArrayList());

        listOfDrawables.add(player);
        listOfEntitiesMade = true;
    }

    private void removeDefeatedEntities() {
        GameMap currentMap = mapManager.getCurrentMap();

        if (currentMap.getSkeletonArrayList() != null) {
            currentMap.getSkeletonArrayList().removeIf(enemy -> {
                if (enemy.isActive()) return false;
                // Archer owns a short death animation; retain it only until
                // the last death frame has had time to render.
                return !(enemy instanceof SkeletonArcher archer)
                        || !archer.isVisible();
            });
        }

        if (currentMap.getBoomArrayList() != null) {
            // Boom remains active for its complete explosion animation, so it
            // is safe to remove immediately after it becomes inactive.
            currentMap.getBoomArrayList().removeIf(boom -> !boom.isActive());
        }
    }

    private void sortArray() {
        player.setLastCameraYValue(cameraY);
        listOfDrawables.sort(null);
    }

    public void setCameraValues(PointF cameraPos) {
        this.cameraX = cameraPos.x;
        this.cameraY = cameraPos.y;
    }

    private void checkForDoorway() {
        Doorway doorwayPlayerIsOn = mapManager.isPlayerOnDoorway(player.getHitbox());

        if (doorwayPlayerIsOn != null) {
            if (!doorwayJustPassed) mapManager.changeMap(doorwayPlayerIsOn.getDoorwayConnectedTo());
        } else doorwayJustPassed = false;

    }

    public void setDoorwayJustPassed(boolean doorwayJustPassed) {
        this.doorwayJustPassed = doorwayJustPassed;
    }


    private void checkEnemyAttack(Character character) {
        character.updateWepHitbox();
        RectF playerHitbox = new RectF(player.getHitbox());
        playerHitbox.left -= cameraX;
        playerHitbox.top -= cameraY;
        playerHitbox.right -= cameraX;
        playerHitbox.bottom -= cameraY;
        if (RectF.intersects(character.getAttackBox(), playerHitbox)) {
            player.damageCharacter(character.getDamage());
            checkPlayerDead();
        }
        character.setAttackChecked(true);
    }

    private void checkPlayerDead() {
        if (player.getCurrentHealth() > 0)
            return;

        if (tutorialMode) {
            player.resetCharacterHealth();
            return;
        }

        game.setCurrentGameState(Game.GameState.DEATH_SCREEN);
        player.resetCharacterHealth();

    }

    private void checkPlayerAttack() {
        RectF attackBoxWithoutCamera = new RectF(player.getAttackBox());
        attackBoxWithoutCamera.left -= cameraX;
        attackBoxWithoutCamera.top -= cameraY;
        attackBoxWithoutCamera.right -= cameraX;
        attackBoxWithoutCamera.bottom -= cameraY;

        // Check Skeleton
        if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
            for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList()) {
                if (!s.isActive()) continue;
                if (attackBoxWithoutCamera.intersects(
                        s.getHitbox().left,
                        s.getHitbox().top,
                        s.getHitbox().right,
                        s.getHitbox().bottom)) {

                    s.damageCharacter(player.getDamage());
                    playSwordHit();

                    if (s.getCurrentHealth() <= 0) {
                        s.setSkeletonInactive();
                        enemyKilled();
                        if (!s.hasDroppedItem()) {
                            s.setHasDroppedItem(true);
                            Item droppedItem = HelpMethods.tryDropItem(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY()));
                            if (droppedItem != null) {
                                mapManager.getCurrentMap().getItemArrayList().add(droppedItem);
                            }
                        }
                    }
                }
            }
        }

        // Check Boom
        if (mapManager.getCurrentMap().getBoomArrayList() != null) {
            for (Boom boom : mapManager.getCurrentMap().getBoomArrayList()) {
                if (!boom.isActive()) continue;
                if (attackBoxWithoutCamera.intersects(
                        boom.getHitbox().left,
                        boom.getHitbox().top,
                        boom.getHitbox().right,
                        boom.getHitbox().bottom)) {
                    boom.damageCharacter(player.getDamage());
                    playSwordHit();
                    if (boom.getCurrentHealth() <= 0) {
                        boom.setBoomInactive();
                        enemyKilled();
                    }
                }
            }
        }

        if (boss != null && boss.isActive()) {
            RectF bossMeleeArea = new RectF(boss.getHitbox());
            bossMeleeArea.inset(-25f, -25f);

            RectF playerWorldHitbox = new RectF(player.getHitbox());
            playerWorldHitbox.offset(-cameraX, -cameraY);
            playerWorldHitbox.inset(-35f, -35f);

            boolean swordTouchesBoss = RectF.intersects(
                    attackBoxWithoutCamera,
                    bossMeleeArea
            );
            boolean playerIsVeryClose = RectF.intersects(
                    playerWorldHitbox,
                    boss.getHitbox()
            );

            float playerWorldX = player.getHitbox().centerX() - cameraX;
            float playerWorldY = player.getHitbox().centerY() - cameraY;
            float bossDx = boss.getHitbox().centerX() - playerWorldX;
            float bossDy = boss.getHitbox().centerY() - playerWorldY;
            boolean bossIsInMeleeRange = bossDx * bossDx + bossDy * bossDy
                    <= 175f * 175f;

            if (swordTouchesBoss || playerIsVeryClose || bossIsInMeleeRange) {
                boss.damage(player.getDamage());
                playSwordHit();
                if (!boss.isActive()) {
                    enemyKilled();
                }
            }
        }

        if (finalBoss != null && finalBoss.isActive()) {
            RectF finalBossMeleeArea = new RectF(finalBoss.getHitbox());
            finalBossMeleeArea.inset(-35f, -35f);

            float playerWorldX = player.getHitbox().centerX() - cameraX;
            float playerWorldY = player.getHitbox().centerY() - cameraY;
            float dx = finalBoss.getHitbox().centerX() - playerWorldX;
            float dy = finalBoss.getHitbox().centerY() - playerWorldY;
            boolean closeEnough = dx * dx + dy * dy <= 205f * 205f;

            if (RectF.intersects(attackBoxWithoutCamera, finalBossMeleeArea)
                    || closeEnough) {
                finalBoss.damage(player.getDamage());
                playSwordHit();
                if (!finalBoss.isActive()) enemyKilled();
            }
        }

        player.setAttackChecked(true);
    }

    private void updateItems(double delta) {
        if (mapManager.getCurrentMap().getItemArrayList() != null) {
            java.util.Iterator<Item> itemIterator = mapManager.getCurrentMap().getItemArrayList().iterator();
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                if (!item.isActive()) {
                    itemIterator.remove();
                    continue;
                }
                item.update(delta);
                RectF itemScreenHitbox = new RectF(
                        item.getHitbox().left + cameraX,
                        item.getHitbox().top + cameraY,
                        item.getHitbox().right + cameraX,
                        item.getHitbox().bottom + cameraY
                );
                boolean collision = (itemScreenHitbox.left < player.getHitbox().right &&
                        itemScreenHitbox.right > player.getHitbox().left &&
                        itemScreenHitbox.top < player.getHitbox().bottom &&
                        itemScreenHitbox.bottom > player.getHitbox().top);

                if (collision) {
                    if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.MEDIPACK) {
                        player.useMedipack();
                    } else if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.FISH) {
                        player.useFish();
                    } else if (item.getItemType() == com.tutorial.androidgametutorial.entities.items.Items.EMPTY_POT) {
                        player.useEmptyPot();
                    }
                    item.deactivate();
                    itemIterator.remove();
                }
            }
        }
    }

    private void spawnEnemies() {
        if (!isOutdoorMap()) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpawnTime >= 3000) {
            lastSpawnTime = currentTime;
            if (mapManager.getCurrentMap().getSkeletonArrayList() == null ||
                    mapManager.getCurrentMap().getBoomArrayList() == null) {
                return;
            }
            int spawnCount = 1;
            for (int i = 0; i < spawnCount; i++) {
                if (mapManager.getCurrentMapLevel() == 4) {
                    int activeWraiths = 0;
                    int activeArchers = 0;
                    for (Skeleton enemy : mapManager.getCurrentMap().getSkeletonArrayList()) {
                        if (enemy instanceof ShadowWraith && enemy.isActive()) activeWraiths++;
                        if (enemy instanceof SkeletonArcher && enemy.isActive()) activeArchers++;
                    }

                    if (activeArchers < 4 && Math.random() < 0.35) {
                        SkeletonArcher archer = new SkeletonArcher(new PointF(0, 0));
                        if (placeEnemyNearPlayer(archer)) {
                            mapManager.getCurrentMap().getSkeletonArrayList().add(archer);
                        }
                    } else if (activeWraiths < 8) {
                        ShadowWraith wraith = new ShadowWraith(new PointF(0, 0));
                        if (placeEnemyNearPlayer(wraith)) {
                            mapManager.getCurrentMap().getSkeletonArrayList().add(wraith);
                        }
                    }
                    continue;
                }

                double random = Math.random();
                if (random < 0.4) {
                    Skeleton skeleton = new Skeleton(new PointF(0, 0), GameCharacters.SKELETON);
                    skeleton.applyDifficulty(currentDifficulty);
                    if (placeEnemyNearPlayer(skeleton)) {
                        mapManager.getCurrentMap().getSkeletonArrayList().add(skeleton);
                    }
                } else {
                    Boom boom = new Boom(new PointF(0, 0));
                    boom.applyDifficulty(currentDifficulty);
                    boom.setPlaying(this);
                    if (placeEnemyNearPlayer(boom)) {
                        mapManager.getCurrentMap().getBoomArrayList().add(boom);
                    }
                }
            }
        }
    }

    private void updateEnemyArrows(double delta) {
        Iterator<EnemyArrow> iterator = enemyArrows.iterator();
        while (iterator.hasNext()) {
            EnemyArrow arrow = iterator.next();
            boolean hitPlayer = arrow.update(
                    delta,
                    player,
                    cameraX,
                    cameraY,
                    mapManager.getCurrentMap()
            );
            if (hitPlayer) checkPlayerDead();
            if (!arrow.isActive()) iterator.remove();
        }
    }

    private boolean isOutdoorMap() {
        return !tutorialMode &&
                (mapManager.getCurrentMap().getFloorType()
                        == com.tutorial.androidgametutorial.environments.Tiles.OUTSIDE ||
                mapManager.getCurrentMap().getFloorType()
                        == com.tutorial.androidgametutorial.environments.Tiles.SNOW ||
                mapManager.getCurrentMap().getFloorType()
                        == com.tutorial.androidgametutorial.environments.Tiles.SHADOW);
    }

    private boolean placeEnemyNearPlayer(Character enemy) {
        GameMap currentMap = mapManager.getCurrentMap();
        float playerWorldX = player.getHitbox().centerX() - cameraX;
        float playerWorldY = player.getHitbox().centerY() - cameraY;

        for (int attempt = 0; attempt < 30; attempt++) {
            double angle = Math.random() * Math.PI * 2;
            float distance = 400 + (float) Math.random() * 400;
            float spawnX = playerWorldX + (float) Math.cos(angle) * distance;
            float spawnY = playerWorldY + (float) Math.sin(angle) * distance;

            enemy.getHitbox().offsetTo(
                    spawnX - enemy.getHitbox().width() / 2f,
                    spawnY - enemy.getHitbox().height() / 2f
            );

            if (HelpMethods.CanWalkHere(enemy.getHitbox(), 0, 0, currentMap)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void render(Canvas c) {
        animatedBackground.drawBackground(
                c,
                cameraX,
                cameraY,
                mapManager.getCurrentMapLevel()
        );
        mapManager.drawTiles(c);
        animatedBackground.drawMapBorder(
                c,
                cameraX,
                cameraY,
                mapManager.getMaxWidthCurrentMap(),
                mapManager.getMaxHeightCurrentMap(),
                isOutdoorMap()
        );
        animatedBackground.drawAtmosphere(c);

        if (listOfEntitiesMade)
            drawSortedEntities(c);

        // Vẽ Boss (chỉ khi boss đã tồn tại)
        if (boss != null && mapManager.getCurrentMapLevel() == 3) {
            boss.draw(c, cameraX, cameraY);
            if (boss.isActive()) {
                drawBossHealthBar(c);
            }
        }

        if (finalBoss != null && mapManager.getCurrentMapLevel() == 4) {
            finalBoss.drawSpells(c, cameraX, cameraY);
            finalBoss.draw(c, cameraX, cameraY);
            if (finalBoss.isActive()) {
                drawFinalBossHealthBar(c);
            }
        }

        drawEnemyArrows(c);
        drawProjectiles(c);
        drawEffectExplosions(c);
        drawSparkSkills(c);
        drawItems(c);
        drawSkillPulse(c);
        playingUI.draw(c);

        if (tutorialMode) {
            drawTutorialGuide(c);
        }
    }

    private void drawTutorialGuide(Canvas canvas) {
        float left = GAME_WIDTH * 0.28f;
        float top = GAME_HEIGHT * 0.13f;
        float right = GAME_WIDTH * 0.72f;
        float bottom = GAME_HEIGHT * 0.26f;
        float radius = 18 * Math.min(GAME_WIDTH / 1920f, GAME_HEIGHT / 1080f);

        canvas.drawRoundRect(left, top, right, bottom, radius, radius, tutorialPanelPaint);

        String title;
        String instruction;
        switch (tutorialStep) {
            case 0 -> {
                title = game.text("STEP 1 - MOVE", "BƯỚC 1 - DI CHUYỂN");
                instruction = game.text(
                        "Drag the MOVE joystick to walk around.",
                        "Kéo cần DI CHUYỂN để đi lại.");
            }
            case 1 -> {
                title = game.text("STEP 2 - ATTACK", "BƯỚC 2 - TẤN CÔNG");
                instruction = game.text(
                        "Tap ATTACK to swing your sword.",
                        "Nhấn TẤN CÔNG để vung vũ khí.");
            }
            case 2 -> {
                title = game.text("STEP 3 - USE A SKILL", "BƯỚC 3 - DÙNG KỸ NĂNG");
                instruction = game.text(
                        "Tap any skill icon to use a special attack.",
                        "Nhấn biểu tượng bất kỳ để dùng kỹ năng.");
            }
            case 3 -> {
                title = game.text(
                        "STEP 4 - DEFEAT THE ENEMY",
                        "BƯỚC 4 - TIÊU DIỆT KẺ ĐỊCH");
                instruction = game.text(
                        "Use attacks and skills to defeat the Skeleton.",
                        "Dùng đòn đánh và kỹ năng để hạ Bộ Xương.");
            }
            default -> {
                title = game.text("TUTORIAL COMPLETE", "HOÀN THÀNH HƯỚNG DẪN");
                instruction = game.text(
                        "Tap MENU in the top-left corner to return home.",
                        "Nhấn nút góc trên bên trái để về trang chủ.");
            }
        }

        canvas.drawText(title, GAME_WIDTH / 2f, GAME_HEIGHT * 0.185f, tutorialTitlePaint);
        canvas.drawText(instruction, GAME_WIDTH / 2f, GAME_HEIGHT * 0.235f, tutorialTextPaint);
    }



    private void drawBossHealthBar(Canvas canvas) {
        float barWidth = 150f;
        float centerX = boss.getHitbox().centerX() + cameraX;
        float y = boss.getHitbox().bottom + cameraY
                - Boss.DRAW_HEIGHT * ENEMY_DRAW_SCALE - 12f;
        float left = centerX - barWidth / 2f;
        float healthWidth = barWidth * boss.getCurrentHealth() / boss.getMaxHealth();

        canvas.drawLine(left, y, left + barWidth, y, healthBarBlack);
        canvas.drawLine(left, y, left + healthWidth, y, healthBarRed);
    }

    private void drawFinalBossHealthBar(Canvas canvas) {
        float barWidth = 300f;
        float centerX = finalBoss.getHitbox().centerX() + cameraX;
        float y = finalBoss.getHitbox().bottom + cameraY
                - 190f * ENEMY_DRAW_SCALE - 12f;
        float left = centerX - barWidth / 2f;
        float healthWidth = barWidth * finalBoss.getCurrentHealth()
                / (float) finalBoss.getMaxHealth();

        canvas.drawLine(left, y, left + barWidth, y, healthBarBlack);
        canvas.drawLine(left, y, left + healthWidth, y, healthBarRed);
    }

    private void drawProjectiles(Canvas c) {
        for (Projectile p : projectiles) {
            if (p.isActive()) {
                p.render(c, projectilePaint, cameraX, cameraY);
            }
        }
        for (ExplosionEffect effect : explosionEffects) {
            effect.render(c, cameraX, cameraY);
        }
    }

    private void drawEffectExplosions(Canvas c) {
        for (EffectExplosion explosion : effectExplosions) {
            if (explosion.isActive()) {
                explosion.render(c, cameraX, cameraY);
            }
        }
    }

    private void drawSparkSkills(Canvas c) {
        for (SparkSkill sparkSkill : sparkSkills) {
            if (sparkSkill.isActive()) {
                sparkSkill.render(c, cameraX, cameraY);
            }
        }
    }

    private void drawSkillPulse(Canvas canvas) {
        long elapsed = System.currentTimeMillis() - skillPulseStarted;
        if (elapsed < 0L || elapsed > 850L) return;

        float progress = elapsed / 850f;
        float centerX = skillPulseX + cameraX;
        float centerY = skillPulseY + cameraY;
        float eased = 1f - (1f - progress) * (1f - progress);
        float mainRadius = skillPulseRadius * (0.18f + 0.82f * eased);

        skillPulseFillPaint.setColor(skillPulseColor);
        skillPulseFillPaint.setAlpha(Math.max(0, 80 - Math.round(progress * 70f)));
        canvas.drawCircle(centerX, centerY, mainRadius, skillPulseFillPaint);

        skillPulsePaint.setColor(skillPulseColor);
        for (int ring = 0; ring < 3; ring++) {
            float ringProgress = Math.min(1f, progress + ring * 0.16f);
            float ringRadius = skillPulseRadius * (0.15f + ringProgress * 0.85f);
            skillPulsePaint.setStrokeWidth((10f - ring * 2f) * (1f - progress * 0.45f));
            skillPulsePaint.setAlpha(Math.max(0,
                    Math.round((190f - ring * 35f) * (1f - ringProgress))));
            canvas.drawCircle(centerX, centerY, ringRadius, skillPulsePaint);
        }

        skillParticlePaint.setColor(skillPulseColor);
        skillParticlePaint.setAlpha(Math.max(0, 220 - Math.round(progress * 190f)));
        float particleRadius = skillPulseRadius * (0.28f + progress * 0.60f);
        float rotation = elapsed * 0.22f;
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(rotation + i * 30f);
            float x = centerX + (float) Math.cos(angle) * particleRadius;
            float y = centerY + (float) Math.sin(angle) * particleRadius;
            float size = 8f + (i % 3) * 3f;
            canvas.drawCircle(x, y, size * (1f - progress * 0.55f),
                    skillParticlePaint);
        }

        if (skillPulseIconRes != 0 && progress < 0.65f) {
            float iconSize = 120f * (1f - progress * 0.35f);
            RectF iconBounds = new RectF(
                    centerX - iconSize / 2f,
                    centerY - iconSize / 2f,
                    centerX + iconSize / 2f,
                    centerY + iconSize / 2f
            );
            skillIconPaint.setAlpha(Math.max(0,
                    255 - Math.round(progress / 0.65f * 255f)));
            LoadoutIcons.drawResource(canvas, skillPulseIconRes,
                    iconBounds, skillIconPaint);
            skillIconPaint.setAlpha(255);
        }

        skillPulsePaint.setAlpha(255);
        skillPulsePaint.setStrokeWidth(12f);
        skillPulseFillPaint.setAlpha(255);
        skillParticlePaint.setAlpha(255);
    }

    private void drawEnemyArrows(Canvas canvas) {
        for (EnemyArrow arrow : enemyArrows) {
            arrow.draw(canvas, cameraX, cameraY);
        }
    }

    public boolean tryPlayerAttack() {
        if (!player.canAttack()) return false;

        player.setLastAttackTime();
        player.setAttacking(true);
        return true;
    }

    public void castSkill(int slot) {
        if (player.castSkill(slot, this) && tutorialMode && tutorialStep <= 2) {
            tutorialStep = 3;
        }
    }

    private void drawSortedEntities(Canvas c) {
        for (Entity e : listOfDrawables) {
            if (e instanceof SkeletonArcher archer) {
                if (archer.isVisible()) {
                    archer.draw(c, cameraX, cameraY);
                    if (archer.isActive()
                            && archer.getCurrentHealth() < archer.getMaxHealth()) {
                        drawHealthBar(c, archer);
                    }
                }
            } else if (e instanceof ShadowWraith wraith) {
                if (wraith.isActive()) {
                    wraith.draw(c, cameraX, cameraY);
                    if (wraith.getCurrentHealth() < wraith.getMaxHealth()) {
                        drawHealthBar(c, wraith);
                    }
                }
            } else if (e instanceof Skeleton skeleton) {
                if (skeleton.isActive())
                    drawCharacter(c, skeleton);
            } else if (e instanceof GameObject gameObject) {
                mapManager.drawObject(c, gameObject);
            } else if (e instanceof Building building) {
                mapManager.drawBuilding(c, building);
            } else if (e instanceof Item item) {
                mapManager.drawItem(c, item);
            } else if (e instanceof Player) {
                drawPlayer(c);
            } else if (e instanceof Boom boom) {
                if (boom.isActive())
                    drawBoom(c, boom);
            }
        }
    }


    private void drawPlayer(Canvas c) {
        c.drawBitmap(Weapons.SHADOW.getWeaponImg(), player.getHitbox().left, player.getHitbox().bottom - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, null);
        boolean weaponBehindPlayer = player.isAttacking()
                && player.getFaceDir() == GameConstants.Face_Dir.UP;
        if (weaponBehindPlayer) drawWeapon(c, player);
        drawPlayerSprite(c);
        if (player.isAttacking() && !weaponBehindPlayer) drawWeapon(c, player);
    }

    private void drawPlayerSprite(Canvas canvas) {
        Bitmap frame = player.getDisplayCharacter().getSprite(
                player.getDisplayAnimationIndex(), player.getFaceDir());
        float drawScale = player.getDisplayCharacter().getDrawScale();
        float centerX = player.getHitbox().centerX();
        float bottom = player.getHitbox().bottom;
        float width = frame.getWidth() * drawScale;
        float height = frame.getHeight() * drawScale;

        RectF destination = new RectF(
                centerX - width / 2f,
                bottom - height,
                centerX + width / 2f,
                bottom
        );
        canvas.drawBitmap(frame, null, destination, playerSpritePaint);
    }


    private void updatePlayerAppearance() {
        // Mỗi class đã có sprite riêng, không cần đổi màu từ cùng một ảnh nữa.
        playerSpritePaint.setColorFilter(null);
    }


    private void drawWeapon(Canvas c, Character character) {
        LoadoutManager.WeaponType weapon = game.getLoadoutManager().getWeapon();
        Bitmap weaponImage = LoadoutIcons.getIconBitmap(
                weapon.getIconColumn(), weapon.getIconRow());
        if (weaponImage == null) return;

        RectF playerBox = character.getHitbox();
        float characterScale = player.getDisplayCharacter().getDrawScale();
        float extraScale = characterScale - 1f;
        float handLift = 45f * extraScale;
        float handX;
        float handY;
        float wantedAngle;
        switch (character.getFaceDir()) {
            case GameConstants.Face_Dir.UP -> {
                handX = playerBox.centerX() + 16f * characterScale;
                handY = playerBox.top + 16f - handLift;
                wantedAngle = -90f;
            }
            case GameConstants.Face_Dir.LEFT -> {
                handX = playerBox.left - 2f - 38f * extraScale;
                handY = playerBox.centerY() + 4f - handLift;
                wantedAngle = 180f;
            }
            case GameConstants.Face_Dir.RIGHT -> {
                handX = playerBox.right + 2f + 38f * extraScale;
                handY = playerBox.centerY() + 4f - handLift;
                wantedAngle = 0f;
            }
            default -> {
                handX = playerBox.centerX() - 14f * characterScale;
                handY = playerBox.centerY() + 12f - handLift;
                wantedAngle = 90f;
            }
        }

        // Ba nhan vat moi co than hinh lon hon. Dat chuoi vu khi sat ban tay
        // thay vi day vu khi ra ngoai theo kich thuoc sprite.
        GameCharacters displayCharacter = player.getDisplayCharacter();
        boolean isNewCharacter = displayCharacter == GameCharacters.WARRIOR
                || displayCharacter == GameCharacters.ROGUE
                || displayCharacter == GameCharacters.GUARDIAN;
        if (isNewCharacter) {
            float handReach = 26f * characterScale;
            float handHeight = playerBox.bottom - 35f * characterScale;
            switch (character.getFaceDir()) {
                case GameConstants.Face_Dir.UP -> {
                    handX = playerBox.centerX() + 11f * characterScale;
                    handY = playerBox.bottom - 57f * characterScale;
                }
                case GameConstants.Face_Dir.LEFT -> {
                    handX = playerBox.centerX() - handReach;
                    handY = handHeight;
                }
                case GameConstants.Face_Dir.RIGHT -> {
                    handX = playerBox.centerX() + handReach;
                    handY = handHeight;
                }
                default -> {
                    handX = playerBox.centerX() - 11f * characterScale;
                    handY = playerBox.bottom - 31f * characterScale;
                }
            }
        }

        float naturalAngle = weapon == LoadoutManager.WeaponType.TWIN_DAGGERS
                ? -68f : -52f;
        float pivotXRatio = weapon == LoadoutManager.WeaponType.TWIN_DAGGERS
                ? 0.50f : 0.04f;
        float pivotYRatio = weapon == LoadoutManager.WeaponType.TWIN_DAGGERS
                ? 0.96f : 0.98f;
        float targetSize = switch (weapon) {
            case TWIN_DAGGERS -> 64f;
            case FROST_SPEAR -> 112f;
            default -> 88f;
        };
        float scale = targetSize / Math.max(weaponImage.getWidth(), weaponImage.getHeight());

        float progress = character.getAttackAnimationProgress();
        float easedProgress = progress * progress * (3f - 2f * progress);
        float swingAngle = -45f + 90f * easedProgress;
        if (character.getFaceDir() == GameConstants.Face_Dir.LEFT
                || character.getFaceDir() == GameConstants.Face_Dir.DOWN) {
            swingAngle = -swingAngle;
        }

        c.save();
        c.translate(handX, handY);
        c.rotate(wantedAngle - naturalAngle + swingAngle);
        c.scale(scale, scale);
        c.drawBitmap(weaponImage,
                -weaponImage.getWidth() * pivotXRatio,
                -weaponImage.getHeight() * pivotYRatio,
                playerWeaponPaint);
        c.restore();
    }

    private void drawEnemyWeapon(Canvas c, Character character) {
        c.rotate(character.getWepRot(), character.getAttackBox().left + cameraX, character.getAttackBox().top + cameraY);
        c.drawBitmap(Weapons.BIG_SWORD.getWeaponImg(), character.getAttackBox().left + cameraX + character.wepRotAdjustLeft(), character.getAttackBox().top + cameraY + character.wepRotAdjustTop(), null);
        c.rotate(character.getWepRot() * -1, character.getAttackBox().left + cameraX, character.getAttackBox().top + cameraY);
    }


    public void drawCharacter(Canvas canvas, Character c) {
        canvas.drawBitmap(Weapons.SHADOW.getWeaponImg(), c.getHitbox().left + cameraX, c.getHitbox().bottom - 5 * GameConstants.Sprite.SCALE_MULTIPLIER + cameraY, null);
        drawScaledEnemy(canvas,
                c.getGameCharType().getSprite(c.getAniIndex(), c.getFaceDir()),
                c.getHitbox());
        canvas.drawRect(c.getHitbox().left + cameraX, c.getHitbox().top + cameraY, c.getHitbox().right + cameraX, c.getHitbox().bottom + cameraY, redPaint);
        if (c.isAttacking())
            drawEnemyWeapon(canvas, c);

        if (c.getCurrentHealth() < c.getMaxHealth())
            drawHealthBar(canvas, c);
    }

    private void drawBoom(Canvas canvas, Boom boom) {
        canvas.drawBitmap(Weapons.SHADOW.getWeaponImg(), boom.getHitbox().left + cameraX, boom.getHitbox().bottom - 5 * GameConstants.Sprite.SCALE_MULTIPLIER + cameraY, null);
        drawScaledEnemy(canvas, boom.getBoomSprite(), boom.getHitbox(),
                boom.getMovementBobOffset());
        canvas.drawRect(boom.getHitbox().left + cameraX, boom.getHitbox().top + cameraY, boom.getHitbox().right + cameraX, boom.getHitbox().bottom + cameraY, redPaint);

        if (boom.getCurrentHealth() < boom.getMaxHealth())
            drawHealthBar(canvas, boom);
    }

    private void drawScaledEnemy(Canvas canvas, Bitmap sprite, RectF hitbox) {
        drawScaledEnemy(canvas, sprite, hitbox, 0f);
    }

    private void drawScaledEnemy(Canvas canvas, Bitmap sprite, RectF hitbox,
                                 float verticalOffset) {
        float width = sprite.getWidth() * ENEMY_DRAW_SCALE;
        float height = sprite.getHeight() * ENEMY_DRAW_SCALE;
        float centerX = hitbox.centerX() + cameraX;
        float bottom = hitbox.bottom + cameraY + verticalOffset;
        RectF destination = new RectF(
                centerX - width / 2f,
                bottom - height,
                centerX + width / 2f,
                bottom
        );
        canvas.drawBitmap(sprite, null, destination, null);
    }

    private void drawItems(Canvas c) {
        if (mapManager.getCurrentMap().getItemArrayList() != null) {
            for (Item item : mapManager.getCurrentMap().getItemArrayList()) {
                if (item.isActive()) {
                    item.render(c, cameraX, cameraY);
                }
            }
        }
    }

    private void drawHealthBar(Canvas canvas, Character c) {
        canvas.drawLine(c.getHitbox().left + cameraX,
                c.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER,
                c.getHitbox().right + cameraX,
                c.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, healthBarBlack);

        float fullBarWidth = c.getHitbox().width();
        float percentOfMaxHealth = (float) c.getCurrentHealth() / c.getMaxHealth();
        float barWidth = fullBarWidth * percentOfMaxHealth;

        canvas.drawLine(c.getHitbox().left + cameraX,
                c.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER,
                c.getHitbox().left + cameraX + barWidth,
                c.getHitbox().top + cameraY - 5 * GameConstants.Sprite.SCALE_MULTIPLIER, healthBarRed);
    }

    private void updatePlayerMove(double delta) {
        if (!movePlayer) return;

        float baseSpeed = (float) (delta * 300 * player.getSpeedMultiplier());

        double angle = Math.atan(Math.abs(lastTouchDiff.y) / Math.abs(lastTouchDiff.x));
        float xSpeed = (float) Math.cos(angle);
        float ySpeed = (float) Math.sin(angle);

        if (xSpeed > ySpeed) {
            player.setFaceDir(lastTouchDiff.x > 0 ? GameConstants.Face_Dir.RIGHT : GameConstants.Face_Dir.LEFT);
        } else {
            player.setFaceDir(lastTouchDiff.y > 0 ? GameConstants.Face_Dir.DOWN : GameConstants.Face_Dir.UP);
        }

        xSpeed = lastTouchDiff.x < 0 ? -xSpeed : xSpeed;
        ySpeed = lastTouchDiff.y < 0 ? -ySpeed : ySpeed;

        float deltaX = -xSpeed * baseSpeed;
        float deltaY = -ySpeed * baseSpeed;

        float deltaCameraX = -cameraX - deltaX;
        float deltaCameraY = -cameraY - deltaY;

        boolean hitWall = false;

        if (HelpMethods.CanWalkHere(
                player.getHitbox(),
                deltaCameraX,
                deltaCameraY,
                mapManager.getCurrentMap())) {
            cameraX += deltaX;
            cameraY += deltaY;
        } else {
            if (HelpMethods.CanWalkHereUpDown(
                    player.getHitbox(),
                    deltaCameraY,
                    -cameraX,
                    mapManager.getCurrentMap())) {
                cameraY += deltaY;
            } else {
                hitWall = true;
            }

            if (HelpMethods.CanWalkHereLeftRight(
                    player.getHitbox(),
                    deltaCameraX,
                    -cameraY,
                    mapManager.getCurrentMap())) {
                cameraX += deltaX;
            } else {
                hitWall = true;
            }

            if (hitWall) playPlayerHitWall();
        }
    }

    public void setGameStateToMenu() {
        pauseMenuOpen = false;
        game.setCurrentGameState(Game.GameState.MENU);
    }

    public void openPauseMenu() {
        if (pauseMenuOpen) return;
        pauseMenuOpen = true;
        pauseStartedAt = System.currentTimeMillis();
        movePlayer = false;
        lastTouchDiff = null;
        playingUI.resetInput();
    }

    public void continueGame() {
        if (!pauseMenuOpen) return;

        long pausedDuration = Math.max(0L,
                System.currentTimeMillis() - pauseStartedAt);
        shiftGameplayTimers(pausedDuration);

        pauseMenuOpen = false;
        pauseStartedAt = 0L;
        playingUI.resetInput();
    }

    public void onExternalPauseFinished(long pausedDuration) {
        if (pausedDuration <= 0L) return;
        shiftGameplayTimers(pausedDuration);
        if (pauseMenuOpen && pauseStartedAt > 0L) {
            // Exclude background time when continueGame() later measures how
            // long the foreground pause menu remained open.
            pauseStartedAt += pausedDuration;
        }
    }

    private void shiftGameplayTimers(long pausedDuration) {
        gameStartTime += pausedDuration;
        if (lastSpawnTime > 0L) lastSpawnTime += pausedDuration;
        if (slowEnemiesUntil > pauseStartedAt) slowEnemiesUntil += pausedDuration;
        if (skillPulseStarted > 0L) skillPulseStarted += pausedDuration;
        player.shiftTimers(pausedDuration);
        if (boss != null) boss.shiftTimers(pausedDuration);
        if (finalBoss != null) finalBoss.shiftTimers(pausedDuration);
    }


    public boolean isPauseMenuOpen() {
        return pauseMenuOpen;
    }

    public void setPlayerMoveTrue(PointF lastTouchDiff) {
        movePlayer = true;
        this.lastTouchDiff = lastTouchDiff;
    }

    public void setPlayerMoveFalse() {
        movePlayer = false;
        player.resetAnimation();
    }

    @Override
    public void touchEvents(MotionEvent event) {
        playingUI.touchEvents(event);
    }

    public Player getPlayer() {
        return player;
    }

    public PlayingUI getPlayingUI() {
        return playingUI;
    }

    private void playSwordHit() {
        if (isSwordSoundEnabled && soundPool != null) {
            float rate = game.getLoadoutManager().getWeapon().getSoundRate();
            soundPool.play(swordHitSoundId, 1, 1, 1, 0, rate);
        }
    }

    public void playSkillSound(LoadoutManager.SkillType skill) {
        if (!isSwordSoundEnabled || soundPool == null) return;

        switch (skill) {
            case CHARGED_BLAST, COMET_SHOT ->
                    soundPool.play(skillWhooshSoundId, 1, 1, 1, 0, 1.25f);
            case SPARK_STORM, OVERLOAD ->
                    soundPool.play(sparkSkillSoundId, 1, 1, 1, 0, 1.0f);
            case ARCANE_BURST, FLAME_RIFT ->
                    soundPool.play(explosionSkillSoundId, 1, 1, 1, 0, 1.0f);
            case FROST_PULSE, GLACIAL_RING ->
                    soundPool.play(playerHitWallSoundId, 0.85f, 0.85f, 1, 0, 1.65f);
            case SACRIFICE_NOVA, VOID_DRAIN ->
                    soundPool.play(explosionSkillSoundId, 1, 1, 1, 0, 0.65f);
        }
    }

    public void setSwordSoundEnabled(boolean enabled) {
        isSwordSoundEnabled = enabled;
    }


    public void dispose() {
        disposed = true;
        synchronized (preloadLock) {
            if (mapPreloadExecutor != null) {
                mapPreloadExecutor.shutdownNow();
                mapPreloadExecutor = null;
            }
        }
        resetInput();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    public void resetInput() {
        playingUI.resetInput();
    }

    public void clearTemporaryAttacks() {
        projectiles.clear();
        enemyArrows.clear();
        explosionEffects.clear();
        effectExplosions.clear();
        sparkSkills.clear();
        player.setAttacking(false);
        movePlayer = false;
        lastTouchDiff = null;
        resetInput();
    }

    public void addProjectile(Projectile p) {
        projectiles.add(p);
    }

    public void addEnemyArrow(EnemyArrow arrow) {
        enemyArrows.add(arrow);
    }

    public void addEffectExplosion(EffectExplosion explosion) {
        effectExplosions.add(explosion);
    }

    public void addSparkSkill(SparkSkill sparkSkill) {
        sparkSkills.add(sparkSkill);
    }

    public void addExplosionEffect(ExplosionEffect explosionEffect) {
        explosionEffects.add(explosionEffect);
    }

    public void playBoomExplosionSound() {
        if (isSwordSoundEnabled) {
            soundPool.play(boomExplosionSoundId, 1, 1, 1, 0, 1f);
        }
    }

    public Skeleton findNearestSkeleton(float px, float py, float range) {
        Skeleton nearest = null;
        float minDistSq = range * range;

        if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
            for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList()) {
                if (!s.isActive()) continue;
                float dx = s.getHitbox().centerX() - px;
                float dy = s.getHitbox().centerY() - py;
                float distSq = dx * dx + dy * dy;

                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = s;
                }
            }
        }
        return nearest;
    }

    public PointF findNearestEnemyPosition(float px, float py, float range) {
        PointF nearest = null;
        float minDistSq = range * range;

        if (finalBoss != null && finalBoss.isActive()) {
            float dx = finalBoss.getHitbox().centerX() - px;
            float dy = finalBoss.getHitbox().centerY() - py;
            if (dx * dx + dy * dy <= minDistSq) {
                return new PointF(
                        finalBoss.getHitbox().centerX(),
                        finalBoss.getHitbox().centerY());
            }
        }

        // Khi boss đang ở trong tầm, skill ưu tiên boss thay vì quái thường.
        if (boss != null && boss.isActive()) {
            float bossDx = boss.getHitbox().centerX() - px;
            float bossDy = boss.getHitbox().centerY() - py;
            if (bossDx * bossDx + bossDy * bossDy <= minDistSq) {
                return new PointF(boss.getHitbox().centerX(), boss.getHitbox().centerY());
            }
        }

        if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
            for (Skeleton skeleton : mapManager.getCurrentMap().getSkeletonArrayList()) {
                if (!skeleton.isActive()) continue;
                float dx = skeleton.getHitbox().centerX() - px;
                float dy = skeleton.getHitbox().centerY() - py;
                float distSq = dx * dx + dy * dy;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = new PointF(
                            skeleton.getHitbox().centerX(),
                            skeleton.getHitbox().centerY());
                }
            }
        }

        if (mapManager.getCurrentMap().getBoomArrayList() != null) {
            for (Boom boom : mapManager.getCurrentMap().getBoomArrayList()) {
                if (!boom.isActive()) continue;
                float dx = boom.getHitbox().centerX() - px;
                float dy = boom.getHitbox().centerY() - py;
                float distSq = dx * dx + dy * dy;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = new PointF(boom.getHitbox().centerX(), boom.getHitbox().centerY());
                }
            }
        }

        if (boss != null && boss.isActive()) {
            float dx = boss.getHitbox().centerX() - px;
            float dy = boss.getHitbox().centerY() - py;
            float distSq = dx * dx + dy * dy;
            if (distSq < minDistSq) {
                nearest = new PointF(boss.getHitbox().centerX(), boss.getHitbox().centerY());
            }
        }

        return nearest;
    }

    public boolean damageBossIfHit(RectF hitbox, int damage) {
        if (finalBoss != null && finalBoss.isActive()
                && RectF.intersects(hitbox, finalBoss.getHitbox())) {
            finalBoss.damage(damage);
            if (!finalBoss.isActive()) enemyKilled();
            return true;
        }

        if (boss != null && boss.isActive() && RectF.intersects(hitbox, boss.getHitbox())) {
            boss.damage(damage);
            if (!boss.isActive()) enemyKilled();
            return true;
        }

        return false;
    }

    public void damageEnemiesInRadius(float centerX, float centerY, float radius,
                                      int damage, boolean applySlow) {
        float radiusSquared = radius * radius;

        if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
            for (Skeleton skeleton : mapManager.getCurrentMap().getSkeletonArrayList()) {
                if (!skeleton.isActive()) continue;
                float dx = skeleton.getHitbox().centerX() - centerX;
                float dy = skeleton.getHitbox().centerY() - centerY;
                if (dx * dx + dy * dy > radiusSquared) continue;

                skeleton.damageCharacter(damage);
                if (skeleton.getCurrentHealth() <= 0) {
                    skeleton.setSkeletonInactive();
                    enemyKilled();
                }
            }
        }

        if (mapManager.getCurrentMap().getBoomArrayList() != null) {
            for (Boom boom : mapManager.getCurrentMap().getBoomArrayList()) {
                if (!boom.isActive()) continue;
                float dx = boom.getHitbox().centerX() - centerX;
                float dy = boom.getHitbox().centerY() - centerY;
                if (dx * dx + dy * dy > radiusSquared) continue;

                boom.damageCharacter(damage);
                if (boom.getCurrentHealth() <= 0) {
                    boom.setBoomInactive();
                    enemyKilled();
                }
            }
        }

        if (boss != null && boss.isActive()) {
            float dx = boss.getHitbox().centerX() - centerX;
            float dy = boss.getHitbox().centerY() - centerY;
            if (dx * dx + dy * dy <= radiusSquared) {
                boss.damage(damage);
                if (!boss.isActive()) enemyKilled();
            }
        }

        if (finalBoss != null && finalBoss.isActive()) {
            float dx = finalBoss.getHitbox().centerX() - centerX;
            float dy = finalBoss.getHitbox().centerY() - centerY;
            if (dx * dx + dy * dy <= radiusSquared) {
                finalBoss.damage(damage);
                if (!finalBoss.isActive()) enemyKilled();
            }
        }

        if (applySlow) slowEnemiesUntil = System.currentTimeMillis() + 3000L;
    }

    public void showAreaSkillEffect(float centerX, float centerY, float radius,
                                    LoadoutManager.SkillType skill) {
        skillPulseStarted = System.currentTimeMillis();
        skillPulseX = centerX;
        skillPulseY = centerY;
        skillPulseRadius = radius;
        skillPulseIconRes = skill.getIconResourceId();
        skillPulseColor = switch (skill) {
            case ARCANE_BURST -> Color.rgb(176, 92, 255);
            case SACRIFICE_NOVA, FLAME_RIFT, VOID_DRAIN ->
                    Color.rgb(40, 190, 255);
            case GLACIAL_RING -> Color.rgb(90, 220, 255);
            case OVERLOAD -> Color.rgb(255, 220, 45);
            default -> Color.CYAN;
        };
    }


    private void updateProjectiles(double delta) {
        for (Projectile p : projectiles) {
            if (!p.isActive()) continue;
            p.update(delta);
            if (!p.isReady()) continue;
            if (mapManager.getCurrentMap().getSkeletonArrayList() != null) {
                for (Skeleton s : mapManager.getCurrentMap().getSkeletonArrayList()) {
                    if (!s.isActive()) continue;
                    if (RectF.intersects(p.getHitbox(), s.getHitbox())) {
                        s.damageCharacter(p.getDamage());
                        explosionEffects.add(new ExplosionEffect(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY())));
                        if (s.getCurrentHealth() <= 0) {
                            s.setSkeletonInactive();
                            enemyKilled();
                            if (!s.hasDroppedItem()) {
                                s.setHasDroppedItem(true);
                                Item droppedItem = HelpMethods.tryDropItem(new PointF(s.getHitbox().centerX(), s.getHitbox().centerY()));
                                if (droppedItem != null) {
                                    mapManager.getCurrentMap().getItemArrayList().add(droppedItem);
                                }
                            }
                        }
                        applyProjectileStatus(p);
                        p.deactivate();
                        break;
                    }
                }
            }

            if (!p.isActive()) continue;

            if (mapManager.getCurrentMap().getBoomArrayList() != null) {
                for (Boom boom : mapManager.getCurrentMap().getBoomArrayList()) {
                    if (!boom.isActive()) continue;
                    if (RectF.intersects(p.getHitbox(), boom.getHitbox())) {
                        boom.damageCharacter(p.getDamage());
                        explosionEffects.add(new ExplosionEffect(new PointF(boom.getHitbox().centerX(), boom.getHitbox().centerY())));
                        if (boom.getCurrentHealth() <= 0) {
                            boom.setBoomInactive();
                            enemyKilled();
                        }
                        applyProjectileStatus(p);
                        p.deactivate();
                        break;
                    }
                }
            }

            if (p.isActive() && boss != null && boss.isActive()
                    && RectF.intersects(p.getHitbox(), boss.getHitbox())) {
                boss.damage(p.getDamage());
                explosionEffects.add(new ExplosionEffect(
                        new PointF(boss.getHitbox().centerX(), boss.getHitbox().centerY())));
                applyProjectileStatus(p);
                p.deactivate();
                if (!boss.isActive()) {
                    enemyKilled();
                }
            }

            if (p.isActive() && finalBoss != null && finalBoss.isActive()
                    && RectF.intersects(p.getHitbox(), finalBoss.getHitbox())) {
                finalBoss.damage(p.getDamage());
                explosionEffects.add(new ExplosionEffect(
                        new PointF(finalBoss.getHitbox().centerX(),
                                finalBoss.getHitbox().centerY())));
                applyProjectileStatus(p);
                p.deactivate();
                if (!finalBoss.isActive()) enemyKilled();
            }

            if (p.isOutOfBounds(mapManager.getMaxWidthCurrentMap(), mapManager.getMaxHeightCurrentMap())) {
                p.deactivate();
            }
        }

        projectiles.removeIf(p -> !p.isActive());

        Iterator<ExplosionEffect> it = explosionEffects.iterator();
        while (it.hasNext()) {
            ExplosionEffect effect = it.next();
            effect.update();
            if (!effect.isActive()) it.remove();
        }
    }

    private void updateEffectExplosions(double delta) {
        Iterator<EffectExplosion> it = effectExplosions.iterator();
        while (it.hasNext()) {
            EffectExplosion explosion = it.next();
            if (explosion.isActive()) {
                explosion.update(delta, this);
            } else {
                it.remove();
            }
        }
    }

    private void updateSparkSkills(double delta) {
        Iterator<SparkSkill> it = sparkSkills.iterator();

        while (it.hasNext()) {
            SparkSkill sparkSkill = it.next();
            if (sparkSkill.isActive()) {
                sparkSkill.update(delta, this);
            } else {
                it.remove();
            }
        }
    }

    public float getCameraX() {
        return cameraX;
    }

    public float getCameraY() {
        return cameraY;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    private void playPlayerHitWall() {
        if (!isSwordSoundEnabled || soundPool == null) return;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastWallSoundTime < 180) return;

        lastWallSoundTime = currentTime;
        soundPool.play(playerHitWallSoundId, 1, 1, 1, 0, 1f);
    }

    private void applyProjectileStatus(Projectile projectile) {
        if (!projectile.slowsEnemy()) return;
        slowEnemiesUntil = System.currentTimeMillis() + 3000L;
        RectF hitbox = projectile.getHitbox();
        skillPulseStarted = System.currentTimeMillis();
        skillPulseX = hitbox.centerX();
        skillPulseY = hitbox.centerY();
        skillPulseRadius = 180f;
        skillPulseColor = Color.rgb(105, 225, 255);
        skillPulseIconRes = 0;
    }

    private void maybeScheduleNextMapPreload() {
        if (tutorialMode || disposed) return;

        int currentMapLevel = mapManager.getCurrentMapLevel();
        if (currentMapLevel < 1 || currentMapLevel >= 4) return;
        if (currentMapLevel == 3 && currentDifficulty != Game.Difficulty.HARD) return;
        if (System.currentTimeMillis() - gameStartTime < MAP_PRELOAD_DELAY) return;

        int nextMapLevel = currentMapLevel + 1;
        synchronized (preloadLock) {
            if (preloadScheduled[nextMapLevel] || disposed) return;
            preloadScheduled[nextMapLevel] = true;

            if (mapPreloadExecutor == null || mapPreloadExecutor.isShutdown()) {
                mapPreloadExecutor = Executors.newSingleThreadExecutor(task -> {
                    Thread thread = new Thread(() -> {
                        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        task.run();
                    }, "MapAssetPreloader");
                    thread.setDaemon(true);
                    return thread;
                });
            }
            mapPreloadExecutor.execute(() -> preloadMapAssets(nextMapLevel));
        }
    }

    private void preloadMapAssets(int mapLevel) {
        if (disposed || Thread.currentThread().isInterrupted()) return;
        try {
            animatedBackground.preload(mapLevel);
            mapManager.preloadMapResources(mapLevel);

            if (mapLevel == 3) {
                for (BossAnimation animation : BossAnimation.values()) {
                    if (Thread.currentThread().isInterrupted()) return;
                    animation.getSprites();
                }
            } else if (mapLevel == 4) {
                SkeletonArcher.preloadSprite();
                ShadowWraith.preloadSprite();
                for (FinalBossAnimation animation : FinalBossAnimation.values()) {
                    if (Thread.currentThread().isInterrupted()) return;
                    animation.getFrame(0, 0);
                }
            }
        } catch (RuntimeException exception) {
            // Keep gameplay alive if a device cannot decode an optional asset;
            // the normal lazy loader can retry when the map becomes active.
            synchronized (preloadLock) {
                preloadScheduled[mapLevel] = false;
            }
            System.err.println("Unable to preload map " + mapLevel
                    + ": " + exception.getMessage());
        }
    }

    private void checkVictoryCondition() {
        long currentTime = System.currentTimeMillis();
        int currentMapLevel = mapManager.getCurrentMapLevel();
        boolean survivalTimeFinished = currentTime - gameStartTime >= VICTORY_TIME;

        if ((currentMapLevel == 1 || currentMapLevel == 2) && survivalTimeFinished) {
            PointF savedPosition = getPlayerWorldPosition();
            mapManager.progressToNextMap();
            gameStartTime = System.currentTimeMillis();
            restorePlayerWorldPosition(savedPosition);
            return;
        }

        if (currentMapLevel == 3 && survivalTimeFinished && boss != null
                && boss.isDeathAnimationFinished()) {
            if (currentDifficulty == Game.Difficulty.HARD) {
                PointF savedPosition = getPlayerWorldPosition();
                mapManager.progressToNextMap();
                gameStartTime = System.currentTimeMillis();
                restorePlayerWorldPosition(savedPosition);
            } else {
                game.getWinScreen().setKillCount(killCount);
                game.setCurrentGameState(Game.GameState.WIN_SCREEN);
            }
            return;
        }

        if (currentMapLevel == 4 && finalBoss != null
                && finalBoss.isDeathAnimationFinished()) {
            game.startTrueEnding(killCount);
        }
    }

    private PointF getPlayerWorldPosition() {
        return new PointF(
                player.getHitbox().centerX() - cameraX,
                player.getHitbox().centerY() - cameraY
        );
    }

    private void restorePlayerWorldPosition(PointF savedPosition) {
        float halfWidth = player.getHitbox().width() / 2f;
        float halfHeight = player.getHitbox().height() / 2f;
        float worldX = Math.max(halfWidth, Math.min(
                mapManager.getMaxWidthCurrentMap() - halfWidth, savedPosition.x));
        float worldY = Math.max(halfHeight, Math.min(
                mapManager.getMaxHeightCurrentMap() - halfHeight, savedPosition.y));

        cameraX = player.getHitbox().centerX() - worldX;
        cameraY = player.getHitbox().centerY() - worldY;
        mapManager.setCameraValues(cameraX, cameraY);
    }

    public void enemyKilled() {
        if (tutorialMode) {
            tutorialStep = 4;
            return;
        }
        killCount++;
    }

    public void setDifficulty(Game.Difficulty difficulty) {
        this.currentDifficulty = difficulty;
    }

    public Game.Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    public boolean isTutorialMode() {
        return tutorialMode;
    }

    public void resetTutorial() {
        if (!tutorialMode) return;

        mapManager.resetTutorialMap();
        calcStartCameraValues();
        player.resetPosition(GAME_WIDTH / 2f, GAME_HEIGHT / 2f);
        player.applyLoadout(game.getLoadoutManager());
        player.resetCharacterHealth();
        player.resetCooldowns();
        updatePlayerAppearance();
        player.resetAnimation();
        movePlayer = false;
        lastTouchDiff = null;
        projectiles.clear();
        enemyArrows.clear();
        explosionEffects.clear();
        effectExplosions.clear();
        sparkSkills.clear();
        slowEnemiesUntil = 0L;
        skillPulseStarted = 0L;
        tutorialStep = 0;
        pauseMenuOpen = false;
        pauseStartedAt = 0L;
        boss = null;
        finalBoss = null;
        resetInput();
    }

    public void resetGame() {
        if (tutorialMode) {
            resetTutorial();
            return;
        }

        mapManager.resetToMap1();
        calcStartCameraValues();
        player.applyLoadout(game.getLoadoutManager());
        player.resetCharacterHealth();
        player.resetCooldowns();
        updatePlayerAppearance();
        player.resetAnimation();
        movePlayer = false;
        lastTouchDiff = null;
        projectiles.clear();
        enemyArrows.clear();
        explosionEffects.clear();
        effectExplosions.clear();
        sparkSkills.clear();
        slowEnemiesUntil = 0L;
        skillPulseStarted = 0L;
        lastSpawnTime = 0;
        gameStartTime = System.currentTimeMillis();
        killCount = 0;
        pauseMenuOpen = false;
        pauseStartedAt = 0L;
        mapManager.resetAllMaps();

        // THAY ĐỔI: Xóa boss khi reset game
        boss = null;
        finalBoss = null;
    }
}
