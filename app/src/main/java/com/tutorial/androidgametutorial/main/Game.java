package com.tutorial.androidgametutorial.main;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.gamestates.DeathScreen;
import com.tutorial.androidgametutorial.gamestates.DifficultyScreen;
import com.tutorial.androidgametutorial.gamestates.CharacterSelectScreen;
import com.tutorial.androidgametutorial.gamestates.CinematicScreen;
import com.tutorial.androidgametutorial.gamestates.LeaderboardScreen;
import com.tutorial.androidgametutorial.gamestates.LoadoutScreen;
import com.tutorial.androidgametutorial.gamestates.Menu;
import com.tutorial.androidgametutorial.gamestates.Playing;
import com.tutorial.androidgametutorial.gamestates.SplashScreen;
import com.tutorial.androidgametutorial.gamestates.WinScreen;
import com.tutorial.androidgametutorial.ui.CustomButton;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Game {

    public static final String GAME_TITLE = "JOURNEY OF THE MONSTER SLAYER";
    public static final String STUDIO_NAME = "KMA STUDIO";
    private static final long BACKGROUND_RESET_TIMEOUT_MS = 5 * 60 * 1000L;

    private SurfaceHolder holder;
    private SplashScreen splashScreen;
    private CinematicScreen openingScreen;
    private CinematicScreen trueEndingScreen;
    private Menu menu;
    private Playing playing;
    private DeathScreen deathScreen;
    private WinScreen winScreen;
    private LeaderboardScreen leaderboardScreen;
    private DifficultyScreen difficultyScreen;
    private CharacterSelectScreen characterSelectScreen;
    private LoadoutScreen loadoutScreen;
    private Playing tutorialPlaying;
    private GameLoop gameLoop;
    private volatile GameState currentGameState = GameState.SPLASH;

    private Context context;
    private final LoadoutManager loadoutManager;
    private final LanguageManager languageManager;
    private MediaPlayer backgroundMusic;
    private CustomButton toggleMusicButton, toggleSwordSoundButton;
    private boolean isMusicOn = true, isSwordSoundOn = true;

    // Difficulty system
    private Difficulty currentDifficulty = Difficulty.EASY;

    // Toggle icons are scaled once during initialization, not every frame.
    private Bitmap musicIcon, soundIcon;
    private final Paint audioButtonPaint = new Paint();
    private final Paint audioButtonBorderPaint = new Paint();
    private final Paint audioOffPaint = new Paint();

    // Add surface management variables
    private volatile boolean surfaceReady = false;
    private volatile boolean paused = false;
    private volatile boolean destroyed = false;
    private long lifecyclePauseStartedAt;
    private final Object renderLock = new Object();
    private final Queue<MotionEvent> pendingTouchEvents = new ConcurrentLinkedQueue<>();
    private boolean hardwareCanvasEnabled = true;
    private int consecutiveHardwareCanvasFailures;

    public Game(SurfaceHolder holder, Context context) {
        this.holder = holder;
        this.context = context;
        languageManager = new LanguageManager(context);
        loadoutManager = new LoadoutManager(context);
        gameLoop = new GameLoop(this);
        initGameStates();
    }

    private void initGameStates() {
        splashScreen = new SplashScreen(this);
        openingScreen = new CinematicScreen(this, CinematicScreen.Type.OPENING);
        trueEndingScreen = new CinematicScreen(this, CinematicScreen.Type.TRUE_ENDING);
        menu = new Menu(this);
        playing = new Playing(this);
        deathScreen = new DeathScreen(this);
        winScreen = new WinScreen(this);
        leaderboardScreen = new LeaderboardScreen(this);
        difficultyScreen = new DifficultyScreen(this);
        characterSelectScreen = new CharacterSelectScreen(this);
        loadoutScreen = new LoadoutScreen(this);
        tutorialPlaying = new Playing(this, true);

        // Initialize MediaPlayer for background music from assets folder
        try {
            AssetFileDescriptor afd = context.getAssets().openFd("background.mp3");
            backgroundMusic = new MediaPlayer();
            backgroundMusic.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            backgroundMusic.prepare();
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f, 0.5f);
            afd.close();
        } catch (Exception e) {
            System.err.println("Error initializing MediaPlayer from assets: " + e.getMessage());
            backgroundMusic = null;
        }

        // Keep the same layout as the original 1920x1080 design, but scale it
        // for the current screen size.
        float uiScale = Math.min(
                MainActivity.GAME_WIDTH / 1920f,
                MainActivity.GAME_HEIGHT / 1080f
        );
        int buttonSize = Math.max(1, Math.round(86 * uiScale));
        int iconSize = Math.max(1, Math.round(58 * uiScale));
        float padding = 20 * uiScale;
        float buttonGap = 15 * uiScale;
        float screenWidth = MainActivity.GAME_WIDTH;

        musicIcon = loadScaledIcon(R.drawable.music, iconSize);
        soundIcon = loadScaledIcon(R.drawable.sound, iconSize);

        toggleMusicButton = new CustomButton(screenWidth - buttonSize - padding, padding, buttonSize, buttonSize);
        toggleSwordSoundButton = new CustomButton(screenWidth - buttonSize - padding, padding + buttonSize + buttonGap, buttonSize, buttonSize);

        audioButtonPaint.setAntiAlias(true);
        audioButtonBorderPaint.setStyle(Paint.Style.STROKE);
        audioButtonBorderPaint.setStrokeWidth(Math.max(2f, 4f * uiScale));
        audioButtonBorderPaint.setColor(Color.rgb(240, 205, 90));
        audioButtonBorderPaint.setAntiAlias(true);
        audioOffPaint.setColor(Color.rgb(245, 65, 65));
        audioOffPaint.setStrokeWidth(Math.max(3f, 7f * uiScale));
        audioOffPaint.setStrokeCap(Paint.Cap.ROUND);
        audioOffPaint.setAntiAlias(true);
    }

    private Bitmap loadScaledIcon(int resourceId, int size) {
        Bitmap original = BitmapFactory.decodeResource(context.getResources(), resourceId);
        if (original == null) return null;

        Bitmap scaled = Bitmap.createScaledBitmap(original, size, size, false);
        if (scaled != original) {
            original.recycle();
        }
        return scaled;
    }

    public void update(double delta) {
        if (paused || destroyed) return;
        processPendingTouchEvents();

        // Only play background music in PLAYING state
        if (currentGameState == GameState.PLAYING
                || currentGameState == GameState.HOW_TO_PLAY
                || currentGameState == GameState.OPENING
                || currentGameState == GameState.TRUE_ENDING) {
            if (isMusicOn) {
                playBackgroundMusic();
            }
        } else {
            pauseBackgroundMusic();
        }

        switch (currentGameState) {
            case SPLASH -> splashScreen.update(delta);
            case OPENING -> openingScreen.update(delta);
            case MENU -> menu.update(delta);
            case PLAYING -> playing.update(delta);
            case DEATH_SCREEN -> deathScreen.update(delta);
            case WIN_SCREEN -> winScreen.update(delta);
            case LEADERBOARD -> leaderboardScreen.update(delta);
            case DIFFICULTY -> difficultyScreen.update(delta);
            case CHARACTER_SELECT -> characterSelectScreen.update(delta);
            case LOADOUT -> loadoutScreen.update(delta);
            case HOW_TO_PLAY -> tutorialPlaying.update(delta);
            case TRUE_ENDING -> trueEndingScreen.update(delta);
        }
    }

    public void render() {
        if (paused || destroyed) return;
        // Synchronize rendering to prevent drawing on a released surface
        synchronized (renderLock) {
            if (!surfaceReady) return;

            Canvas c = null;
            try {
                c = lockRenderCanvas();
                if (c == null) return; // Surface not ready

                c.drawColor(Color.BLACK);

                // Draw the game
                switch (currentGameState) {
                    case SPLASH -> splashScreen.render(c);
                    case OPENING -> openingScreen.render(c);
                    case MENU -> menu.render(c);
                    case PLAYING -> {
                        playing.render(c);

                        drawAudioToggle(c, toggleMusicButton, musicIcon, isMusicOn);
                        drawAudioToggle(c, toggleSwordSoundButton, soundIcon, isSwordSoundOn);
                    }
                    case DEATH_SCREEN -> deathScreen.render(c);
                    case WIN_SCREEN -> winScreen.render(c);
                    case LEADERBOARD -> leaderboardScreen.render(c);
                    case DIFFICULTY -> difficultyScreen.render(c);
                    case CHARACTER_SELECT -> characterSelectScreen.render(c);
                    case LOADOUT -> loadoutScreen.render(c);
                    case HOW_TO_PLAY -> tutorialPlaying.render(c);
                    case TRUE_ENDING -> trueEndingScreen.render(c);
                }
            } catch (Exception e) {
                // Log the error but don't crash
                System.err.println("Error during rendering: " + e.getMessage());
            } finally {
                if (c != null) {
                    try {
                        holder.unlockCanvasAndPost(c);
                    } catch (Exception e) {
                        // Surface was already released, ignore
                        System.err.println("Surface already released during unlock: " + e.getMessage());
                    }
                }
            }
        }
    }

    public boolean touchEvent(MotionEvent event) {
        if (!destroyed && !paused) {
            // Android recycles MotionEvent objects after onTouchEvent returns,
            // so queue a copy for the game thread.
            pendingTouchEvents.offer(MotionEvent.obtain(event));
            if (isInteractivePauseOpen()) {
                gameLoop.requestPausedFrame();
            }
        }
        return true;
    }

    boolean shouldSuspendFrameLoop() {
        return paused || destroyed || !surfaceReady || isInteractivePauseOpen();
    }

    private boolean isInteractivePauseOpen() {
        if (currentGameState == GameState.PLAYING) {
            return playing.isPauseMenuOpen();
        }
        return currentGameState == GameState.HOW_TO_PLAY
                && tutorialPlaying.isPauseMenuOpen();
    }

    private void processPendingTouchEvents() {
        MotionEvent event;
        while ((event = pendingTouchEvents.poll()) != null) {
            try {
                handleTouchEvent(event);
            } finally {
                event.recycle();
            }
        }
    }

    private Canvas lockRenderCanvas() {
        if (hardwareCanvasEnabled) {
            try {
                Canvas canvas = holder.lockHardwareCanvas();
                if (canvas != null) {
                    consecutiveHardwareCanvasFailures = 0;
                    return canvas;
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                consecutiveHardwareCanvasFailures++;
                if (consecutiveHardwareCanvasFailures >= 3) {
                    hardwareCanvasEnabled = false;
                    System.err.println(
                            "Hardware Canvas unavailable; using software Canvas."
                    );
                }
            }
        }

        // Some emulators and vendor devices can temporarily reject a hardware
        // canvas while their Surface is being recreated. Keep a safe fallback.
        return holder.lockCanvas();
    }

    private void handleTouchEvent(MotionEvent event) {
        if (currentGameState == GameState.PLAYING
                && event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (toggleMusicButton.getHitbox().contains(event.getX(), event.getY())) {
                toggleMusic();
                return;
            } else if (toggleSwordSoundButton.getHitbox().contains(event.getX(), event.getY())) {
                toggleSound();
                return;
            }
        }

        switch (currentGameState) {
            case SPLASH -> splashScreen.touchEvents(event);
            case OPENING -> openingScreen.touchEvents(event);
            case MENU -> menu.touchEvents(event);
            case PLAYING -> playing.touchEvents(event);
            case DEATH_SCREEN -> deathScreen.touchEvents(event);
            case WIN_SCREEN -> winScreen.touchEvents(event);
            case LEADERBOARD -> leaderboardScreen.touchEvents(event);
            case DIFFICULTY -> difficultyScreen.touchEvents(event);
            case CHARACTER_SELECT -> characterSelectScreen.touchEvents(event);
            case LOADOUT -> loadoutScreen.touchEvents(event);
            case HOW_TO_PLAY -> tutorialPlaying.touchEvents(event);
            case TRUE_ENDING -> trueEndingScreen.touchEvents(event);
        }

    }

    private void drawAudioToggle(Canvas canvas, CustomButton button,
                                 Bitmap icon, boolean enabled) {
        RectF box = button.getHitbox();
        audioButtonPaint.setColor(enabled
                ? Color.argb(225, 25, 58, 70)
                : Color.argb(225, 65, 35, 45));
        float radius = box.width() * 0.18f;
        canvas.drawRoundRect(box, radius, radius, audioButtonPaint);
        canvas.drawRoundRect(box, radius, radius, audioButtonBorderPaint);

        if (icon != null) {
            float iconX = box.centerX() - icon.getWidth() / 2f;
            float iconY = box.centerY() - icon.getHeight() / 2f;
            canvas.drawBitmap(icon, iconX, iconY, null);
        }

        if (!enabled) {
            float padding = box.width() * 0.2f;
            canvas.drawLine(box.left + padding, box.top + padding,
                    box.right - padding, box.bottom - padding, audioOffPaint);
        }
    }

    public void startGameLoop() {
        if (surfaceReady && !paused && !destroyed) {
            gameLoop.startGameLoop();
        }
    }

    public enum GameState {
        SPLASH, OPENING, MENU, PLAYING, DEATH_SCREEN, WIN_SCREEN, LEADERBOARD,
        DIFFICULTY, CHARACTER_SELECT, LOADOUT, HOW_TO_PLAY, TRUE_ENDING;
    }

    public enum Difficulty {
        EASY, HARD;
    }

    public Difficulty getCurrentDifficulty() {
        return currentDifficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        // Pass the difficulty setting to Playing class
        playing.setDifficulty(difficulty);
    }

    public void setCurrentDifficulty(Difficulty currentDifficulty) {
        this.currentDifficulty = currentDifficulty;
        // Pass the difficulty setting to Playing class
        playing.setDifficulty(currentDifficulty);
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public void setCurrentGameState(GameState currentGameState) {
        this.currentGameState = currentGameState;
    }

    public void startOpeningCinematic() {
        openingScreen.restart();
        currentGameState = GameState.OPENING;
    }

    public void finishOpeningCinematic() {
        currentGameState = GameState.MENU;
    }

    public void startTrueEnding(int killCount) {
        winScreen.setKillCount(killCount);
        trueEndingScreen.restart();
        currentGameState = GameState.TRUE_ENDING;
    }

    public void finishTrueEnding() {
        currentGameState = GameState.WIN_SCREEN;
    }

    public boolean isMusicOn() {
        return isMusicOn;
    }

    public boolean isSoundOn() {
        return isSwordSoundOn;
    }

    public void toggleMusic() {
        isMusicOn = !isMusicOn;
        if (isMusicOn && currentGameState == GameState.PLAYING) {
            playBackgroundMusic();
        } else {
            pauseBackgroundMusic();
        }
    }

    public void toggleSound() {
        isSwordSoundOn = !isSwordSoundOn;
        playing.setSwordSoundEnabled(isSwordSoundOn);
        tutorialPlaying.setSwordSoundEnabled(isSwordSoundOn);
    }

    public void startTutorial() {
        tutorialPlaying.resetTutorial();
        currentGameState = GameState.HOW_TO_PLAY;
    }

    public void exitGame() {
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public Menu getMenu() {
        return menu;
    }

    public Playing getPlaying() {
        return playing;
    }

    public LoadoutManager getLoadoutManager() {
        return loadoutManager;
    }

    public LanguageManager getLanguage() {
        return languageManager;
    }

    public String text(String english, String vietnamese) {
        return languageManager.text(english, vietnamese);
    }

    public void toggleLanguage() {
        languageManager.toggle();
    }

    public DeathScreen getDeathScreen() {
        return deathScreen;
    }

    public WinScreen getWinScreen() {
        return winScreen;
    }

    public LeaderboardScreen getLeaderboardScreen() {
        return leaderboardScreen;
    }

    public DifficultyScreen getDifficultyScreen() {
        return difficultyScreen;
    }

    public Context getContext() {
        return context;
    }

    // Clean up MediaPlayer when game is destroyed
    public void cleanup() {
        if (destroyed) return;

        destroyed = true;
        gameLoop.stopGameLoop();
        clearPendingTouchEvents();
        playing.dispose();
        tutorialPlaying.dispose();
        openingScreen.dispose();
        trueEndingScreen.dispose();

        if (backgroundMusic != null) {
            try {
                backgroundMusic.release();
            } catch (Exception e) {
                System.err.println("Error releasing MediaPlayer: " + e.getMessage());
            }
            backgroundMusic = null;
        }
    }

    // Surface management methods
    public void setSurfaceReady(boolean ready) {
        synchronized (renderLock) {
            this.surfaceReady = ready;
        }

        // Do not join the game thread while holding renderLock.
        if (ready) {
            startGameLoop();
        } else {
            gameLoop.stopGameLoop();
            clearPendingTouchEvents();
            playing.resetInput();
            tutorialPlaying.resetInput();
            pauseBackgroundMusic();
        }
    }

    public void pauseGame() {
        if (!paused) {
            lifecyclePauseStartedAt = SystemClock.elapsedRealtime();
        }
        paused = true;
        gameLoop.stopGameLoop();
        clearPendingTouchEvents();
        playing.resetInput();
        tutorialPlaying.resetInput();

        // Android navigation (Back/Home/Recents) always leaves an active run
        // on the same explicit pause screen used by the in-game Home button.
        if (currentGameState == GameState.PLAYING) {
            playing.openPauseMenu();
        } else if (currentGameState == GameState.HOW_TO_PLAY) {
            tutorialPlaying.openPauseMenu();
        }
        pauseBackgroundMusic();
    }

    public void resumeGame() {
        if (destroyed) return;
        long pausedDuration = 0L;
        if (paused && lifecyclePauseStartedAt > 0L) {
            pausedDuration = Math.max(
                    0L, SystemClock.elapsedRealtime() - lifecyclePauseStartedAt
            );
        }

        if (pausedDuration >= BACKGROUND_RESET_TIMEOUT_MS) {
            // A long background stay starts a clean session instead of
            // resuming stale combat/projectile state several minutes later.
            clearPendingTouchEvents();
            playing.resetGame();
            tutorialPlaying.resetTutorial();
            currentGameState = GameState.MENU;
        } else if (pausedDuration > 0L) {
            playing.onExternalPauseFinished(pausedDuration);
            tutorialPlaying.onExternalPauseFinished(pausedDuration);
        }
        lifecyclePauseStartedAt = 0L;
        paused = false;
        startGameLoop();
        if (isInteractivePauseOpen()) {
            // Draw the pause overlay once, then GameLoop parks again until a
            // pause-menu button supplies another input-driven frame.
            gameLoop.requestPausedFrame();
        }
    }

    private void playBackgroundMusic() {
        if (backgroundMusic == null || destroyed) return;

        try {
            if (!backgroundMusic.isPlaying()) {
                backgroundMusic.start();
            }
        } catch (IllegalStateException e) {
            System.err.println("Error starting background music: " + e.getMessage());
        }
    }

    private void pauseBackgroundMusic() {
        if (backgroundMusic == null) return;

        try {
            if (backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
        } catch (IllegalStateException e) {
            System.err.println("Error pausing background music: " + e.getMessage());
        }
    }

    private void clearPendingTouchEvents() {
        MotionEvent event;
        while ((event = pendingTouchEvents.poll()) != null) {
            event.recycle();
        }
    }
}
