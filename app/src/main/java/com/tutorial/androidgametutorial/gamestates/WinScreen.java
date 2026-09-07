package com.tutorial.androidgametutorial.gamestates;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.helpers.LeaderboardManager;
import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.ui.CustomButton;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

public class WinScreen extends BaseState implements GameStateInterface {

    private CustomButton playAgainButton, menuButton, leaderboardButton;
    private Paint titlePaint, statsPaint, mapPaint, buttonPaint;
    private Paint newRecordPaint, recordPaint, borderPaint, buttonTextPaint;
    private int killCount = 0;
    private int bestKillCount = 0;
    private SharedPreferences sharedPrefs;
    private LeaderboardManager leaderboardManager;
    private boolean isNewRecord = false;

    public WinScreen(Game game) {
        super(game);
        initButtons();
        initPaints();

        // Khởi tạo SharedPreferences để lưu kỷ lục (tương thích ngược)
        sharedPrefs = game.getContext().getSharedPreferences("GameStats", Context.MODE_PRIVATE);
        bestKillCount = sharedPrefs.getInt("bestKillCount", 0);

        // Khởi tạo LeaderboardManager
        leaderboardManager = new LeaderboardManager(game.getContext());
    }

    private void initButtons() {
        float buttonWidth = Math.min(320, GAME_WIDTH * 0.32f);
        float buttonHeight = Math.min(76, GAME_HEIGHT * 0.08f);
        float centerX = GAME_WIDTH / 2f;
        float startY = GAME_HEIGHT * 0.62f;
        float buttonGap = buttonHeight + GAME_HEIGHT * 0.02f;

        playAgainButton = new CustomButton(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight);
        menuButton = new CustomButton(centerX - buttonWidth / 2, startY + buttonGap, buttonWidth, buttonHeight);
        leaderboardButton = new CustomButton(centerX - buttonWidth / 2, startY + buttonGap * 2, buttonWidth, buttonHeight);
    }

    private void initPaints() {
        float textScale = GAME_HEIGHT / 1080f;

        titlePaint = new Paint();
        titlePaint.setColor(Color.rgb(255, 215, 64));
        titlePaint.setTextSize(Math.max(36, 64 * textScale));
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        statsPaint = new Paint();
        statsPaint.setColor(Color.WHITE);
        statsPaint.setTextSize(Math.max(18, 30 * textScale));
        statsPaint.setTextAlign(Paint.Align.CENTER);

        mapPaint = new Paint(statsPaint);
        mapPaint.setTextAlign(Paint.Align.LEFT);

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.GREEN);
        buttonPaint.setStyle(Paint.Style.FILL);

        newRecordPaint = new Paint();
        newRecordPaint.setColor(Color.rgb(255, 215, 0));
        newRecordPaint.setTextSize(Math.max(24, 40 * textScale));
        newRecordPaint.setFakeBoldText(true);
        newRecordPaint.setTextAlign(Paint.Align.CENTER);

        recordPaint = new Paint();
        recordPaint.setColor(Color.rgb(98, 214, 255));
        recordPaint.setTextSize(Math.max(20, 32 * textScale));
        recordPaint.setFakeBoldText(true);
        recordPaint.setTextAlign(Paint.Align.CENTER);

        borderPaint = new Paint();
        borderPaint.setColor(Color.rgb(255, 215, 64));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);

        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(Math.max(18, 28 * textScale));
        buttonTextPaint.setFakeBoldText(true);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    public void update(double delta) {
        // Victory screen doesn't need update logic
    }

    @Override
    public void render(Canvas c) {
        c.drawColor(Color.rgb(8, 12, 24));

        float centerX = GAME_WIDTH / 2f;

        if (isNewRecord) {
            c.drawText(game.text("NEW RECORD!", "KỶ LỤC MỚI!"),
                    centerX, GAME_HEIGHT * 0.07f, newRecordPaint);
        }

        c.drawText(game.text("VICTORY", "CHIẾN THẮNG"),
                centerX, GAME_HEIGHT * 0.16f, titlePaint);
        boolean hardMode = game.getCurrentDifficulty() == Game.Difficulty.HARD;
        c.drawText(
                hardMode
                        ? game.text("You conquered all four maps!", "Bạn đã chinh phục cả bốn màn!")
                        : game.text("You survived all three maps!", "Bạn đã sống sót qua cả ba màn!"),
                centerX,
                GAME_HEIGHT * 0.23f,
                statsPaint
        );

        String map1Line = game.text(
                "MAP 1  -  OUTDOOR  -  COMPLETE",
                "MÀN 1  -  ĐỒNG CỎ  -  HOÀN THÀNH");
        String map2Line = game.text(
                "MAP 2  -  SNOW  -  COMPLETE",
                "MÀN 2  -  TUYẾT  -  HOÀN THÀNH");
        String map3Line = game.text(
                "MAP 3  -  DESERT  -  COMPLETE",
                "MÀN 3  -  SA MẠC  -  HOÀN THÀNH");
        String map4Line = game.text(
                "MAP 4  -  SHADOW REALM  -  COMPLETE",
                "MÀN 4  -  CÕI BÓNG TỐI  -  HOÀN THÀNH");
        String longestMapLine = hardMode ? map4Line : map3Line;
        float mapLeft = centerX - mapPaint.measureText(longestMapLine) / 2f;
        c.drawText(map1Line, mapLeft, GAME_HEIGHT * 0.32f, mapPaint);
        c.drawText(map2Line, mapLeft, GAME_HEIGHT * 0.365f, mapPaint);
        c.drawText(map3Line, mapLeft, GAME_HEIGHT * 0.41f, mapPaint);
        if (hardMode) {
            c.drawText(map4Line,
                    mapLeft, GAME_HEIGHT * 0.445f, mapPaint);
        }
        c.drawText(game.text("TOTAL ENEMIES DEFEATED: ", "TỔNG QUÁI ĐÃ HẠ: ")
                        + killCount,
                centerX, GAME_HEIGHT * 0.475f, recordPaint);

        int bestScore = leaderboardManager.getBestScore();
        c.drawText(game.text("PERSONAL BEST: ", "KỶ LỤC CÁ NHÂN: ")
                        + bestScore,
                centerX, GAME_HEIGHT * 0.525f, statsPaint);

        drawButton(c, playAgainButton, game.text("PLAY AGAIN", "CHƠI LẠI"));
        drawButton(c, menuButton, game.text("MAIN MENU", "MENU CHÍNH"));
        drawButton(c, leaderboardButton, game.text("LEADERBOARD", "BẢNG XẾP HẠNG"));
    }

    private void drawButton(Canvas c, CustomButton button, String text) {
        RectF buttonRect = button.getHitbox();

        buttonPaint.setColor(Color.rgb(31, 78, 121));
        c.drawRect(buttonRect, buttonPaint);

        // Draw button border
        c.drawRect(buttonRect, borderPaint);

        // Draw button text
        float textX = buttonRect.centerX();
        float textY = buttonRect.centerY() - (buttonTextPaint.ascent() + buttonTextPaint.descent()) / 2;
        c.drawText(text, textX, textY, buttonTextPaint);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (playAgainButton.getHitbox().contains(event.getX(), event.getY())) {
                // Reset game and start playing again
                game.getPlaying().resetGame();
                game.setCurrentGameState(Game.GameState.PLAYING);
            } else if (menuButton.getHitbox().contains(event.getX(), event.getY())) {
                // Go back to main menu
                game.setCurrentGameState(Game.GameState.MENU);
            } else if (leaderboardButton.getHitbox().contains(event.getX(), event.getY())) {
                // Go to leaderboard screen
                game.getLeaderboardScreen().updateLeaderboard();
                game.setCurrentGameState(Game.GameState.LEADERBOARD);
            }
        }
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;

        // Debug để kiểm tra giá trị
        System.out.println("🏆 WinScreen: Nhận killCount = " + killCount);

        // Kiểm tra xem có phải kỷ lục mới không
        isNewRecord = leaderboardManager.isNewRecord(killCount);

        // Thêm điểm số mới vào leaderboard
        leaderboardManager.addScore(killCount);

        // Cập nhật kỷ lục cao nhất nếu cần (tương thích ngược)
        if (killCount > bestKillCount) {
            bestKillCount = killCount;
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putInt("bestKillCount", bestKillCount);
            editor.apply();
            System.out.println("🥇 KỶ LỤC MỚI! " + bestKillCount + " quái bị tiêu diệt!");
        }

        System.out.println("🏆 WinScreen: Đã lưu killCount = " + this.killCount + ", isNewRecord = " + isNewRecord);
    }

    public int getKillCount() {
        return killCount;
    }

    public int getBestKillCount() {
        return leaderboardManager.getBestScore();
    }
}
