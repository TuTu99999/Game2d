package com.tutorial.androidgametutorial.gamestates;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.helpers.LeaderboardManager;
import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.ui.CustomButton;

import java.util.List;

import static com.tutorial.androidgametutorial.main.MainActivity.GAME_HEIGHT;
import static com.tutorial.androidgametutorial.main.MainActivity.GAME_WIDTH;

public class LeaderboardScreen extends BaseState implements GameStateInterface {

    private CustomButton backButton, clearButton;
    private Paint titlePaint, headerPaint, entryPaint, buttonPaint, rankPaint;
    private Paint emptyPaint, linePaint, columnPaint, borderPaint, buttonTextPaint, panelPaint;
    private LeaderboardManager leaderboardManager;
    private List<LeaderboardManager.LeaderboardEntry> topScores;

    public LeaderboardScreen(Game game) {
        super(game);
        initButtons();
        initPaints();

        leaderboardManager = new LeaderboardManager(game.getContext());
        updateLeaderboard();
    }

    private void initButtons() {
        float buttonWidth = Math.min(220, GAME_WIDTH * 0.25f);
        float buttonHeight = Math.min(70, GAME_HEIGHT * 0.08f);
        float centerX = GAME_WIDTH / 2f;
        float bottomY = GAME_HEIGHT * 0.86f;

        backButton = new CustomButton(centerX - buttonWidth - 20, bottomY, buttonWidth, buttonHeight);
        clearButton = new CustomButton(centerX + 20, bottomY, buttonWidth, buttonHeight);
    }

    private void initPaints() {
        float textScale = GAME_HEIGHT / 1080f;

        titlePaint = new Paint();
        titlePaint.setColor(Color.rgb(255, 215, 64));
        titlePaint.setTextSize(Math.max(36, 64 * textScale));
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        headerPaint = new Paint();
        headerPaint.setColor(Color.CYAN);
        headerPaint.setTextSize(Math.max(22, 34 * textScale));
        headerPaint.setFakeBoldText(true);
        headerPaint.setTextAlign(Paint.Align.CENTER);

        entryPaint = new Paint();
        entryPaint.setColor(Color.WHITE);
        entryPaint.setTextSize(Math.max(18, 30 * textScale));
        entryPaint.setTextAlign(Paint.Align.CENTER);

        rankPaint = new Paint();
        rankPaint.setColor(Color.YELLOW);
        rankPaint.setTextSize(Math.max(20, 34 * textScale));
        rankPaint.setFakeBoldText(true);
        rankPaint.setTextAlign(Paint.Align.CENTER);

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.BLUE);
        buttonPaint.setStyle(Paint.Style.FILL);

        emptyPaint = new Paint();
        emptyPaint.setColor(Color.GRAY);
        emptyPaint.setTextSize(Math.max(22, 34 * textScale));
        emptyPaint.setTextAlign(Paint.Align.CENTER);

        linePaint = new Paint();
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(2);

        columnPaint = new Paint();
        columnPaint.setColor(Color.CYAN);
        columnPaint.setTextSize(Math.max(18, 28 * textScale));
        columnPaint.setFakeBoldText(true);
        columnPaint.setTextAlign(Paint.Align.CENTER);

        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);

        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(Math.max(18, 26 * textScale));
        buttonTextPaint.setFakeBoldText(true);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);

        panelPaint = new Paint();
        panelPaint.setColor(Color.rgb(22, 31, 50));
        panelPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void update(double delta) {
        // Leaderboard screen doesn't need update logic
    }

    @Override
    public void render(Canvas c) {
        c.drawColor(Color.rgb(8, 12, 24));

        c.drawText(game.text("LEADERBOARD", "BẢNG XẾP HẠNG"),
                GAME_WIDTH / 2f, GAME_HEIGHT * 0.12f, titlePaint);
        c.drawText(game.text("TOP 6 SCORES", "6 THÀNH TÍCH CAO NHẤT"),
                GAME_WIDTH / 2f, GAME_HEIGHT * 0.20f, headerPaint);

        float panelLeft = GAME_WIDTH * 0.08f;
        float panelRight = GAME_WIDTH * 0.92f;
        float panelTop = GAME_HEIGHT * 0.24f;
        float panelBottom = GAME_HEIGHT * 0.79f;
        c.drawRoundRect(panelLeft, panelTop, panelRight, panelBottom, 24, 24, panelPaint);

        float rankX = GAME_WIDTH * 0.18f;
        float scoreX = GAME_WIDTH * 0.45f;
        float dateX = GAME_WIDTH * 0.75f;
        float headerY = GAME_HEIGHT * 0.29f;
        float startY = GAME_HEIGHT * 0.36f;
        float lineHeight = GAME_HEIGHT * 0.07f;

        c.drawText(game.text("RANK", "HẠNG"), rankX, headerY, columnPaint);
        c.drawText(game.text("SCORE", "ĐIỂM"), scoreX, headerY, columnPaint);
        c.drawText(game.text("DATE", "NGÀY"), dateX, headerY, columnPaint);

        if (topScores.isEmpty()) {
            c.drawText(game.text("NO SCORES YET", "CHƯA CÓ THÀNH TÍCH"),
                    GAME_WIDTH / 2f, GAME_HEIGHT * 0.50f, emptyPaint);
        } else {
            for (int i = 0; i < topScores.size(); i++) {
                LeaderboardManager.LeaderboardEntry entry = topScores.get(i);
                float y = startY + i * lineHeight;

                Paint currentRankPaint = getRankPaint(entry.rank);
                String rankText = getRankDisplay(entry.rank);
                c.drawText(rankText, rankX, y, currentRankPaint);

                String scoreText = entry.score + game.text(" KILLS", " QUÁI");
                c.drawText(scoreText, scoreX, y, entryPaint);

                c.drawText(entry.date, dateX, y, entryPaint);

                if (i < topScores.size() - 1) {
                    c.drawLine(panelLeft + 30, y + lineHeight * 0.35f,
                            panelRight - 30, y + lineHeight * 0.35f, linePaint);
                }
            }
        }

        drawButton(c, backButton, game.text("BACK", "QUAY LẠI"), Color.rgb(31, 78, 121));
        drawButton(c, clearButton, game.text("CLEAR", "XÓA"), Color.rgb(150, 45, 45));
    }

    private Paint getRankPaint(int rank) {
        switch (rank) {
            case 1:
                rankPaint.setColor(Color.rgb(255, 215, 0)); // Gold
                break;
            case 2:
                rankPaint.setColor(Color.rgb(192, 192, 192)); // Silver
                break;
            case 3:
                rankPaint.setColor(Color.rgb(205, 127, 50)); // Bronze
                break;
            default:
                rankPaint.setColor(Color.WHITE);
                break;
        }
        return rankPaint;
    }

    private String getRankDisplay(int rank) {
        return "#" + rank;
    }

    private void drawButton(Canvas c, CustomButton button, String text, int color) {
        RectF buttonRect = button.getHitbox();

        // Draw button background
        buttonPaint.setColor(color);
        c.drawRoundRect(buttonRect, 18, 18, buttonPaint);

        // Draw button border
        c.drawRoundRect(buttonRect, 18, 18, borderPaint);

        // Draw button text
        float textX = buttonRect.centerX();
        float textY = buttonRect.centerY() - (buttonTextPaint.ascent() + buttonTextPaint.descent()) / 2;
        c.drawText(text, textX, textY, buttonTextPaint);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (backButton.getHitbox().contains(event.getX(), event.getY())) {
                // Go back to main menu
                game.setCurrentGameState(Game.GameState.MENU);
            } else if (clearButton.getHitbox().contains(event.getX(), event.getY())) {
                // Clear leaderboard
                leaderboardManager.clearLeaderboard();
                updateLeaderboard();
            }
        }
    }

    public void updateLeaderboard() {
        topScores = leaderboardManager.getTop6Scores();
    }
}
