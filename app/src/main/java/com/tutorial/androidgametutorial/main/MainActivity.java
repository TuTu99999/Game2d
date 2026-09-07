package com.tutorial.androidgametutorial.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private static Context gameContext;
    public static int GAME_WIDTH, GAME_HEIGHT;
    private GamePanel gamePanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the application context so this static reference does not keep
        // an old Activity alive after the screen is recreated.
        gameContext = getApplicationContext();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(dm);

        GAME_WIDTH = dm.widthPixels;
        GAME_HEIGHT = dm.heightPixels;

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        gamePanel = new GamePanel(this);
        setContentView(gamePanel);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Match the Home and Recents navigation buttons: retain the
                // task and let onPause() freeze the active game safely.
                moveTaskToBack(true);
            }
        });
    }

    @Override
    protected void onPause() {
        if (gamePanel != null) {
            gamePanel.pauseGame();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gamePanel != null) {
            gamePanel.resumeGame();
        }
    }

    @Override
    protected void onDestroy() {
        if (gamePanel != null) {
            gamePanel.destroyGame();
        }
        super.onDestroy();
    }

    public static Context getGameContext() {
        return gameContext;
    }
}
