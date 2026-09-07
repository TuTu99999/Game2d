package com.tutorial.androidgametutorial.ui;


import android.graphics.Bitmap;

import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.main.MainActivity;

public enum GameImages {


    HOME_BACKGROUND(R.drawable.home_background_4_maps),
    MAINMENU_MENUBG(R.drawable.mainmenu_menubackground),
    DEATH_MENU_MENUBG(R.drawable.menu_youdied_background);

    private final int resourceId;
    private volatile Bitmap image;

    GameImages(int resID) {
        resourceId = resID;
    }

    public Bitmap getImage() {
        if (image == null || image.isRecycled()) {
            synchronized (this) {
                if (image == null || image.isRecycled()) {
                    image = BitmapCache.get(
                            MainActivity.getGameContext(), resourceId
                    );
                }
            }
        }
        return image;
    }
}
