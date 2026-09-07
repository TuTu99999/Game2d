package com.tutorial.androidgametutorial.gamestates;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.ui.CustomButton;
import com.tutorial.androidgametutorial.ui.GameImages;

public class Menu extends BaseState implements GameStateInterface {

    private final CustomButton btnPlay;
    private final CustomButton btnDifficulty;
    private final CustomButton btnLeaderboard;
    private final CustomButton btnCharacter;
    private final CustomButton btnLoadout;
    private final CustomButton btnHowToPlay;
    private final CustomButton btnSettings;
    private final CustomButton btnExit;
    private final CustomButton settingsMusic;
    private final CustomButton settingsSound;
    private final CustomButton settingsLanguage;
    private final CustomButton settingsBack;
    private boolean settingsOpen;

    private final Bitmap background;
    private final Rect backgroundSource;
    private final Rect backgroundDestination;

    private final Paint darkOverlayPaint = new Paint();
    private final Paint titlePaint = new Paint();
    private final Paint subtitlePaint = new Paint();
    private final Paint buttonPaint = new Paint();
    private final Paint buttonBorderPaint = new Paint();
    private final Paint buttonTextPaint = new Paint();
    private final Paint footerPaint = new Paint();
    private final Paint languageTrackPaint = new Paint();
    private final Paint languageThumbPaint = new Paint();

    private final float uiScale;

    public Menu(Game game) {
        super(game);

        uiScale = Math.min(
                MainActivity.GAME_WIDTH / 1920f,
                MainActivity.GAME_HEIGHT / 1080f
        );

        background = GameImages.HOME_BACKGROUND.getImage();
        backgroundSource = new Rect(0, 0, background.getWidth(), background.getHeight());
        backgroundDestination = new Rect(0, 0, MainActivity.GAME_WIDTH, MainActivity.GAME_HEIGHT);

        float centerX = MainActivity.GAME_WIDTH / 2f;
        float bigWidth = 380 * uiScale;
        float smallWidth = 300 * uiScale;
        float bigHeight = 78 * uiScale;
        float smallHeight = 68 * uiScale;
        float gap = 24 * uiScale;

        btnPlay = new CustomButton(
                centerX - bigWidth / 2f,
                MainActivity.GAME_HEIGHT * 0.29f,
                bigWidth,
                bigHeight
        );

        float leftColumnX = centerX - smallWidth - gap / 2f;
        float rightColumnX = centerX + gap / 2f;

        btnDifficulty = new CustomButton(
                leftColumnX,
                MainActivity.GAME_HEIGHT * 0.41f,
                smallWidth,
                smallHeight
        );
        btnLeaderboard = new CustomButton(
                rightColumnX,
                MainActivity.GAME_HEIGHT * 0.41f,
                smallWidth,
                smallHeight
        );
        btnCharacter = new CustomButton(
                leftColumnX,
                MainActivity.GAME_HEIGHT * 0.51f,
                smallWidth,
                smallHeight
        );
        btnLoadout = new CustomButton(
                rightColumnX,
                MainActivity.GAME_HEIGHT * 0.51f,
                smallWidth,
                smallHeight
        );
        btnHowToPlay = new CustomButton(
                rightColumnX,
                MainActivity.GAME_HEIGHT * 0.63f,
                smallWidth,
                smallHeight
        );
        btnSettings = new CustomButton(
                leftColumnX,
                MainActivity.GAME_HEIGHT * 0.63f,
                smallWidth,
                smallHeight
        );
        btnExit = new CustomButton(
                centerX - smallWidth / 2f,
                MainActivity.GAME_HEIGHT * 0.76f,
                smallWidth,
                smallHeight
        );

        settingsMusic = new CustomButton(
                centerX - bigWidth / 2f,
                MainActivity.GAME_HEIGHT * 0.34f,
                bigWidth,
                bigHeight
        );
        settingsSound = new CustomButton(
                centerX - bigWidth / 2f,
                MainActivity.GAME_HEIGHT * 0.46f,
                bigWidth,
                bigHeight
        );
        settingsLanguage = new CustomButton(
                centerX - bigWidth / 2f,
                MainActivity.GAME_HEIGHT * 0.58f,
                bigWidth,
                bigHeight
        );
        settingsBack = new CustomButton(
                centerX - smallWidth / 2f,
                MainActivity.GAME_HEIGHT * 0.72f,
                smallWidth,
                smallHeight
        );

        darkOverlayPaint.setColor(Color.argb(75, 0, 0, 18));

        titlePaint.setColor(Color.rgb(255, 205, 75));
        titlePaint.setTextSize(64 * uiScale);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setAntiAlias(true);

        subtitlePaint.setColor(Color.WHITE);
        subtitlePaint.setTextSize(24 * uiScale);
        subtitlePaint.setTextAlign(Paint.Align.CENTER);
        subtitlePaint.setAntiAlias(true);

        buttonPaint.setStyle(Paint.Style.FILL);
        buttonPaint.setAntiAlias(true);

        buttonBorderPaint.setColor(Color.rgb(255, 190, 65));
        buttonBorderPaint.setStyle(Paint.Style.STROKE);
        buttonBorderPaint.setStrokeWidth(3 * uiScale);
        buttonBorderPaint.setAntiAlias(true);

        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(28 * uiScale);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        buttonTextPaint.setAntiAlias(true);

        footerPaint.setColor(Color.LTGRAY);
        footerPaint.setTextSize(18 * uiScale);
        footerPaint.setTextAlign(Paint.Align.CENTER);
        footerPaint.setAntiAlias(true);

        languageTrackPaint.setAntiAlias(true);
        languageThumbPaint.setColor(Color.WHITE);
        languageThumbPaint.setAntiAlias(true);
    }

    @Override
    public void update(double delta) {
        // Home screen has no update logic.
    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawBitmap(background, backgroundSource, backgroundDestination, null);
        canvas.drawRect(backgroundDestination, darkOverlayPaint);

        float centerX = MainActivity.GAME_WIDTH / 2f;
        if (settingsOpen) {
            renderSettings(canvas, centerX);
            return;
        }

        canvas.drawText(
                game.text(Game.GAME_TITLE, "HÀNH TRÌNH DIỆT QUÁI"),
                centerX,
                MainActivity.GAME_HEIGHT * 0.16f,
                titlePaint
        );
        canvas.drawText(
                game.text("CURRENT DIFFICULTY: ", "ĐỘ KHÓ HIỆN TẠI: ")
                        + game.getLanguage().difficulty(game.getCurrentDifficulty()),
                centerX,
                MainActivity.GAME_HEIGHT * 0.22f,
                subtitlePaint
        );

        drawButton(canvas, btnPlay, game.text("PLAY", "CHƠI"));
        drawButton(canvas, btnDifficulty, game.text("DIFFICULTY", "ĐỘ KHÓ"));
        drawButton(canvas, btnLeaderboard, game.text("LEADERBOARD", "BẢNG XẾP HẠNG"));
        drawButton(canvas, btnCharacter, game.text("CHARACTER", "NHÂN VẬT"));
        drawButton(canvas, btnLoadout, game.text("WEAPONS & SKILLS", "VŨ KHÍ & KỸ NĂNG"));
        drawButton(canvas, btnHowToPlay, game.text("HOW TO PLAY", "CÁCH CHƠI"));
        drawButton(canvas, btnSettings, game.text("SETTINGS", "CÀI ĐẶT"));
        drawButton(canvas, btnExit, game.text("EXIT", "THOÁT"));

        canvas.drawText(
                Game.STUDIO_NAME,
                centerX,
                MainActivity.GAME_HEIGHT * 0.94f,
                footerPaint
        );
    }

    private void renderSettings(Canvas canvas, float centerX) {
        canvas.drawText(
                game.text("SETTINGS", "CÀI ĐẶT"),
                centerX,
                MainActivity.GAME_HEIGHT * 0.17f,
                titlePaint
        );
        canvas.drawText(
                game.text("AUDIO & LANGUAGE", "ÂM THANH & NGÔN NGỮ"),
                centerX,
                MainActivity.GAME_HEIGHT * 0.25f,
                subtitlePaint
        );

        drawButton(canvas, settingsMusic,
                game.isMusicOn()
                        ? game.text("MUSIC: ON", "NHẠC: BẬT")
                        : game.text("MUSIC: OFF", "NHẠC: TẮT"));
        drawButton(canvas, settingsSound,
                game.isSoundOn()
                        ? game.text("SOUND: ON", "ÂM THANH: BẬT")
                        : game.text("SOUND: OFF", "ÂM THANH: TẮT"));
        drawLanguageToggle(canvas);
        drawButton(canvas, settingsBack, game.text("BACK TO HOME", "VỀ TRANG CHỦ"));

        canvas.drawText(
                Game.STUDIO_NAME,
                centerX,
                MainActivity.GAME_HEIGHT * 0.88f,
                footerPaint
        );
    }

    private void drawButton(Canvas canvas, CustomButton button, String text) {
        RectF hitbox = button.getHitbox();

        if (button.isPushed()) {
            buttonPaint.setColor(Color.argb(235, 85, 90, 140));
        } else {
            buttonPaint.setColor(Color.argb(220, 30, 38, 80));
        }

        float cornerRadius = 12 * uiScale;
        canvas.drawRoundRect(hitbox, cornerRadius, cornerRadius, buttonPaint);
        canvas.drawRoundRect(hitbox, cornerRadius, cornerRadius, buttonBorderPaint);

        float textY = hitbox.centerY() - (buttonTextPaint.ascent() + buttonTextPaint.descent()) / 2f;
        canvas.drawText(text, hitbox.centerX(), textY, buttonTextPaint);
    }

    private void drawLanguageToggle(Canvas canvas) {
        RectF hitbox = settingsLanguage.getHitbox();
        boolean vietnamese = game.getLanguage().isVietnamese();

        buttonPaint.setColor(settingsLanguage.isPushed()
                ? Color.argb(235, 85, 90, 140)
                : Color.argb(220, 30, 38, 80));
        float cornerRadius = 12 * uiScale;
        canvas.drawRoundRect(hitbox, cornerRadius, cornerRadius, buttonPaint);
        canvas.drawRoundRect(hitbox, cornerRadius, cornerRadius, buttonBorderPaint);

        float switchWidth = 82 * uiScale;
        float switchHeight = 40 * uiScale;
        float switchRight = hitbox.right - 22 * uiScale;
        RectF track = new RectF(
                switchRight - switchWidth,
                hitbox.centerY() - switchHeight / 2f,
                switchRight,
                hitbox.centerY() + switchHeight / 2f
        );
        languageTrackPaint.setColor(vietnamese
                ? Color.rgb(35, 190, 125)
                : Color.rgb(95, 105, 130));
        canvas.drawRoundRect(track, switchHeight / 2f, switchHeight / 2f,
                languageTrackPaint);

        float thumbRadius = 16 * uiScale;
        float thumbX = vietnamese
                ? track.right - switchHeight / 2f
                : track.left + switchHeight / 2f;
        canvas.drawCircle(thumbX, track.centerY(), thumbRadius, languageThumbPaint);

        float oldTextSize = buttonTextPaint.getTextSize();
        Paint.Align oldAlign = buttonTextPaint.getTextAlign();
        buttonTextPaint.setTextSize(24 * uiScale);
        buttonTextPaint.setTextAlign(Paint.Align.LEFT);
        float textY = hitbox.centerY()
                - (buttonTextPaint.ascent() + buttonTextPaint.descent()) / 2f;
        canvas.drawText(
                game.getLanguage().languageSettingLabel(),
                hitbox.left + 24 * uiScale,
                textY,
                buttonTextPaint
        );
        buttonTextPaint.setTextSize(oldTextSize);
        buttonTextPaint.setTextAlign(oldAlign);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            pressButtonAt(event.getX(), event.getY());
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            clickButtonAt(event.getX(), event.getY());
            releaseAllButtons();
        } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            releaseAllButtons();
        }
    }

    private void pressButtonAt(float x, float y) {
        if (settingsOpen) {
            pressSettingsButtonAt(x, y);
            return;
        }

        if (btnPlay.getHitbox().contains(x, y)) {
            btnPlay.setPushed(true);
        } else if (btnDifficulty.getHitbox().contains(x, y)) {
            btnDifficulty.setPushed(true);
        } else if (btnLeaderboard.getHitbox().contains(x, y)) {
            btnLeaderboard.setPushed(true);
        } else if (btnCharacter.getHitbox().contains(x, y)) {
            btnCharacter.setPushed(true);
        } else if (btnLoadout.getHitbox().contains(x, y)) {
            btnLoadout.setPushed(true);
        } else if (btnHowToPlay.getHitbox().contains(x, y)) {
            btnHowToPlay.setPushed(true);
        } else if (btnSettings.getHitbox().contains(x, y)) {
            btnSettings.setPushed(true);
        } else if (btnExit.getHitbox().contains(x, y)) {
            btnExit.setPushed(true);
        }
    }

    private void pressSettingsButtonAt(float x, float y) {
        if (settingsMusic.getHitbox().contains(x, y)) {
            settingsMusic.setPushed(true);
        } else if (settingsSound.getHitbox().contains(x, y)) {
            settingsSound.setPushed(true);
        } else if (settingsLanguage.getHitbox().contains(x, y)) {
            settingsLanguage.setPushed(true);
        } else if (settingsBack.getHitbox().contains(x, y)) {
            settingsBack.setPushed(true);
        }
    }

    private void clickButtonAt(float x, float y) {
        if (settingsOpen) {
            clickSettingsButtonAt(x, y);
            return;
        }

        if (btnPlay.isPushed() && btnPlay.getHitbox().contains(x, y)) {
            game.getPlaying().resetGame();
            game.setCurrentGameState(Game.GameState.PLAYING);
        } else if (btnDifficulty.isPushed() && btnDifficulty.getHitbox().contains(x, y)) {
            game.setCurrentGameState(Game.GameState.DIFFICULTY);
        } else if (btnLeaderboard.isPushed() && btnLeaderboard.getHitbox().contains(x, y)) {
            game.getLeaderboardScreen().updateLeaderboard();
            game.setCurrentGameState(Game.GameState.LEADERBOARD);
        } else if (btnCharacter.isPushed() && btnCharacter.getHitbox().contains(x, y)) {
            game.setCurrentGameState(Game.GameState.CHARACTER_SELECT);
        } else if (btnLoadout.isPushed() && btnLoadout.getHitbox().contains(x, y)) {
            game.setCurrentGameState(Game.GameState.LOADOUT);
        } else if (btnHowToPlay.isPushed() && btnHowToPlay.getHitbox().contains(x, y)) {
            game.startTutorial();
        } else if (btnSettings.isPushed() && btnSettings.getHitbox().contains(x, y)) {
            settingsOpen = true;
        } else if (btnExit.isPushed() && btnExit.getHitbox().contains(x, y)) {
            game.exitGame();
        }
    }

    private void clickSettingsButtonAt(float x, float y) {
        if (settingsMusic.isPushed() && settingsMusic.getHitbox().contains(x, y)) {
            game.toggleMusic();
        } else if (settingsSound.isPushed() && settingsSound.getHitbox().contains(x, y)) {
            game.toggleSound();
        } else if (settingsLanguage.isPushed()
                && settingsLanguage.getHitbox().contains(x, y)) {
            game.toggleLanguage();
        } else if (settingsBack.isPushed() && settingsBack.getHitbox().contains(x, y)) {
            settingsOpen = false;
        }
    }

    private void releaseAllButtons() {
        btnPlay.setPushed(false);
        btnDifficulty.setPushed(false);
        btnLeaderboard.setPushed(false);
        btnCharacter.setPushed(false);
        btnLoadout.setPushed(false);
        btnHowToPlay.setPushed(false);
        btnSettings.setPushed(false);
        btnExit.setPushed(false);
        settingsMusic.setPushed(false);
        settingsSound.setPushed(false);
        settingsLanguage.setPushed(false);
        settingsBack.setPushed(false);
    }
}
