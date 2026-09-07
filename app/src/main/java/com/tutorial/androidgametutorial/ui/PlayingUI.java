package com.tutorial.androidgametutorial.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.entities.Player;
import com.tutorial.androidgametutorial.gamestates.Playing;
import com.tutorial.androidgametutorial.main.LoadoutManager;
import com.tutorial.androidgametutorial.main.MainActivity;

public class PlayingUI {

    private final Playing playing;
    private final float uiScale;

    // Controls
    private final PointF joystickCenterPos;
    private final PointF joystickKnobPos;
    private final float joystickRadius;
    private final float joystickKnobRadius;
    private int joystickPointerId = -1;
    private boolean touchDown;

    private final PointF attackBtnCenterPos;
    private final float attackBtnRadius;
    private int attackBtnPointerId = -1;

    private final PointF skillBtnCenterPos;
    private final float skillBtnRadius;
    private int skillBtnPointerId = -1;

    private final PointF sparkSkillBtnCenterPos;
    private final float sparkSkillBtnRadius;
    private int sparkSkillBtnPointerId = -1;

    private final PointF thirdSkillBtnCenterPos;
    private final float thirdSkillBtnRadius;
    private int thirdSkillBtnPointerId = -1;

    // Paint objects are created once and reused every frame.
    private final Paint joystickBasePaint = new Paint();
    private final Paint joystickRingPaint = new Paint();
    private final Paint joystickGuidePaint = new Paint();
    private final Paint joystickKnobPaint = new Paint();
    private final Paint joystickKnobRingPaint = new Paint();
    private final Paint joystickHighlightPaint = new Paint();
    private final Paint iconPaint = new Paint();
    private final Paint skillBackgroundPaint = new Paint();
    private final Paint skillReadyBackgroundPaint = new Paint();
    private final Paint skillDarkPaint = new Paint();
    private final Paint skillBorderPaint = new Paint();
    private final Paint statusTextPaint = new Paint();
    private final Paint statusBackgroundPaint = new Paint();
    private final Paint mapLevelPaint = new Paint();
    private final Paint mapBackgroundPaint = new Paint();
    private final Paint pauseOverlayPaint = new Paint();
    private final Paint pausePanelPaint = new Paint();
    private final Paint pauseButtonPaint = new Paint();
    private final Paint pauseButtonPressedPaint = new Paint();
    private final Paint pauseBorderPaint = new Paint();
    private final Paint pauseTitlePaint = new Paint();
    private final Paint pauseTextPaint = new Paint();

    private final RectF attackIconBounds;
    private final RectF throwSkillIconBounds;
    private final RectF sparkSkillIconBounds;
    private final RectF thirdSkillIconBounds;
    private final RectF attackCooldownBounds;
    private final RectF throwCooldownBounds;
    private final RectF sparkCooldownBounds;
    private final RectF thirdSkillCooldownBounds;
    private final Path skillCirclePath = new Path();
    private final Path cooldownProgressPath = new Path();

    private final CustomButton btnMenu;
    private final CustomButton btnContinue;
    private final CustomButton btnHome;
    private final float healthIconX;
    private final float healthIconY;

    public PlayingUI(Playing playing) {
        this.playing = playing;

        // The old UI was designed for 1920x1080. Scale sizes from that design
        // and anchor the controls to the screen edges.
        uiScale = Math.min(
                MainActivity.GAME_WIDTH / 1920f,
                MainActivity.GAME_HEIGHT / 1080f
        );

        joystickCenterPos = new PointF(
                250 * uiScale,
                MainActivity.GAME_HEIGHT - 280 * uiScale
        );
        joystickKnobPos = new PointF(joystickCenterPos.x, joystickCenterPos.y);
        joystickRadius = 120 * uiScale;
        joystickKnobRadius = 50 * uiScale;

        attackBtnCenterPos = new PointF(
                MainActivity.GAME_WIDTH - 220 * uiScale,
                MainActivity.GAME_HEIGHT - 185 * uiScale
        );
        attackBtnRadius = 80 * uiScale;

        skillBtnCenterPos = new PointF(
                attackBtnCenterPos.x - 200 * uiScale,
                attackBtnCenterPos.y
        );
        skillBtnRadius = attackBtnRadius;

        sparkSkillBtnCenterPos = new PointF(
                attackBtnCenterPos.x,
                attackBtnCenterPos.y - 185 * uiScale
        );
        sparkSkillBtnRadius = attackBtnRadius;

        thirdSkillBtnCenterPos = new PointF(
                attackBtnCenterPos.x - 200 * uiScale,
                attackBtnCenterPos.y - 185 * uiScale
        );
        thirdSkillBtnRadius = attackBtnRadius;

        attackIconBounds = createSquareBounds(attackBtnCenterPos, attackBtnRadius * 0.95f);
        throwSkillIconBounds = createSquareBounds(skillBtnCenterPos, skillBtnRadius * 0.95f);
        sparkSkillIconBounds = createSquareBounds(sparkSkillBtnCenterPos, sparkSkillBtnRadius * 0.95f);
        thirdSkillIconBounds = createSquareBounds(thirdSkillBtnCenterPos, thirdSkillBtnRadius * 0.95f);

        attackCooldownBounds = createSquareBounds(attackBtnCenterPos, attackBtnRadius);
        throwCooldownBounds = createSquareBounds(skillBtnCenterPos, skillBtnRadius);
        sparkCooldownBounds = createSquareBounds(sparkSkillBtnCenterPos, sparkSkillBtnRadius);
        thirdSkillCooldownBounds = createSquareBounds(thirdSkillBtnCenterPos, thirdSkillBtnRadius);

        healthIconX = 150 * uiScale;
        healthIconY = 25 * uiScale;

        joystickBasePaint.setColor(Color.argb(145, 9, 18, 32));
        joystickBasePaint.setStyle(Paint.Style.FILL);
        joystickBasePaint.setAntiAlias(true);

        joystickRingPaint.setColor(Color.argb(210, 150, 190, 220));
        joystickRingPaint.setStrokeWidth(5 * uiScale);
        joystickRingPaint.setStyle(Paint.Style.STROKE);
        joystickRingPaint.setAntiAlias(true);

        joystickGuidePaint.setColor(Color.argb(100, 190, 220, 240));
        joystickGuidePaint.setStrokeWidth(3 * uiScale);
        joystickGuidePaint.setStyle(Paint.Style.STROKE);
        joystickGuidePaint.setAntiAlias(true);

        joystickKnobPaint.setColor(Color.argb(235, 52, 83, 115));
        joystickKnobPaint.setStyle(Paint.Style.FILL);
        joystickKnobPaint.setAntiAlias(true);

        joystickKnobRingPaint.setColor(Color.argb(240, 205, 230, 245));
        joystickKnobRingPaint.setStrokeWidth(4 * uiScale);
        joystickKnobRingPaint.setStyle(Paint.Style.STROKE);
        joystickKnobRingPaint.setAntiAlias(true);

        joystickHighlightPaint.setColor(Color.argb(150, 255, 255, 255));
        joystickHighlightPaint.setStyle(Paint.Style.FILL);
        joystickHighlightPaint.setAntiAlias(true);

        iconPaint.setAntiAlias(false);
        iconPaint.setFilterBitmap(false);

        skillBackgroundPaint.setColor(Color.argb(210, 18, 28, 42));
        skillBackgroundPaint.setStyle(Paint.Style.FILL);
        skillBackgroundPaint.setAntiAlias(true);

        skillReadyBackgroundPaint.setColor(Color.argb(230, 42, 68, 92));
        skillReadyBackgroundPaint.setStyle(Paint.Style.FILL);
        skillReadyBackgroundPaint.setAntiAlias(true);

        skillDarkPaint.setColor(Color.argb(175, 0, 0, 0));
        skillDarkPaint.setStyle(Paint.Style.FILL);
        skillDarkPaint.setAntiAlias(true);

        skillBorderPaint.setColor(Color.rgb(215, 230, 240));
        skillBorderPaint.setStyle(Paint.Style.STROKE);
        skillBorderPaint.setStrokeWidth(5 * uiScale);
        skillBorderPaint.setAntiAlias(true);

        statusTextPaint.setFakeBoldText(true);
        statusTextPaint.setAntiAlias(true);

        statusBackgroundPaint.setColor(Color.argb(150, 0, 0, 0));
        statusBackgroundPaint.setAntiAlias(true);

        mapLevelPaint.setFakeBoldText(true);
        mapLevelPaint.setTextAlign(Paint.Align.CENTER);
        mapLevelPaint.setAntiAlias(true);
        mapBackgroundPaint.setAntiAlias(true);

        float menuPadding = 5 * uiScale;
        btnMenu = new CustomButton(
                menuPadding,
                menuPadding,
                ButtonImages.PLAYING_MENU.getWidth(),
                ButtonImages.PLAYING_MENU.getHeight()
        );

        float pauseButtonWidth = 340 * uiScale;
        float pauseButtonHeight = 115 * uiScale;
        float pauseButtonGap = 45 * uiScale;
        float pauseButtonsWidth = pauseButtonWidth * 2f + pauseButtonGap;
        float pauseButtonTop = MainActivity.GAME_HEIGHT / 2f;
        btnContinue = new CustomButton(
                (MainActivity.GAME_WIDTH - pauseButtonsWidth) / 2f,
                pauseButtonTop,
                pauseButtonWidth,
                pauseButtonHeight
        );
        btnHome = new CustomButton(
                btnContinue.getHitbox().right + pauseButtonGap,
                pauseButtonTop,
                pauseButtonWidth,
                pauseButtonHeight
        );

        pauseOverlayPaint.setColor(Color.argb(185, 0, 0, 0));
        pausePanelPaint.setColor(Color.argb(245, 18, 30, 52));
        pauseButtonPaint.setColor(Color.rgb(42, 75, 104));
        pauseButtonPressedPaint.setColor(Color.rgb(65, 112, 145));
        pauseBorderPaint.setColor(Color.rgb(225, 205, 100));
        pauseBorderPaint.setStyle(Paint.Style.STROKE);
        pauseBorderPaint.setStrokeWidth(Math.max(2f, 5f * uiScale));
        pauseBorderPaint.setAntiAlias(true);
        pauseTitlePaint.setColor(Color.WHITE);
        pauseTitlePaint.setTextAlign(Paint.Align.CENTER);
        pauseTitlePaint.setTextSize(52 * uiScale);
        pauseTitlePaint.setFakeBoldText(true);
        pauseTitlePaint.setAntiAlias(true);
        pauseTextPaint.setColor(Color.WHITE);
        pauseTextPaint.setTextAlign(Paint.Align.CENTER);
        pauseTextPaint.setTextSize(32 * uiScale);
        pauseTextPaint.setFakeBoldText(true);
        pauseTextPaint.setAntiAlias(true);
    }

    private RectF createSquareBounds(PointF center, float radius) {
        return new RectF(
                center.x - radius,
                center.y - radius,
                center.x + radius,
                center.y + radius
        );
    }

    private RectF createIconBounds(PointF center, float maxSize, Bitmap bitmap) {
        if (bitmap == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
            float halfSize = maxSize / 2f;
            return new RectF(center.x - halfSize, center.y - halfSize,
                    center.x + halfSize, center.y + halfSize);
        }

        float scale = Math.min(maxSize / bitmap.getWidth(), maxSize / bitmap.getHeight());
        float width = bitmap.getWidth() * scale;
        float height = bitmap.getHeight() * scale;
        return new RectF(
                center.x - width / 2f,
                center.y - height / 2f,
                center.x + width / 2f,
                center.y + height / 2f
        );
    }

    public void draw(Canvas canvas) {
        drawJoystick(canvas);

        Player player = playing.getPlayer();
        LoadoutManager loadout = playing.getGame().getLoadoutManager();
        LoadoutManager.WeaponType weapon = loadout.getWeapon();
        drawSkillIcon(canvas, weapon.getIconColumn(), weapon.getIconRow(), false,
                attackIconBounds, attackCooldownBounds,
                player.getAttackCooldownRemaining());
        drawSelectedSkill(canvas, loadout.getSkill(0), 0,
                throwSkillIconBounds, throwCooldownBounds);
        drawSelectedSkill(canvas, loadout.getSkill(1), 1,
                sparkSkillIconBounds, sparkCooldownBounds);
        drawSelectedSkill(canvas, loadout.getSkill(2), 2,
                thirdSkillIconBounds, thirdSkillCooldownBounds);

        drawMenuButton(canvas);
        drawHealthBar(canvas);
        drawStatusEffects(canvas);
        drawMapLevel(canvas);

        if (playing.isPauseMenuOpen()) {
            drawPauseMenu(canvas);
        }
    }

    private void drawPauseMenu(Canvas canvas) {
        canvas.drawRect(0f, 0f, MainActivity.GAME_WIDTH,
                MainActivity.GAME_HEIGHT, pauseOverlayPaint);

        float panelPadding = 55 * uiScale;
        float panelLeft = btnContinue.getHitbox().left - panelPadding;
        float panelRight = btnHome.getHitbox().right + panelPadding;
        float panelTop = btnContinue.getHitbox().top - 180 * uiScale;
        float panelBottom = btnContinue.getHitbox().bottom + panelPadding;
        RectF panel = new RectF(panelLeft, panelTop, panelRight, panelBottom);
        canvas.drawRoundRect(panel, 28 * uiScale, 28 * uiScale, pausePanelPaint);
        canvas.drawRoundRect(panel, 28 * uiScale, 28 * uiScale, pauseBorderPaint);

        float titleY = panelTop + 85 * uiScale
                - (pauseTitlePaint.ascent() + pauseTitlePaint.descent()) / 2f;
        canvas.drawText(playing.getGame().text(
                        "GAME PAUSED", "TRÒ CHƠI ĐÃ TẠM DỪNG"),
                panel.centerX(), titleY, pauseTitlePaint);
        drawPauseButton(canvas, btnContinue,
                playing.getGame().text("CONTINUE", "TIẾP TỤC"));
        drawPauseButton(canvas, btnHome,
                playing.getGame().text("HOME", "TRANG CHỦ"));
    }

    private void drawPauseButton(Canvas canvas, CustomButton button, String text) {
        RectF box = button.getHitbox();
        Paint background = button.isPushed()
                ? pauseButtonPressedPaint : pauseButtonPaint;
        canvas.drawRoundRect(box, 18 * uiScale, 18 * uiScale, background);
        canvas.drawRoundRect(box, 18 * uiScale, 18 * uiScale, pauseBorderPaint);
        float textY = box.centerY()
                - (pauseTextPaint.ascent() + pauseTextPaint.descent()) / 2f;
        canvas.drawText(text, box.centerX(), textY, pauseTextPaint);
    }

    private void drawJoystick(Canvas canvas) {
        canvas.drawCircle(joystickCenterPos.x, joystickCenterPos.y, joystickRadius, joystickBasePaint);
        canvas.drawCircle(joystickCenterPos.x, joystickCenterPos.y, joystickRadius, joystickRingPaint);
        canvas.drawCircle(joystickCenterPos.x, joystickCenterPos.y, joystickRadius * 0.58f, joystickGuidePaint);

        canvas.drawCircle(joystickKnobPos.x, joystickKnobPos.y, joystickKnobRadius, joystickKnobPaint);
        canvas.drawCircle(joystickKnobPos.x, joystickKnobPos.y, joystickKnobRadius, joystickKnobRingPaint);
        canvas.drawCircle(
                joystickKnobPos.x - joystickKnobRadius * 0.25f,
                joystickKnobPos.y - joystickKnobRadius * 0.25f,
                joystickKnobRadius * 0.14f,
                joystickHighlightPaint
        );
    }

    private void drawSelectedSkill(Canvas canvas, LoadoutManager.SkillType skill, int slot,
                                   RectF iconBounds, RectF cooldownBounds) {
        drawSkillIcon(canvas, skill.getIconResourceId(), 0, true, iconBounds,
                cooldownBounds, playing.getPlayer().getSkillCooldownRemaining(slot));
    }

    private void drawSkillIcon(Canvas canvas, int iconValue, int iconRow,
                               boolean resourceIcon, RectF iconBounds,
                               RectF cooldownBounds, float cooldownRemaining) {
        float centerX = cooldownBounds.centerX();
        float centerY = cooldownBounds.centerY();
        float radius = cooldownBounds.width() / 2f;

        skillCirclePath.reset();
        skillCirclePath.addCircle(centerX, centerY, radius, Path.Direction.CW);

        int circleSave = canvas.save();
        canvas.clipPath(skillCirclePath);

        float remaining = Math.max(0f, Math.min(1f, cooldownRemaining));
        if (remaining > 0f) {
            canvas.drawCircle(centerX, centerY, radius, skillBackgroundPaint);
            drawIcon(canvas, iconValue, iconRow, resourceIcon, iconBounds);
            canvas.drawCircle(centerX, centerY, radius, skillDarkPaint);

            float elapsed = 1f - remaining;
            if (elapsed > 0f) {
                cooldownProgressPath.reset();
                cooldownProgressPath.moveTo(centerX, centerY);
                cooldownProgressPath.arcTo(cooldownBounds, -90f, elapsed * 360f);
                cooldownProgressPath.close();

                int progressSave = canvas.save();
                canvas.clipPath(cooldownProgressPath);
                canvas.drawCircle(centerX, centerY, radius, skillReadyBackgroundPaint);
                drawIcon(canvas, iconValue, iconRow, resourceIcon, iconBounds);
                canvas.restoreToCount(progressSave);
            }
        } else {
            canvas.drawCircle(centerX, centerY, radius, skillReadyBackgroundPaint);
            drawIcon(canvas, iconValue, iconRow, resourceIcon, iconBounds);
        }

        canvas.restoreToCount(circleSave);

        canvas.drawCircle(centerX, centerY, radius, skillBorderPaint);
    }

    private void drawIcon(Canvas canvas, int iconValue, int iconRow,
                          boolean resourceIcon, RectF bounds) {
        if (resourceIcon) {
            LoadoutIcons.drawResource(canvas, iconValue, bounds, iconPaint);
        } else {
            LoadoutIcons.draw(canvas, iconValue, iconRow, bounds, iconPaint);
        }
    }

    private void drawMenuButton(Canvas canvas) {
        Bitmap buttonImage = ButtonImages.PLAYING_MENU.getBtnImg(btnMenu.isPushed());
        canvas.drawBitmap(buttonImage, btnMenu.getHitbox().left, btnMenu.getHitbox().top, null);
    }

    private void drawHealthBar(Canvas canvas) {
        Player player = playing.getPlayer();

        for (int i = 0; i < player.getMaxHealth() / 100; i++) {
            float x = healthIconX + 100 * uiScale * i;
            int heartValue = player.getCurrentHealth() - 100 * i;

            if (heartValue <= 0) {
                canvas.drawBitmap(HealthIcons.HEART_EMPTY.getIcon(), x, healthIconY, null);
            } else if (heartValue == 25) {
                canvas.drawBitmap(HealthIcons.HEART_1Q.getIcon(), x, healthIconY, null);
            } else if (heartValue == 50) {
                canvas.drawBitmap(HealthIcons.HEART_HALF.getIcon(), x, healthIconY, null);
            } else if (heartValue < 100) {
                canvas.drawBitmap(HealthIcons.HEART_3Q.getIcon(), x, healthIconY, null);
            } else {
                canvas.drawBitmap(HealthIcons.HEART_FULL.getIcon(), x, healthIconY, null);
            }
        }
    }

    private void drawStatusEffects(Canvas canvas) {
        Player player = playing.getPlayer();
        int heartCount = Math.max(1, player.getMaxHealth() / 100);
        float heartsWidth = heartCount * 100 * uiScale;
        float statusCenterX = healthIconX + heartsWidth / 2f;
        float statusY = healthIconY + 95 * uiScale;
        float armorX = statusCenterX - 85 * uiScale;
        float speedX = statusCenterX - 70 * uiScale;
        float speedY = player.hasShield() ? statusY + 50 * uiScale : statusY;

        statusTextPaint.setColor(Color.WHITE);
        statusTextPaint.setTextSize(30 * uiScale);

        if (player.hasSpeedBoost()) {
            String speedLabel = playing.getGame().text("SPEED", "TỐC ĐỘ");
            long timeLeft = player.getSpeedBoostTimeLeft();
            String speedTime = timeLeft > 0
                    ? timeLeft / 1000 + playing.getGame().text("s", " giây")
                    : "";
            float speedLabelWidth = statusTextPaint.measureText(speedLabel);
            float speedTimeX = speedX + speedLabelWidth + 14 * uiScale;
            statusTextPaint.setTextSize(20 * uiScale);
            float speedPanelRight = speedTimeX
                    + statusTextPaint.measureText(speedTime)
                    + 12 * uiScale;
            statusTextPaint.setTextSize(30 * uiScale);

            canvas.drawRoundRect(
                    speedX - 10 * uiScale,
                    speedY - 5 * uiScale,
                    speedPanelRight,
                    speedY + 35 * uiScale,
                    10 * uiScale,
                    10 * uiScale,
                    statusBackgroundPaint
            );

            statusTextPaint.setColor(Color.YELLOW);
            canvas.drawText(speedLabel, speedX, speedY + 25 * uiScale, statusTextPaint);

            if (timeLeft > 0) {
                statusTextPaint.setTextSize(20 * uiScale);
                canvas.drawText(
                        speedTime,
                        speedTimeX,
                        speedY + 25 * uiScale,
                        statusTextPaint
                );
                statusTextPaint.setTextSize(30 * uiScale);
            }
        }

        if (player.hasShield()) {
            String armorLabel = playing.getGame().text("ARMOR", "GIÁP");
            statusTextPaint.setTextSize(30 * uiScale);
            float armorLabelWidth = statusTextPaint.measureText(armorLabel);
            float armorCountX = armorX + armorLabelWidth + 14 * uiScale;

            statusTextPaint.setTextSize(20 * uiScale);
            String armorCount = "x" + player.getShieldHits();
            float armorPanelRight = armorCountX
                    + statusTextPaint.measureText(armorCount)
                    + 12 * uiScale;

            canvas.drawRoundRect(
                    armorX - 10 * uiScale,
                    statusY - 5 * uiScale,
                    armorPanelRight,
                    statusY + 35 * uiScale,
                    10 * uiScale,
                    10 * uiScale,
                    statusBackgroundPaint
            );

            statusTextPaint.setTextSize(30 * uiScale);
            statusTextPaint.setColor(Color.CYAN);
            canvas.drawText(armorLabel, armorX, statusY + 25 * uiScale, statusTextPaint);

            statusTextPaint.setTextSize(20 * uiScale);
            statusTextPaint.setColor(Color.WHITE);
            canvas.drawText(
                    armorCount,
                    armorCountX,
                    statusY + 25 * uiScale,
                    statusTextPaint
            );
        }
    }

    private void drawMapLevel(Canvas canvas) {
        if (playing.getMapManager() == null) return;

        int currentMapLevel = playing.getMapManager().getCurrentMapLevel();
        float mapTextX = MainActivity.GAME_WIDTH / 2f;
        float mapTextY = 80 * uiScale;
        float mapPanelHalfWidth = 190 * uiScale;

        mapLevelPaint.setColor(Color.WHITE);
        mapLevelPaint.setTextSize(40 * uiScale);

        String mapText;
        if (playing.isTutorialMode()) {
            mapText = playing.getGame().text("TUTORIAL", "HƯỚNG DẪN");
            mapBackgroundPaint.setColor(Color.argb(170, 80, 50, 140));
            mapLevelPaint.setColor(Color.rgb(255, 215, 64));
        } else if (currentMapLevel == 1) {
            mapText = playing.getGame().text("MAP 1", "MÀN 1");
            mapBackgroundPaint.setColor(Color.argb(150, 0, 100, 0));
        } else if (currentMapLevel == 2) {
            mapText = playing.getGame().text("MAP 2 - SNOW", "MÀN 2 - TUYẾT");
            mapBackgroundPaint.setColor(Color.argb(150, 0, 150, 200));
            mapLevelPaint.setColor(Color.CYAN);
        } else if (currentMapLevel == 3) {
            mapText = playing.getGame().text("MAP 3 - DESERT", "MÀN 3 - SA MẠC");
            mapBackgroundPaint.setColor(Color.argb(170, 180, 90, 0));
            mapLevelPaint.setColor(Color.YELLOW);
        } else {
            mapText = playing.getGame().text(
                    "MAP 4 - SHADOW REALM", "MÀN 4 - CÕI BÓNG TỐI");
            mapPanelHalfWidth = 275 * uiScale;
            mapBackgroundPaint.setColor(Color.argb(185, 63, 22, 103));
            mapLevelPaint.setColor(Color.rgb(211, 145, 255));
        }

        canvas.drawRoundRect(
                mapTextX - mapPanelHalfWidth,
                mapTextY - 35 * uiScale,
                mapTextX + mapPanelHalfWidth,
                mapTextY + 15 * uiScale,
                15 * uiScale,
                15 * uiScale,
                mapBackgroundPaint
        );
        canvas.drawText(mapText, mapTextX, mapTextY, mapLevelPaint);
    }

    public void touchEvents(MotionEvent event) {
        if (playing.isPauseMenuOpen()) {
            handlePauseMenuTouch(event);
            return;
        }

        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);
        PointF eventPos = new PointF(event.getX(actionIndex), event.getY(actionIndex));

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (checkInsideJoystick(eventPos, pointerId)) {
                    touchDown = true;
                    updateJoystick(eventPos.x, eventPos.y);
                } else if (checkInsideAttackBtn(eventPos) && attackBtnPointerId < 0) {
                    if (playing.tryPlayerAttack()) {
                        attackBtnPointerId = pointerId;
                    }
                } else if (checkInsideSkillBtn(eventPos) && skillBtnPointerId < 0) {
                    playing.castSkill(0);
                    skillBtnPointerId = pointerId;
                } else if (checkInsideSparkSkillBtn(eventPos) && sparkSkillBtnPointerId < 0) {
                    playing.castSkill(1);
                    sparkSkillBtnPointerId = pointerId;
                } else if (checkInsideThirdSkillBtn(eventPos) && thirdSkillBtnPointerId < 0) {
                    playing.castSkill(2);
                    thirdSkillBtnPointerId = pointerId;
                } else if (isIn(eventPos, btnMenu)) {
                    btnMenu.setPushed(true, pointerId);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (touchDown) {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        if (event.getPointerId(i) == joystickPointerId) {
                            updateJoystick(event.getX(i), event.getY(i));
                            break;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (pointerId == joystickPointerId) {
                    resetJoystick();
                } else {
                    if (isIn(eventPos, btnMenu) && btnMenu.isPushed(pointerId)) {
                        resetJoystick();
                        playing.openPauseMenu();
                    }
                    btnMenu.unPush(pointerId);

                    if (pointerId == attackBtnPointerId) {
                        attackBtnPointerId = -1;
                    }
                    if (pointerId == skillBtnPointerId) {
                        skillBtnPointerId = -1;
                    }
                    if (pointerId == sparkSkillBtnPointerId) {
                        sparkSkillBtnPointerId = -1;
                    }
                    if (pointerId == thirdSkillBtnPointerId) {
                        thirdSkillBtnPointerId = -1;
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                resetInput();
                break;
        }
    }

    private void handlePauseMenuTouch(MotionEvent event) {
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);
        float x = event.getX(actionIndex);
        float y = event.getY(actionIndex);

        if (action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_POINTER_DOWN) {
            if (btnContinue.getHitbox().contains(x, y)) {
                btnContinue.setPushed(true, pointerId);
            } else if (btnHome.getHitbox().contains(x, y)) {
                btnHome.setPushed(true, pointerId);
            }
            return;
        }

        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_POINTER_UP) {
            boolean continuePressed = btnContinue.isPushed(pointerId)
                    && btnContinue.getHitbox().contains(x, y);
            boolean homePressed = btnHome.isPushed(pointerId)
                    && btnHome.getHitbox().contains(x, y);
            btnContinue.unPush(pointerId);
            btnHome.unPush(pointerId);

            if (continuePressed) {
                playing.continueGame();
            } else if (homePressed) {
                playing.setGameStateToMenu();
            }
            return;
        }

        if (action == MotionEvent.ACTION_CANCEL) {
            releasePauseButtons();
        }
    }

    private void releasePauseButtons() {
        if (btnContinue.getPointerId() >= 0) {
            btnContinue.unPush(btnContinue.getPointerId());
        }
        if (btnHome.getPointerId() >= 0) {
            btnHome.unPush(btnHome.getPointerId());
        }
    }

    private boolean checkInsideJoystick(PointF eventPos, int pointerId) {
        if (!isInsideRadius(eventPos, joystickCenterPos, joystickRadius)) return false;

        joystickPointerId = pointerId;
        return true;
    }

    private boolean checkInsideAttackBtn(PointF eventPos) {
        return isInsideRadius(eventPos, attackBtnCenterPos, attackBtnRadius);
    }

    private boolean checkInsideSkillBtn(PointF eventPos) {
        return isInsideRadius(eventPos, skillBtnCenterPos, skillBtnRadius);
    }

    private boolean checkInsideSparkSkillBtn(PointF eventPos) {
        return isInsideRadius(eventPos, sparkSkillBtnCenterPos, sparkSkillBtnRadius);
    }

    private boolean checkInsideThirdSkillBtn(PointF eventPos) {
        return isInsideRadius(eventPos, thirdSkillBtnCenterPos, thirdSkillBtnRadius);
    }

    private void updateJoystick(float touchX, float touchY) {
        float xDiff = touchX - joystickCenterPos.x;
        float yDiff = touchY - joystickCenterPos.y;
        float distance = (float) Math.hypot(xDiff, yDiff);

        if (distance < 5 * uiScale) {
            joystickKnobPos.set(joystickCenterPos.x, joystickCenterPos.y);
            playing.setPlayerMoveFalse();
            return;
        }

        float maxKnobDistance = joystickRadius - joystickKnobRadius;
        float knobScale = Math.min(1f, maxKnobDistance / distance);
        joystickKnobPos.set(
                joystickCenterPos.x + xDiff * knobScale,
                joystickCenterPos.y + yDiff * knobScale
        );
        playing.setPlayerMoveTrue(new PointF(xDiff, yDiff));
    }

    private boolean isInsideRadius(PointF eventPos, PointF center, float radius) {
        float xDistance = eventPos.x - center.x;
        float yDistance = eventPos.y - center.y;
        return xDistance * xDistance + yDistance * yDistance <= radius * radius;
    }

    private void resetJoystick() {
        touchDown = false;
        joystickPointerId = -1;
        joystickKnobPos.set(joystickCenterPos.x, joystickCenterPos.y);
        playing.setPlayerMoveFalse();
    }

    public void resetInput() {
        resetJoystick();
        playing.getPlayer().setAttacking(false);
        attackBtnPointerId = -1;
        skillBtnPointerId = -1;
        sparkSkillBtnPointerId = -1;
        thirdSkillBtnPointerId = -1;

        if (btnMenu.getPointerId() >= 0) {
            btnMenu.unPush(btnMenu.getPointerId());
        }
        releasePauseButtons();
    }

    private boolean isIn(PointF eventPos, CustomButton button) {
        return button.getHitbox().contains(eventPos.x, eventPos.y);
    }
}
