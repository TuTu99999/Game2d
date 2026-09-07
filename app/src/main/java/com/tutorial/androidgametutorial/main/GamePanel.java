package com.tutorial.androidgametutorial.main;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    private final Game game;

    public GamePanel(Context context) {
        super(context);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        game = new Game(holder, context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return game.touchEvent(event);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        game.setSurfaceReady(true);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        // Surface is still valid
        game.setSurfaceReady(true);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        game.setSurfaceReady(false);
    }

    public void pauseGame() {
        game.pauseGame();
    }

    public void resumeGame() {
        game.resumeGame();
    }

    public void destroyGame() {
        game.cleanup();
    }
}
