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

public class DeathScreen extends BaseState implements GameStateInterface {

    private CustomButton btnReplay, btnMainMenu, btnEscape;
    private final Paint titlePaint = new Paint();
    private final Paint buttonPaint = new Paint();
    private final Paint buttonBorderPaint = new Paint();
    private final Paint buttonTextPaint = new Paint();

    private final int menuWidth = GameImages.MAINMENU_MENUBG.getImage().getWidth();
    private final int menuHeight = GameImages.MAINMENU_MENUBG.getImage().getHeight() + 110;
    private final int menuX = (MainActivity.GAME_WIDTH - menuWidth) / 2;
    private final int menuY = (MainActivity.GAME_HEIGHT - menuHeight) / 2;

    private final int buttonWidth = 300;
    private final int buttonHeight = 100;
    private int buttonsX = menuX + menuWidth / 2 - buttonWidth / 2;
    private int btnReplayY = menuY + 210, btnMainMenuY = btnReplayY + 145, btnEscapeY = btnMainMenuY + 145;


    public DeathScreen(Game game) {
        super(game);
        btnReplay = new CustomButton(buttonsX, btnReplayY, buttonWidth, buttonHeight);
        btnMainMenu = new CustomButton(buttonsX, btnMainMenuY, buttonWidth, buttonHeight);
        btnEscape = new CustomButton(buttonsX, btnEscapeY, buttonWidth, buttonHeight);

        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(40f);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setAntiAlias(true);

        buttonPaint.setAntiAlias(true);
        buttonBorderPaint.setColor(Color.rgb(255, 205, 75));
        buttonBorderPaint.setStyle(Paint.Style.STROKE);
        buttonBorderPaint.setStrokeWidth(4f);
        buttonBorderPaint.setAntiAlias(true);
        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(28f);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        buttonTextPaint.setAntiAlias(true);
    }


    @Override
    public void render(Canvas c) {
        drawBackground(c);
        drawButtons(c);
    }

    private void drawButtons(Canvas c) {
        drawButton(c, btnReplay, game.text("REPLAY", "CHƠI LẠI"));
        drawButton(c, btnMainMenu, game.text("MAIN MENU", "MENU CHÍNH"));
        drawButton(c, btnEscape, game.text("EXIT", "THOÁT"));
    }

    private void drawButton(Canvas canvas, CustomButton button, String label) {
        RectF box = button.getHitbox();
        buttonPaint.setColor(button.isPushed()
                ? Color.rgb(74, 92, 142) : Color.rgb(38, 53, 94));
        canvas.drawRoundRect(box, 12f, 12f, buttonPaint);
        canvas.drawRoundRect(box, 12f, 12f, buttonBorderPaint);
        float y = box.centerY()
                - (buttonTextPaint.ascent() + buttonTextPaint.descent()) / 2f;
        canvas.drawText(label, box.centerX(), y, buttonTextPaint);
    }

    private void drawBackground(Canvas c) {
        c.drawBitmap(
                GameImages.MAINMENU_MENUBG.getImage(),
                null,
                new android.graphics.Rect(menuX, menuY, menuX + menuWidth, menuY + menuHeight),
                null
        );
        c.drawText(game.text("YOU DIED", "BẠN ĐÃ THẤT BẠI"),
                menuX + menuWidth / 2f, menuY + 125f, titlePaint);
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isIn(event, btnReplay))
                btnReplay.setPushed(true);
            else if (isIn(event, btnMainMenu))
                btnMainMenu.setPushed(true);
            else if (isIn(event, btnEscape))
                btnEscape.setPushed(true);

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isIn(event, btnReplay)) {
                if (btnReplay.isPushed()) {
                    // Reset game về trạng thái ban đầu trước khi chuyển state
                    game.getPlaying().resetGame();
                    game.setCurrentGameState(Game.GameState.PLAYING);
                }
            } else if (isIn(event, btnMainMenu)) {
                if (btnMainMenu.isPushed())
                    game.setCurrentGameState(Game.GameState.MENU);
            } else if (isIn(event, btnEscape)) {
                if (btnEscape.isPushed())
                    System.exit(0); // Thoát game
            }

            btnReplay.setPushed(false);
            btnMainMenu.setPushed(false);
            btnEscape.setPushed(false);
        }

    }


    @Override
    public void update(double delta) {
    }

}
