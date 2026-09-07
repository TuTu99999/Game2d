package com.tutorial.androidgametutorial.gamestates;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.main.MainActivity;

/**
 * A small image-based cutscene player. It loads only the current scene so the
 * cinematic stays light on memory even on older Android devices.
 */
public class CinematicScreen extends BaseState implements GameStateInterface {

    public enum Type {
        OPENING,
        TRUE_ENDING
    }

    private static final int[] OPENING_IMAGES = {
            R.drawable.opening_01_peaceful_world,
            R.drawable.opening_02_ancient_seal,
            R.drawable.opening_03_seal_shatters,
            R.drawable.opening_04_invasion,
            R.drawable.opening_05_adventurer_rises,
            R.drawable.opening_06_fallen_village,
            R.drawable.opening_07_frozen_forest,
            R.drawable.opening_08_cursed_desert,
            R.drawable.opening_09_realm_of_death,
            R.drawable.opening_10_title_journey
    };

    private static final String[] OPENING_CAPTIONS = {
            "Long ago, the world lived beneath the protection\nof the Seal of Dawn.",
            "It imprisoned an ancient evil...\nthe Bringer of Death.",
            "But one night, the seal was shattered.",
            "The dead rose, and monsters spread across the land.",
            "One survivor refused to run.",
            "The journey began in a village\non the edge of ruin.",
            "Beyond the village waited a land\nfrozen by darkness.",
            "An ancient gate slept beneath the cursed sands.",
            "And beyond it... a realm\nno living soul had returned from.",
            "The hunt begins."
    };

    private static final String[] OPENING_CAPTIONS_VI = {
            "Thuở xa xưa, thế giới được bảo vệ\nbởi Phong Ấn Bình Minh.",
            "Nó giam giữ một ác thần cổ xưa...\nKẻ Mang Đến Cái Chết.",
            "Nhưng vào một đêm, phong ấn đã vỡ tan.",
            "Người chết trỗi dậy, quái vật tràn khắp thế gian.",
            "Một người sống sót đã không chịu chạy trốn.",
            "Hành trình bắt đầu tại một ngôi làng\nbên bờ diệt vong.",
            "Phía sau ngôi làng là vùng đất\nbị bóng tối đóng băng.",
            "Một cánh cổng cổ ngủ quên dưới sa mạc bị nguyền rủa.",
            "Và phía sau nó... là một cõi giới\nchưa người sống nào trở về.",
            "Cuộc săn bắt đầu."
    };

    private static final double[] OPENING_DURATIONS = {
            3.2, 3.4, 3.8, 3.8, 3.3, 3.2, 3.2, 3.2, 3.5, 4.0
    };

    private static final int[] ENDING_IMAGES = {
            R.drawable.ending_01_boss_falls,
            R.drawable.ending_02_final_fragment,
            R.drawable.ending_03_truth,
            R.drawable.ending_04_choice,
            R.drawable.ending_05_breaking_seal,
            R.drawable.ending_06_world_awakens,
            R.drawable.ending_07_journey_home,
            R.drawable.ending_08_after_credit
    };

    private static final String[] ENDING_CAPTIONS = {
            "At last, the Bringer of Death had fallen.",
            "But the restored seal demanded a terrible price.",
            "Restore the seal...\nand another will take my place.",
            "No more sacrifices.\nThis cycle ends with me.",
            "The Slayer did not restore the seal.\nThe Slayer destroyed it.",
            "For the first time in centuries,\nthe dead were free.",
            "The world needed no new guardian...\nonly the courage to face the dawn.",
            "Death was gone.\nBut something else had awakened."
    };

    private static final String[] ENDING_CAPTIONS_VI = {
            "Cuối cùng, Kẻ Mang Đến Cái Chết đã gục ngã.",
            "Nhưng việc khôi phục phong ấn đòi hỏi một cái giá khủng khiếp.",
            "Khôi phục phong ấn...\nvà một kẻ khác sẽ thế chỗ ta.",
            "Không còn hi sinh nữa.\nVòng luân hồi này kết thúc cùng ta.",
            "Dũng sĩ không khôi phục phong ấn.\nDũng sĩ đã phá hủy nó.",
            "Lần đầu tiên sau nhiều thế kỷ,\nnhững linh hồn đã được tự do.",
            "Thế giới không cần thêm người canh giữ...\nchỉ cần dũng khí đối mặt bình minh.",
            "Cái chết đã biến mất.\nNhưng một thứ khác vừa thức tỉnh."
    };

    private static final double[] ENDING_DURATIONS = {
            3.5, 4.0, 4.5, 4.0, 4.3, 4.0, 4.2, 4.5
    };

    private final Type type;
    private final int[] imageIds;
    private final String[] captions;
    private final double[] durations;

    private final Paint imagePaint = new Paint();
    private final Paint overlayPaint = new Paint();
    private final Paint captionPaint = new Paint();
    private final Paint labelPaint = new Paint();
    private final Paint titlePaint = new Paint();
    private final Paint buttonPaint = new Paint();
    private final Paint buttonBorderPaint = new Paint();
    private final Paint buttonTextPaint = new Paint();
    private final Rect sourceRect = new Rect();
    private final Rect destinationRect;
    private final RectF skipButton;
    private final float uiScale;

    private SoundPool soundPool;
    private int explosionSoundId;
    private int whooshSoundId;
    private Bitmap currentImage;
    private int currentScene;
    private double sceneElapsed;
    private boolean finished;

    public CinematicScreen(Game game, Type type) {
        super(game);
        this.type = type;

        if (type == Type.OPENING) {
            imageIds = OPENING_IMAGES;
            captions = OPENING_CAPTIONS;
            durations = OPENING_DURATIONS;
        } else {
            imageIds = ENDING_IMAGES;
            captions = ENDING_CAPTIONS;
            durations = ENDING_DURATIONS;
        }

        uiScale = Math.min(
                MainActivity.GAME_WIDTH / 1920f,
                MainActivity.GAME_HEIGHT / 1080f
        );
        destinationRect = new Rect(
                0, 0, MainActivity.GAME_WIDTH, MainActivity.GAME_HEIGHT
        );
        skipButton = new RectF(
                MainActivity.GAME_WIDTH - 190f * uiScale,
                28f * uiScale,
                MainActivity.GAME_WIDTH - 28f * uiScale,
                88f * uiScale
        );

        imagePaint.setFilterBitmap(false);

        captionPaint.setColor(Color.WHITE);
        captionPaint.setTextSize(31f * uiScale);
        captionPaint.setTextAlign(Paint.Align.CENTER);
        captionPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        captionPaint.setAntiAlias(true);

        labelPaint.setColor(Color.rgb(255, 206, 84));
        labelPaint.setTextSize(21f * uiScale);
        labelPaint.setTextAlign(Paint.Align.LEFT);
        labelPaint.setTypeface(Typeface.DEFAULT_BOLD);
        labelPaint.setAntiAlias(true);

        titlePaint.setColor(Color.rgb(255, 205, 75));
        titlePaint.setTextSize(62f * uiScale);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setShadowLayer(8f * uiScale, 0f, 3f * uiScale, Color.BLACK);
        titlePaint.setAntiAlias(true);

        buttonPaint.setColor(Color.argb(185, 10, 16, 38));
        buttonPaint.setAntiAlias(true);

        buttonBorderPaint.setColor(Color.rgb(255, 205, 75));
        buttonBorderPaint.setStyle(Paint.Style.STROKE);
        buttonBorderPaint.setStrokeWidth(Math.max(2f, 3f * uiScale));
        buttonBorderPaint.setAntiAlias(true);

        buttonTextPaint.setColor(Color.WHITE);
        buttonTextPaint.setTextSize(23f * uiScale);
        buttonTextPaint.setTextAlign(Paint.Align.CENTER);
        buttonTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        buttonTextPaint.setAntiAlias(true);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();
        explosionSoundId = soundPool.load(game.getContext(), R.raw.explosion, 1);
        whooshSoundId = soundPool.load(game.getContext(), R.raw.fast_whoosh, 1);
    }

    public void restart() {
        currentScene = 0;
        sceneElapsed = 0.0;
        finished = false;
        loadCurrentImage();
    }

    @Override
    public void update(double delta) {
        if (finished) return;

        sceneElapsed += delta;
        if (sceneElapsed >= durations[currentScene]) {
            currentScene++;
            sceneElapsed = 0.0;

            if (currentScene >= imageIds.length) {
                finishCinematic();
                return;
            }

            loadCurrentImage();
            playSceneSound();
        }
    }

    @Override
    public void render(Canvas canvas) {
        if (currentImage == null || finished) return;

        updateSourceRect();
        canvas.drawBitmap(currentImage, sourceRect, destinationRect, imagePaint);

        if (isOpeningTitleScene()) {
            canvas.drawText(
                    game.text(Game.GAME_TITLE, "HÀNH TRÌNH DIỆT QUÁI"),
                    MainActivity.GAME_WIDTH / 2f,
                    MainActivity.GAME_HEIGHT * 0.30f,
                    titlePaint
            );
        } else if (isEndingTitleScene()) {
            canvas.drawText(
                    game.text("TRUE ENDING", "KẾT THÚC THỰC SỰ"),
                    MainActivity.GAME_WIDTH / 2f,
                    MainActivity.GAME_HEIGHT * 0.22f,
                    titlePaint
            );
            canvas.drawText(
                    game.text("THE CYCLE IS BROKEN", "VÒNG LUÂN HỒI ĐÃ BỊ PHÁ VỠ"),
                    MainActivity.GAME_WIDTH / 2f,
                    MainActivity.GAME_HEIGHT * 0.30f,
                    titlePaint
            );
        }

        drawCaption(canvas);
        drawHeader(canvas);
        drawSkipButton(canvas);
        drawFade(canvas);
    }

    private void updateSourceRect() {
        int bitmapWidth = currentImage.getWidth();
        int bitmapHeight = currentImage.getHeight();
        float screenAspect = MainActivity.GAME_WIDTH / (float) MainActivity.GAME_HEIGHT;

        int baseWidth = bitmapWidth;
        int baseHeight = Math.round(baseWidth / screenAspect);
        if (baseHeight > bitmapHeight) {
            baseHeight = bitmapHeight;
            baseWidth = Math.round(baseHeight * screenAspect);
        }

        float progress = (float) Math.min(1.0, sceneElapsed / durations[currentScene]);
        float zoom = 1f + 0.035f * progress;
        int cropWidth = Math.max(1, Math.round(baseWidth / zoom));
        int cropHeight = Math.max(1, Math.round(baseHeight / zoom));

        float direction = currentScene % 2 == 0 ? 1f : -1f;
        float maxPan = Math.max(0f, (baseWidth - cropWidth) * 0.35f);
        float centerX = bitmapWidth / 2f + direction * maxPan * (progress - 0.5f);
        float centerY = bitmapHeight / 2f;

        int left = clamp(Math.round(centerX - cropWidth / 2f), 0, bitmapWidth - cropWidth);
        int top = clamp(Math.round(centerY - cropHeight / 2f), 0, bitmapHeight - cropHeight);
        sourceRect.set(left, top, left + cropWidth, top + cropHeight);
    }

    private void drawCaption(Canvas canvas) {
        float panelTop = MainActivity.GAME_HEIGHT * 0.76f;
        String caption;
        if (game.getLanguage().isVietnamese()) {
            caption = type == Type.OPENING
                    ? OPENING_CAPTIONS_VI[currentScene]
                    : ENDING_CAPTIONS_VI[currentScene];
        } else {
            caption = captions[currentScene];
        }
        String[] lines = caption.split("\\n");
        float lineHeight = 42f * uiScale;
        float centerY = panelTop + (MainActivity.GAME_HEIGHT - panelTop) / 2f;
        float firstY = centerY - (lines.length - 1) * lineHeight / 2f
                - (captionPaint.ascent() + captionPaint.descent()) / 2f;

        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(
                    lines[i],
                    MainActivity.GAME_WIDTH / 2f,
                    firstY + i * lineHeight,
                    captionPaint
            );
        }
    }

    private void drawHeader(Canvas canvas) {
        String label = type == Type.OPENING
                ? game.text("OPENING", "MỞ ĐẦU")
                : game.text("TRUE ENDING", "KẾT THÚC THỰC SỰ");
        canvas.drawText(
                label + "  " + (currentScene + 1) + "/" + imageIds.length,
                30f * uiScale,
                61f * uiScale,
                labelPaint
        );
    }

    private void drawSkipButton(Canvas canvas) {
        float radius = 12f * uiScale;
        canvas.drawRoundRect(skipButton, radius, radius, buttonPaint);
        canvas.drawRoundRect(skipButton, radius, radius, buttonBorderPaint);
        canvas.drawText(
                game.text("SKIP", "BỎ QUA"),
                skipButton.centerX(),
                skipButton.centerY() - (buttonTextPaint.ascent()
                        + buttonTextPaint.descent()) / 2f,
                buttonTextPaint
        );
    }

    private void drawFade(Canvas canvas) {
        double fadeDuration = 0.45;
        int alpha = 0;

        if (sceneElapsed < fadeDuration) {
            alpha = (int) (255 * (1.0 - sceneElapsed / fadeDuration));
        } else if (sceneElapsed > durations[currentScene] - fadeDuration) {
            alpha = (int) (255 * (sceneElapsed
                    - (durations[currentScene] - fadeDuration)) / fadeDuration);
        }

        if (alpha > 0) {
            overlayPaint.setColor(Color.argb(clamp(alpha, 0, 255), 0, 0, 0));
            canvas.drawRect(destinationRect, overlayPaint);
        }
    }

    private boolean isOpeningTitleScene() {
        return type == Type.OPENING && currentScene == imageIds.length - 1;
    }

    private boolean isEndingTitleScene() {
        return type == Type.TRUE_ENDING && currentScene == imageIds.length - 1;
    }

    private void loadCurrentImage() {
        recycleCurrentImage();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        currentImage = BitmapFactory.decodeResource(
                game.getContext().getResources(),
                imageIds[currentScene],
                options
        );
    }

    private void playSceneSound() {
        if (!game.isSoundOn() || soundPool == null) return;

        if ((type == Type.OPENING && (currentScene == 2 || currentScene == 3))
                || (type == Type.TRUE_ENDING && currentScene == 4)) {
            soundPool.play(explosionSoundId, 0.75f, 0.75f, 1, 0, 0.9f);
        } else if ((type == Type.OPENING && (currentScene == 4 || currentScene == 9))
                || (type == Type.TRUE_ENDING && currentScene == 7)) {
            soundPool.play(whooshSoundId, 0.65f, 0.65f, 1, 0, 0.9f);
        }
    }

    private void finishCinematic() {
        if (finished) return;
        finished = true;
        recycleCurrentImage();

        if (type == Type.OPENING) {
            game.finishOpeningCinematic();
        } else {
            game.finishTrueEnding();
        }
    }

    @Override
    public void touchEvents(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP
                && skipButton.contains(event.getX(), event.getY())) {
            finishCinematic();
        }
    }

    public void dispose() {
        recycleCurrentImage();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    private void recycleCurrentImage() {
        if (currentImage != null && !currentImage.isRecycled()) {
            currentImage.recycle();
        }
        currentImage = null;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
