package com.tutorial.androidgametutorial.entities;

import android.graphics.Bitmap;

import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;

public class EffectExplosionSprites implements BitmapMethods {
    
    private Bitmap[] explosionSprites = new Bitmap[7]; // hits_41 đến hits_47
    
    public EffectExplosionSprites() {
        loadExplosionSprites();
    }
    
    private void loadExplosionSprites() {
        int[] explosionResIds = {
            R.drawable.hits_41,
            R.drawable.hits_42,
            R.drawable.hits_43,
            R.drawable.hits_44,
            R.drawable.hits_45,
            R.drawable.hits_46,
            R.drawable.hits_47
        };
        
        for (int i = 0; i < explosionSprites.length; i++) {
            explosionSprites[i] = BitmapCache.getScaled(
                    MainActivity.getGameContext(), explosionResIds[i],
                    GameConstants.Sprite.SIZE, GameConstants.Sprite.SIZE, false
            );
        }
    }
    
    public Bitmap getExplosionSprite(int frame) {
        if (frame < 0 || frame >= explosionSprites.length) {
            return explosionSprites[0]; // Default to first frame
        }
        return explosionSprites[frame];
    }
    
    public int getExplosionFrameCount() {
        return explosionSprites.length;
    }
}
