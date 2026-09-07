package com.tutorial.androidgametutorial.gamestates;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.interfaces.GameStateInterface;
import com.tutorial.androidgametutorial.main.Game;
import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.ui.GameImages;

public class SplashScreen extends BaseState implements GameStateInterface {

    private static final double SPLASH_DURATION = 1.5;

    private final Bitmap background;
    private final Bitmap logo;
    private final Rect backgroundSource;
    private final Rect backgroundDestination;
    private final Path logoClipPath = new Path();

    private final Paint darkOverlayPaint = new Paint();
    private final Paint logoBackgroundPaint = new Paint();
    private final Paint titlePaint = new Paint();
    private final Paint studioPaint = new Paint();
    private final Paint loadingBackgroundPaint = new Paint();
    private final Paint loadingPaint = new Paint();

    private final float uiScale;
    private double elapsedTime;

    public SplashScreen(Game game) {
        super(game);

        uiScale = Math.min(
                MainActivity.GAME_WIDTH / 1920f,
                MainActivity.GAME_HEIGHT / 1080f
        );

        background = GameImages.HOME_BACKGROUND.getImage();
        backgroundSource = new Rect(0, 0, background.getWidth(), background.getHeight());
        backgroundDestination = new Rect(0, 0, MainActivity.GAME_WIDTH, MainActivity.GAME_HEIGHT);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap originalLogo = BitmapFactory.decodeResource(
                game.getContext().getResources(),
                R.drawable.launcher_adventurer_icon,
                options
        );
        int logoSize = Math.max(1, Math.round(172 * uiScale));
        logo = Bitmap.createScaledBitmap(originalLogo, logoSize, logoSize, false);
        if (logo != originalLogo) {
            originalLogo.recycle();
        }

        float logoCenterX = MainActivity.GAME_WIDTH / 2f;
        float logoCenterY = MainActivity.GAME_HEIGHT * 0.34f;
        logoClipPath.addCircle(logoCenterX, logoCenterY, 86 * uiScale, Path.Direction.CW);

        darkOverlayPaint.setColor(Color.argb(105, 0, 0, 20));

        logoBackgroundPaint.setColor(Color.argb(210, 24, 30, 70));
        logoBackgroundPaint.setStyle(Paint.Style.FILL);
        logoBackgroundPaint.setAntiAlias(true);

        titlePaint.setColor(Color.rgb(255, 205, 75));
        titlePaint.setTextSize(72 * uiScale);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setAntiAlias(true);

        studioPaint.setColor(Color.WHITE);
        studioPaint.setTextSize(28 * uiScale);
        studioPaint.setTextAlign(Paint.Align.CENTER);
        studioPaint.setAntiAlias(true);

        loadingBackgroundPaint.setColor(Color.argb(180, 10, 14, 35));
        loadingPaint.setColor(Color.rgb(255, 185, 60));
    }

    @Override
    public void update(double delta) {
        elapsedTime += delta;
        if (elapsedTime >= SPLASH_DURATION) {
            game.startOpeningCinematic();
        }
    }

    @Override
    public void render(Canvas canvas) {
        int contentAlpha = getContentAlpha();
        int savedLayer = canvas.saveLayerAlpha(
                0,
                0,
                MainActivity.GAME_WIDTH,
                MainActivity.GAME_HEIGHT,
                contentAlpha
        );

        canvas.drawBitmap(background, backgroundSource, backgroundDestination, null);
        canvas.drawRect(backgroundDestination, darkOverlayPaint);

        float centerX = MainActivity.GAME_WIDTH / 2f;
        float logoCenterY = MainActivity.GAME_HEIGHT * 0.34f;
        float logoRadius = 86 * uiScale;
        canvas.drawCircle(centerX, logoCenterY, logoRadius, logoBackgroundPaint);
        int logoSave = canvas.save();
        canvas.clipPath(logoClipPath);
        canvas.drawBitmap(
                logo,
                centerX - logo.getWidth() / 2f,
                logoCenterY - logo.getHeight() / 2f,
                null
        );
        canvas.restoreToCount(logoSave);

        canvas.drawText(
                game.text(Game.GAME_TITLE, "HÀNH TRÌNH DIỆT QUÁI"),
                centerX,
                MainActivity.GAME_HEIGHT * 0.58f,
                titlePaint
        );
        canvas.drawText(
                Game.STUDIO_NAME,
                centerX,
                MainActivity.GAME_HEIGHT * 0.65f,
                studioPaint
        );

        float barWidth = 420 * uiScale;
        float barHeight = 14 * uiScale;
        float barLeft = centerX - barWidth / 2f;
        float barTop = MainActivity.GAME_HEIGHT * 0.76f;
        float progress = (float) Math.min(1, elapsedTime / SPLASH_DURATION);

        canvas.drawRect(
                barLeft,
                barTop,
                barLeft + barWidth,
                barTop + barHeight,
                loadingBackgroundPaint
        );
        canvas.drawRect(
                barLeft,
                barTop,
                barLeft + barWidth * progress,
                barTop + barHeight,
                loadingPaint
        );

        canvas.restoreToCount(savedLayer);
    }

    private int getContentAlpha() {
        double fadeDuration = 0.3;

        if (elapsedTime < fadeDuration) {
            return (int) (255 * elapsedTime / fadeDuration);
        }
        if (elapsedTime > SPLASH_DURATION - fadeDuration) {
            return (int) (255 * (SPLASH_DURATION - elapsedTime) / fadeDuration);
        }
        return 255;
    }

    @Override
    public void touchEvents(MotionEvent event) {
        // Splash changes screen automatically.
    }
}
