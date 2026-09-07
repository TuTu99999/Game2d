package com.tutorial.androidgametutorial.gamestates;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.ui.CustomButton;
import com.tutorial.androidgametutorial.ui.GameImages;

public class DifficultyScreen extends BaseState implements GameStateInterface {

    private final CustomButton btnEasy;
    private final CustomButton btnHard;
    private final CustomButton btnBack;
    private final Paint titlePaint = new Paint();
    private final Paint buttonPaint = new Paint();
    private final Paint selectedPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint textPaint = new Paint();

    private final int menuX = MainActivity.GAME_WIDTH / 6;
    private final int menuY = 200;
    private final int menuWidth = GameImages.MAINMENU_MENUBG.getImage().getWidth();
    private final int buttonWidth = 300;
    private final int buttonHeight = 100;

    public DifficultyScreen(Game game) {
        super(game);
        int buttonX = menuX + menuWidth / 2 - buttonWidth / 2;
        int easyY = menuY + 145;
        int hardY = easyY + 130;
        int backY = hardY + 170;

        btnEasy = new CustomButton(buttonX, easyY, buttonWidth, buttonHeight);
        btnHard = new CustomButton(buttonX, hardY, buttonWidth, buttonHeight);
        btnBack = new CustomButton(buttonX, backY, buttonWidth, buttonHeight);

        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(42f);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setAntiAlias(true);

        buttonPaint.setColor(Color.rgb(38, 53, 94));
        selectedPaint.setColor(Color.rgb(66, 102, 92));
        borderPaint.setColor(Color.rgb(255, 205, 75));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        borderPaint.setAntiAlias(true);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawBitmap(GameImages.MAINMENU_MENUBG.getImage(), menuX, menuY, null);
        canvas.drawText(game.text("CHOOSE DIFFICULTY", "CHỌN ĐỘ KHÓ"),
                menuX + menuWidth / 2f, menuY + 85f, titlePaint);
        drawButton(canvas, btnEasy, game.text("EASY", "DỄ"),
                game.getCurrentDifficulty() == Game.Difficulty.EASY);
        drawButton(canvas, btnHard, game.text("HARD", "KHÓ"),
                game.getCurrentDifficulty() == Game.Difficulty.HARD);
        drawButton(canvas, btnBack, game.text("BACK", "QUAY LẠI"), false);
    }

    private void drawButton(Canvas canvas, CustomButton button,
                            String label, boolean selected) {
        RectF box = button.getHitbox();
        canvas.drawRoundRect(box, 12f, 12f,
                selected || button.isPushed() ? selectedPaint : buttonPaint);
        canvas.drawRoundRect(box, 12f, 12f, borderPaint);
        float y = box.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f;
        canvas.drawText(label, box.centerX(), y, textPaint);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (isIn(event, btnEasy)) btnEasy.setPushed(true);
            else if (isIn(event, btnHard)) btnHard.setPushed(true);
            else if (isIn(event, btnBack)) btnBack.setPushed(true);
            return;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) return;

        if (isIn(event, btnEasy) && btnEasy.isPushed()) {
            game.setDifficulty(Game.Difficulty.EASY);
            game.setCurrentGameState(Game.GameState.MENU);
        } else if (isIn(event, btnHard) && btnHard.isPushed()) {
            game.setDifficulty(Game.Difficulty.HARD);
            game.setCurrentGameState(Game.GameState.MENU);
        } else if (isIn(event, btnBack) && btnBack.isPushed()) {
            game.setCurrentGameState(Game.GameState.MENU);
        }

        btnEasy.setPushed(false);
        btnHard.setPushed(false);
        btnBack.setPushed(false);
    }
}
