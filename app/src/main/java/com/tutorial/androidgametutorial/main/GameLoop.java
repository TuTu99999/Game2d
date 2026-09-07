package com.tutorial.androidgametutorial.main;

import java.util.concurrent.locks.LockSupport;

public class GameLoop implements Runnable {

    private static final int TARGET_FPS = 60;
    private static final long FRAME_DURATION_NANOS = 1_000_000_000L / TARGET_FPS;
    private static final double NANOS_TO_SECONDS = 1.0 / 1_000_000_000.0;
    private static final double MAX_DELTA_SECONDS = 0.05;

    private Thread gameThread;
    private final Game game;
    private final Object suspensionLock = new Object();
    private volatile boolean running = false;
    private boolean pausedFrameRequested;
    private boolean resetDeltaAfterSuspension;

    public GameLoop(Game game) {
        this.game = game;
    }

    @Override
    public void run() {
        long previousFrameStart = System.nanoTime();

        try {
            while (running) {
                if (!awaitFramePermission()) break;

                long frameStart = System.nanoTime();
                double delta = resetDeltaAfterSuspension
                        ? 0.0
                        : Math.min(
                                (frameStart - previousFrameStart) * NANOS_TO_SECONDS,
                                MAX_DELTA_SECONDS
                        );
                resetDeltaAfterSuspension = false;
                previousFrameStart = frameStart;

                game.update(delta);
                game.render();

                // Pace from the beginning of the frame. Rendering time is part
                // of the 16.67 ms budget instead of being added on top of it.
                // If a frame is late, start the next one immediately; never run
                // catch-up updates that could make gameplay advance too fast.
                waitUntil(frameStart + FRAME_DURATION_NANOS);
            }
        } finally {
            synchronized (this) {
                if (gameThread == Thread.currentThread()) {
                    running = false;
                    gameThread = null;
                }
            }
        }
    }

    public synchronized void startGameLoop() {
        if (!running) {
            running = true;
            gameThread = new Thread(this, "GameLoop");
            gameThread.start();
        }
    }

    /**
     * Processes one input-driven frame while the in-game pause menu is idle.
     * The loop goes straight back to sleep unless that input resumes the game.
     */
    public void requestPausedFrame() {
        synchronized (suspensionLock) {
            pausedFrameRequested = true;
            suspensionLock.notifyAll();
        }
    }

    private boolean awaitFramePermission() {
        synchronized (suspensionLock) {
            while (running
                    && game.shouldSuspendFrameLoop()
                    && !pausedFrameRequested) {
                try {
                    resetDeltaAfterSuspension = true;
                    suspensionLock.wait();
                } catch (InterruptedException exception) {
                    if (!running) return false;
                }
            }

            if (!running) return false;
            pausedFrameRequested = false;
            return true;
        }
    }

    private void waitUntil(long deadlineNanos) {
        while (running) {
            long remainingNanos = deadlineNanos - System.nanoTime();
            if (remainingNanos <= 0L) return;

            LockSupport.parkNanos(remainingNanos);
            if (Thread.currentThread().isInterrupted()) return;
        }
    }

    public void stopGameLoop() {
        Thread threadToStop;

        synchronized (this) {
            running = false;
            threadToStop = gameThread;
            if (threadToStop != null) {
                threadToStop.interrupt();
            }
        }

        synchronized (suspensionLock) {
            pausedFrameRequested = false;
            suspensionLock.notifyAll();
        }

        // Never wait for the current thread to finish itself.
        if (threadToStop != null && threadToStop != Thread.currentThread()) {
            try {
                threadToStop.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        synchronized (this) {
            if (gameThread == threadToStop) {
                gameThread = null;
            }
        }
    }
}
