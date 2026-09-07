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

public class LoadoutScreen extends BaseState implements GameStateInterface {

    private final Bitmap background;
    private final Rect backgroundSource;
    private final Rect backgroundDestination;
    private final RectF[] weaponCards = new RectF[3];
    private final RectF skillCard;
    private final RectF skillLeftArrow;
    private final RectF skillRightArrow;
    private final RectF[] skillSlotButtons = new RectF[3];
    private final RectF backButton;
    private final Paint overlayPaint = new Paint();
    private final Paint panelPaint = new Paint();
    private final Paint selectedPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint titlePaint = new Paint();
    private final Paint sectionPaint = new Paint();
    private final Paint namePaint = new Paint();
    private final Paint infoPaint = new Paint();
    private final Paint iconPaint = new Paint();
    private final Paint dotPaint = new Paint();
    private final float uiScale;
    private int currentSkillIndex = 0;
    private float touchDownX;
    private float touchDownY;

    public LoadoutScreen(Game game) {
        super(game);
        uiScale = Math.min(MainActivity.GAME_WIDTH / 1920f, MainActivity.GAME_HEIGHT / 1080f);
        background = GameImages.HOME_BACKGROUND.getImage();
        backgroundSource = new Rect(0, 0, background.getWidth(), background.getHeight());
        backgroundDestination = new Rect(0, 0, MainActivity.GAME_WIDTH, MainActivity.GAME_HEIGHT);

        float weaponWidth = 390 * uiScale;
        float weaponGap = 55 * uiScale;
        float weaponTotal = weaponWidth * 3 + weaponGap * 2;
        float weaponStart = (MainActivity.GAME_WIDTH - weaponTotal) / 2f;
        float weaponTop = MainActivity.GAME_HEIGHT * 0.18f;
        for (int i = 0; i < weaponCards.length; i++) {
            float left = weaponStart + i * (weaponWidth + weaponGap);
            weaponCards[i] = new RectF(left, weaponTop, left + weaponWidth,
                    weaponTop + 245 * uiScale);
        }

        float centerX = MainActivity.GAME_WIDTH / 2f;
        float skillWidth = 650 * uiScale;
        float skillTop = MainActivity.GAME_HEIGHT * 0.55f;
        skillCard = new RectF(centerX - skillWidth / 2f, skillTop,
                centerX + skillWidth / 2f, skillTop + 265 * uiScale);

        float arrowSize = 120 * uiScale;
        skillLeftArrow = new RectF(skillCard.left - 170 * uiScale,
                skillCard.centerY() - arrowSize / 2f,
                skillCard.left - 50 * uiScale,
                skillCard.centerY() + arrowSize / 2f);
        skillRightArrow = new RectF(skillCard.right + 50 * uiScale,
                skillCard.centerY() - arrowSize / 2f,
                skillCard.right + 170 * uiScale,
                skillCard.centerY() + arrowSize / 2f);

        float slotWidth = 220 * uiScale;
        float slotGap = 25 * uiScale;
        float slotTotalWidth = slotWidth * 3 + slotGap * 2;
        float slotStartX = centerX - slotTotalWidth / 2f;
        float slotTop = MainActivity.GAME_HEIGHT - 180 * uiScale;
        for (int i = 0; i < skillSlotButtons.length; i++) {
            float left = slotStartX + i * (slotWidth + slotGap);
            skillSlotButtons[i] = new RectF(left, slotTop,
                    left + slotWidth, slotTop + 78 * uiScale);
        }

        backButton = new RectF(70 * uiScale,
                MainActivity.GAME_HEIGHT - 110 * uiScale,
                310 * uiScale, MainActivity.GAME_HEIGHT - 35 * uiScale);

        overlayPaint.setColor(Color.argb(170, 3, 7, 20));
        panelPaint.setColor(Color.argb(225, 24, 31, 58));
        selectedPaint.setColor(Color.argb(235, 46, 71, 76));

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4 * uiScale);
        borderPaint.setAntiAlias(true);

        titlePaint.setColor(Color.rgb(255, 205, 75));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(52 * uiScale);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setAntiAlias(true);

        sectionPaint.setColor(Color.WHITE);
        sectionPaint.setTextAlign(Paint.Align.CENTER);
        sectionPaint.setTextSize(28 * uiScale);
        sectionPaint.setTypeface(Typeface.DEFAULT_BOLD);
        sectionPaint.setAntiAlias(true);

        namePaint.setColor(Color.WHITE);
        namePaint.setTextAlign(Paint.Align.CENTER);
        namePaint.setTextSize(23 * uiScale);
        namePaint.setTypeface(Typeface.DEFAULT_BOLD);
        namePaint.setAntiAlias(true);

        infoPaint.setColor(Color.LTGRAY);
        infoPaint.setTextAlign(Paint.Align.CENTER);
        infoPaint.setTextSize(18 * uiScale);
        infoPaint.setAntiAlias(true);

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
        canvas.drawText(game.text("WEAPONS & SKILLS", "VŨ KHÍ & KỸ NĂNG"), MainActivity.GAME_WIDTH / 2f,
                MainActivity.GAME_HEIGHT * 0.085f, titlePaint);

        canvas.drawText(game.text("CHOOSE ONE WEAPON", "CHỌN MỘT VŨ KHÍ"), MainActivity.GAME_WIDTH / 2f,
                MainActivity.GAME_HEIGHT * 0.15f, sectionPaint);
        LoadoutManager.WeaponType selectedWeapon = game.getLoadoutManager().getWeapon();
        LoadoutManager.WeaponType[] weapons = LoadoutManager.WeaponType.values();
        for (int i = 0; i < weapons.length; i++) {
            drawWeaponCard(canvas, weaponCards[i], weapons[i], weapons[i] == selectedWeapon);
        }

        canvas.drawText(game.text("CHOOSE THREE SKILLS", "CHỌN BA KỸ NĂNG"), MainActivity.GAME_WIDTH / 2f,
                MainActivity.GAME_HEIGHT * 0.48f, sectionPaint);
        drawSelectedSkillSlots(canvas);

        LoadoutManager.SkillType skill = getCurrentSkill();
        int selectedSlot = game.getLoadoutManager().findSkillSlot(skill);
        drawSkillCard(canvas, skillCard, skill, selectedSlot);
        drawArrow(canvas, skillLeftArrow, "<");
        drawArrow(canvas, skillRightArrow, ">");
        drawSkillPageDots(canvas);
        drawSkillSlotButtons(canvas, selectedSlot);

        drawBackButton(canvas);
    }

    private void drawWeaponCard(Canvas canvas, RectF card,
                                LoadoutManager.WeaponType weapon, boolean selected) {
        drawCardBase(canvas, card, selected);
        float iconSize = 125 * uiScale;
        RectF iconRect = new RectF(card.centerX() - iconSize / 2f,
                card.top + 10 * uiScale, card.centerX() + iconSize / 2f,
                card.top + 10 * uiScale + iconSize);
        LoadoutIcons.draw(canvas, weapon.getIconColumn(), weapon.getIconRow(), iconRect, iconPaint);
        canvas.drawText(upper(game.getLanguage().weaponName(weapon)), card.centerX(),
                card.top + 165 * uiScale, namePaint);
        canvas.drawText(game.text("DAMAGE ", "SÁT THƯƠNG ")
                        + weapon.getDamage() + "  |  "
                        + formatSeconds(weapon.getCooldownMs(), 2),
                card.centerX(), card.top + 205 * uiScale, infoPaint);
    }

    private void drawSkillCard(Canvas canvas, RectF card,
                               LoadoutManager.SkillType skill, int selectedSlot) {
        drawCardBase(canvas, card, selectedSlot >= 0);
        float iconSize = 125 * uiScale;
        RectF iconRect = new RectF(card.centerX() - iconSize / 2f,
                card.top + 10 * uiScale, card.centerX() + iconSize / 2f,
                card.top + 10 * uiScale + iconSize);
        LoadoutIcons.drawResource(canvas, skill.getIconResourceId(), iconRect, iconPaint);
        canvas.drawText(upper(game.getLanguage().skillName(skill)), card.centerX(),
                card.top + 158 * uiScale, namePaint);
        canvas.drawText(game.getLanguage().skillDescription(skill), card.centerX(),
                card.top + 193 * uiScale, infoPaint);
        canvas.drawText(game.text("COOLDOWN ", "HỒI CHIÊU ")
                        + formatSeconds(skill.getCooldownMs(), 1),
                card.centerX(), card.top + 225 * uiScale, infoPaint);
        if (selectedSlot >= 0) {
            infoPaint.setColor(Color.rgb(255, 220, 95));
            canvas.drawText(game.text("SLOT ", "Ô ") + (selectedSlot + 1), card.centerX(),
                    card.bottom - 15 * uiScale, infoPaint);
            infoPaint.setColor(Color.LTGRAY);
        }
    }

    private void drawSelectedSkillSlots(Canvas canvas) {
        infoPaint.setColor(Color.rgb(255, 220, 95));
        canvas.drawText(game.text(
                        "CHOOSE A SKILL, THEN TAP SLOT 1, 2 OR 3",
                        "CHỌN KỸ NĂNG, SAU ĐÓ NHẤN Ô 1, 2 HOẶC 3"),
                MainActivity.GAME_WIDTH / 2f,
                MainActivity.GAME_HEIGHT * 0.525f, infoPaint);
        infoPaint.setColor(Color.LTGRAY);
    }

    private void drawArrow(Canvas canvas, RectF arrow, String symbol) {
        drawCardBase(canvas, arrow, false);
        float oldSize = sectionPaint.getTextSize();
        sectionPaint.setTextSize(50 * uiScale);
        float y = arrow.centerY() - (sectionPaint.ascent() + sectionPaint.descent()) / 2f;
        canvas.drawText(symbol, arrow.centerX(), y, sectionPaint);
        sectionPaint.setTextSize(oldSize);
    }

    private void drawSkillPageDots(Canvas canvas) {
        int count = LoadoutManager.SkillType.values().length;
        float gap = 32 * uiScale;
        float startX = MainActivity.GAME_WIDTH / 2f - gap * (count - 1) / 2f;
        float y = skillCard.bottom + 20 * uiScale;
        for (int i = 0; i < count; i++) {
            dotPaint.setColor(i == currentSkillIndex
                    ? Color.rgb(255, 205, 75) : Color.rgb(110, 125, 155));
            canvas.drawCircle(startX + i * gap, y, 7 * uiScale, dotPaint);
        }
    }

    private void drawSkillSlotButtons(Canvas canvas, int selectedSlot) {
        float oldSectionSize = sectionPaint.getTextSize();
        float oldInfoSize = infoPaint.getTextSize();
        sectionPaint.setTextSize(21 * uiScale);
        infoPaint.setTextSize(14 * uiScale);

        for (int slot = 0; slot < skillSlotButtons.length; slot++) {
            RectF button = skillSlotButtons[slot];
            boolean containsCurrentSkill = slot == selectedSlot;
            drawCardBase(canvas, button, containsCurrentSkill);
            canvas.drawText(game.text("SLOT ", "Ô ") + (slot + 1), button.centerX(),
                    button.top + 29 * uiScale, sectionPaint);

            infoPaint.setColor(containsCurrentSkill
                    ? Color.rgb(255, 220, 95) : Color.LTGRAY);
            canvas.drawText(game.getLanguage().skillName(
                            game.getLoadoutManager().getSkill(slot)),
                    button.centerX(), button.top + 59 * uiScale, infoPaint);
        }

        sectionPaint.setTextSize(oldSectionSize);
        infoPaint.setTextSize(oldInfoSize);
        infoPaint.setColor(Color.LTGRAY);
    }

    private void drawCardBase(Canvas canvas, RectF card, boolean selected) {
        canvas.drawRoundRect(card, 14 * uiScale, 14 * uiScale,
                selected ? selectedPaint : panelPaint);
        borderPaint.setColor(selected ? Color.rgb(255, 205, 75) : Color.rgb(110, 130, 175));
        canvas.drawRoundRect(card, 14 * uiScale, 14 * uiScale, borderPaint);
    }

    private void drawBackButton(Canvas canvas) {
        canvas.drawRoundRect(backButton, 12 * uiScale, 12 * uiScale, panelPaint);
        borderPaint.setColor(Color.rgb(255, 205, 75));
        canvas.drawRoundRect(backButton, 12 * uiScale, 12 * uiScale, borderPaint);
        float y = backButton.centerY() - (sectionPaint.ascent() + sectionPaint.descent()) / 2f;
        canvas.drawText(game.text("BACK", "QUAY LẠI"), backButton.centerX(), y, sectionPaint);
    }

    private String upper(String text) {
        return text.toUpperCase(game.getLanguage().isVietnamese()
                ? new java.util.Locale("vi", "VN") : java.util.Locale.US);
    }

    private String formatSeconds(long milliseconds, int decimals) {
        String pattern = decimals == 2 ? "%.2f" : "%.1f";
        return String.format(java.util.Locale.US, pattern, milliseconds / 1000f)
                + game.text("s", " giây");
    }

    private LoadoutManager.SkillType getCurrentSkill() {
        return LoadoutManager.SkillType.values()[currentSkillIndex];
    }

    private void showPreviousSkill() {
        int count = LoadoutManager.SkillType.values().length;
        currentSkillIndex = (currentSkillIndex - 1 + count) % count;
    }

    private void showNextSkill() {
        int count = LoadoutManager.SkillType.values().length;
        currentSkillIndex = (currentSkillIndex + 1) % count;
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
        if (touchDownY > MainActivity.GAME_HEIGHT * 0.47f
                && Math.abs(swipeX) > 80 * uiScale
                && Math.abs(swipeX) > Math.abs(swipeY)) {
            if (swipeX < 0) showNextSkill();
            else showPreviousSkill();
            return;
        }

        for (int i = 0; i < weaponCards.length; i++) {
            if (weaponCards[i].contains(x, y)) {
                game.getLoadoutManager().setWeapon(LoadoutManager.WeaponType.values()[i]);
                return;
            }
        }

        if (skillLeftArrow.contains(x, y)) {
            showPreviousSkill();
            return;
        }
        if (skillRightArrow.contains(x, y)) {
            showNextSkill();
            return;
        }
        for (int slot = 0; slot < skillSlotButtons.length; slot++) {
            if (skillSlotButtons[slot].contains(x, y)) {
                game.getLoadoutManager().assignSkillToSlot(slot, getCurrentSkill());
                return;
            }
        }

        if (backButton.contains(x, y)) {
            game.setCurrentGameState(Game.GameState.MENU);
        }
    }
}
