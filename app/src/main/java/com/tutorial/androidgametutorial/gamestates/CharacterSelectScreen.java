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
import com.tutorial.androidgametutorial.main.LoadoutManager;
import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.ui.GameImages;
import com.tutorial.androidgametutorial.ui.LoadoutIcons;

public class CharacterSelectScreen extends BaseState implements GameStateInterface {

    private final Bitmap background;
    private final Rect backgroundSource;
    private final Rect backgroundDestination;
    private final RectF characterCard;
    private final RectF leftArrow;
    private final RectF rightArrow;
    private final RectF selectButton;
    private final RectF backButton;
    private final Paint overlayPaint = new Paint();
    private final Paint panelPaint = new Paint();
    private final Paint selectedPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint titlePaint = new Paint();
    private final Paint textPaint = new Paint();
    private final Paint smallTextPaint = new Paint();
    private final Paint iconPaint = new Paint();
    private final Paint dotPaint = new Paint();
    private final float uiScale;

    private int currentCharacterIndex;
    private float touchDownX;
    private float touchDownY;

    public CharacterSelectScreen(Game game) {
        super(game);
        uiScale = Math.min(MainActivity.GAME_WIDTH / 1920f, MainActivity.GAME_HEIGHT / 1080f);
        currentCharacterIndex = game.getLoadoutManager().getCharacter().ordinal();

        background = GameImages.HOME_BACKGROUND.getImage();
        backgroundSource = new Rect(0, 0, background.getWidth(), background.getHeight());
        backgroundDestination = new Rect(0, 0, MainActivity.GAME_WIDTH, MainActivity.GAME_HEIGHT);

        float centerX = MainActivity.GAME_WIDTH / 2f;
        float cardWidth = 620 * uiScale;
        float cardHeight = 590 * uiScale;
        float cardTop = MainActivity.GAME_HEIGHT * 0.20f;
        characterCard = new RectF(centerX - cardWidth / 2f, cardTop,
                centerX + cardWidth / 2f, cardTop + cardHeight);

        float arrowWidth = 130 * uiScale;
        float arrowHeight = 130 * uiScale;
        leftArrow = new RectF(
                characterCard.left - 190 * uiScale,
                characterCard.centerY() - arrowHeight / 2f,
                characterCard.left - 190 * uiScale + arrowWidth,
                characterCard.centerY() + arrowHeight / 2f
        );
        rightArrow = new RectF(
                characterCard.right + 60 * uiScale,
                characterCard.centerY() - arrowHeight / 2f,
                characterCard.right + 60 * uiScale + arrowWidth,
                characterCard.centerY() + arrowHeight / 2f
        );

        selectButton = new RectF(centerX - 190 * uiScale,
                MainActivity.GAME_HEIGHT - 125 * uiScale,
                centerX + 190 * uiScale,
                MainActivity.GAME_HEIGHT - 45 * uiScale);
        backButton = new RectF(70 * uiScale,
                MainActivity.GAME_HEIGHT - 125 * uiScale,
                310 * uiScale,
                MainActivity.GAME_HEIGHT - 45 * uiScale);

        overlayPaint.setColor(Color.argb(165, 3, 7, 20));
        panelPaint.setColor(Color.argb(230, 24, 31, 58));
        selectedPaint.setColor(Color.argb(235, 48, 69, 86));

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(5 * uiScale);
        borderPaint.setAntiAlias(true);

        titlePaint.setColor(Color.rgb(255, 205, 75));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(58 * uiScale);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setAntiAlias(true);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(31 * uiScale);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setAntiAlias(true);

        smallTextPaint.setColor(Color.LTGRAY);
        smallTextPaint.setTextAlign(Paint.Align.CENTER);
        smallTextPaint.setTextSize(23 * uiScale);
        smallTextPaint.setAntiAlias(true);

        iconPaint.setFilterBitmap(false);
        dotPaint.setAntiAlias(true);
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public void render(Canvas canvas) {
        canvas.drawBitmap(background, backgroundSource, backgroundDestination, null);
        canvas.drawRect(backgroundDestination, overlayPaint);
        canvas.drawText(game.text("CHOOSE YOUR HERO", "CHỌN NHÂN VẬT"), MainActivity.GAME_WIDTH / 2f,
                MainActivity.GAME_HEIGHT * 0.10f, titlePaint);
        canvas.drawText(game.text(
                        "Swipe or use the arrows, then tap SELECT",
                        "Vuốt hoặc dùng mũi tên, rồi nhấn CHỌN"),
                MainActivity.GAME_WIDTH / 2f, MainActivity.GAME_HEIGHT * 0.16f, smallTextPaint);

        LoadoutManager.CharacterType character = getCurrentCharacter();
        boolean isSelected = character == game.getLoadoutManager().getCharacter();
        drawCharacterCard(canvas, character, isSelected);
        drawArrow(canvas, leftArrow, "<");
        drawArrow(canvas, rightArrow, ">");
        drawPageDots(canvas);
        drawButton(canvas, backButton, game.text("BACK", "QUAY LẠI"), false);
        drawButton(canvas, selectButton,
                isSelected ? game.text("SELECTED", "ĐÃ CHỌN")
                        : game.text("SELECT", "CHỌN"), isSelected);
    }

    private void drawCharacterCard(Canvas canvas, LoadoutManager.CharacterType character,
                                   boolean isSelected) {
        float radius = 20 * uiScale;
        canvas.drawRoundRect(characterCard, radius, radius,
                isSelected ? selectedPaint : panelPaint);
        borderPaint.setColor(isSelected
                ? Color.rgb(255, 205, 75) : Color.rgb(120, 140, 185));
        canvas.drawRoundRect(characterCard, radius, radius, borderPaint);

        float iconSize = 285 * uiScale;
        RectF iconRect = new RectF(
                characterCard.centerX() - iconSize / 2f,
                characterCard.top + 25 * uiScale,
                characterCard.centerX() + iconSize / 2f,
                characterCard.top + 25 * uiScale + iconSize
        );
        if (character.getIconResourceId() != 0) {
            LoadoutIcons.drawResource(canvas, character.getIconResourceId(), iconRect, iconPaint);
        } else {
            LoadoutIcons.draw(canvas, character.getIconColumn(), character.getIconRow(),
                    iconRect, iconPaint);
        }

        float nameY = iconRect.bottom + 42 * uiScale;
        canvas.drawText(game.getLanguage().characterName(character).toUpperCase(
                        game.getLanguage().isVietnamese()
                                ? new java.util.Locale("vi", "VN") : java.util.Locale.US),
                characterCard.centerX(), nameY, textPaint);
        canvas.drawText(game.text("HEALTH: ", "MÁU: ") + character.getMaxHealth(), characterCard.centerX(),
                nameY + 55 * uiScale, smallTextPaint);
        canvas.drawText(game.text("SPEED: ", "TỐC ĐỘ: ")
                        + Math.round(character.getSpeedMultiplier() * 100) + "%",
                characterCard.centerX(), nameY + 96 * uiScale, smallTextPaint);
        canvas.drawText(game.text("DAMAGE: ", "SÁT THƯƠNG: ")
                        + Math.round(character.getDamageMultiplier() * 100) + "%",
                characterCard.centerX(), nameY + 137 * uiScale, smallTextPaint);
    }

    private void drawArrow(Canvas canvas, RectF arrow, String symbol) {
        canvas.drawRoundRect(arrow, 20 * uiScale, 20 * uiScale, panelPaint);
        borderPaint.setColor(Color.rgb(255, 205, 75));
        canvas.drawRoundRect(arrow, 20 * uiScale, 20 * uiScale, borderPaint);
        float oldSize = textPaint.getTextSize();
        textPaint.setTextSize(58 * uiScale);
        float y = arrow.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f;
        canvas.drawText(symbol, arrow.centerX(), y, textPaint);
        textPaint.setTextSize(oldSize);
    }

    private void drawPageDots(Canvas canvas) {
        int count = LoadoutManager.CharacterType.values().length;
        float gap = 34 * uiScale;
        float startX = MainActivity.GAME_WIDTH / 2f - gap * (count - 1) / 2f;
        float y = characterCard.bottom + 24 * uiScale;
        for (int i = 0; i < count; i++) {
            dotPaint.setColor(i == currentCharacterIndex
                    ? Color.rgb(255, 205, 75) : Color.rgb(110, 125, 155));
            canvas.drawCircle(startX + i * gap, y, 8 * uiScale, dotPaint);
        }
    }

    private void drawButton(Canvas canvas, RectF button, String label, boolean selected) {
        canvas.drawRoundRect(button, 12 * uiScale, 12 * uiScale,
                selected ? selectedPaint : panelPaint);
        borderPaint.setColor(Color.rgb(255, 205, 75));
        canvas.drawRoundRect(button, 12 * uiScale, 12 * uiScale, borderPaint);
        float y = button.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f;
        canvas.drawText(label, button.centerX(), y, textPaint);
    }

    private LoadoutManager.CharacterType getCurrentCharacter() {
        return LoadoutManager.CharacterType.values()[currentCharacterIndex];
    }

    private void showPreviousCharacter() {
        int count = LoadoutManager.CharacterType.values().length;
        currentCharacterIndex = (currentCharacterIndex - 1 + count) % count;
    }

    private void showNextCharacter() {
        int count = LoadoutManager.CharacterType.values().length;
        currentCharacterIndex = (currentCharacterIndex + 1) % count;
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            touchDownX = event.getX();
            touchDownY = event.getY();
            return;
        }
        if (event.getActionMasked() != MotionEvent.ACTION_UP) return;

        float x = event.getX();
        float y = event.getY();
        float swipeX = x - touchDownX;
        float swipeY = y - touchDownY;
        if (Math.abs(swipeX) > 80 * uiScale && Math.abs(swipeX) > Math.abs(swipeY)) {
            if (swipeX < 0) showNextCharacter();
            else showPreviousCharacter();
            return;
        }

        if (leftArrow.contains(x, y)) {
            showPreviousCharacter();
        } else if (rightArrow.contains(x, y)) {
            showNextCharacter();
        } else if (selectButton.contains(x, y)) {
            game.getLoadoutManager().setCharacter(getCurrentCharacter());
        } else if (backButton.contains(x, y)) {
            game.setCurrentGameState(Game.GameState.MENU);
        }
    }
}
