package com.tutorial.androidgametutorial.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;


import com.tutorial.androidgametutorial.main.MainActivity;
import com.tutorial.androidgametutorial.R;
import com.tutorial.androidgametutorial.helpers.GameConstants;
import com.tutorial.androidgametutorial.helpers.BitmapCache;
import com.tutorial.androidgametutorial.helpers.interfaces.BitmapMethods;

public enum GameCharacters implements BitmapMethods {

    PLAYER(R.drawable.player_spritesheet, 7, 4, 1f, false, false, false),
    ADVENTURER(R.drawable.adventurer_spritesheet, 7, 4, 1f, false, false, false),
    WARRIOR(R.drawable.warrior_spritesheet, 7, 4, 1.30f, false, false, true),
    ROGUE(R.drawable.rogue_spritesheet, 7, 4, 1.30f, true, false, true),
    GUARDIAN(R.drawable.guardian_spritesheet, 7, 4, 1.30f, true, false, false),
    SKELETON(R.drawable.skeleton_spritesheet, 7, 4, 1f, false, false, false),
    BOOM(R.drawable.boom_smile, 1, 1, 1f, false, false, false);

    private static final int STABLE_CANVAS_WIDTH = 112;
    private static final float NEW_CHARACTER_TARGET_HEIGHT = 80f;
    private static final float ADVENTURER_TARGET_HEIGHT = 92f;
    private static final float STABLE_MAX_WIDTH = 108f;
    private final int resourceId;
    private volatile Bitmap[][] sprites;
    private final int rows;
    private final int cols;
    private final float drawScale;
    private final boolean mirrorSideFrames;
    private final boolean mirrorLeftInsteadOfRight;
    private final boolean swapNativeSideFrames;

    GameCharacters(int resID, int rows, int cols, float drawScale,
                   boolean mirrorSideFrames, boolean mirrorLeftInsteadOfRight,
                   boolean swapNativeSideFrames) {
        this.resourceId = resID;
        this.rows = rows;
        this.cols = cols;
        this.drawScale = drawScale;
        this.mirrorSideFrames = mirrorSideFrames;
        this.mirrorLeftInsteadOfRight = mirrorLeftInsteadOfRight;
        this.swapNativeSideFrames = swapNativeSideFrames;
    }

    private void ensureLoaded() {
        if (sprites != null) return;
        synchronized (this) {
            if (sprites != null) return;

            Bitmap spriteSheet = BitmapCache.decodeTemporary(
                    MainActivity.getGameContext(), resourceId, null
            );
            if (spriteSheet == null) {
                throw new IllegalStateException(
                        "Unable to decode character sheet " + resourceId
                );
            }

            Bitmap[][] loadedSprites = new Bitmap[rows][cols];
            float stableFrameScale = usesStableFrameScale()
                    ? calculateStableFrameScale(spriteSheet)
                    : 1f;
            try {
                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < cols; col++) {
                        int sourceCol = getSourceColumn(col);
                        int left = spriteSheet.getWidth() * sourceCol / cols;
                        int right = spriteSheet.getWidth() * (sourceCol + 1) / cols;
                        int top = spriteSheet.getHeight() * row / rows;
                        int bottom = spriteSheet.getHeight() * (row + 1) / rows;
                        Bitmap sourceFrame = Bitmap.createBitmap(
                                spriteSheet, left, top,
                                right - left, bottom - top
                        );
                        Bitmap preparedFrame = prepareFrame(sourceFrame, stableFrameScale);
                        if (preparedFrame != sourceFrame) sourceFrame.recycle();

                        if (shouldMirrorFrame(row, col)) {
                            Bitmap mirroredFrame = mirrorHorizontally(preparedFrame);
                            if (mirroredFrame != preparedFrame) preparedFrame.recycle();
                            loadedSprites[row][col] = mirroredFrame;
                        } else {
                            loadedSprites[row][col] = preparedFrame;
                        }
                    }
                }
                sprites = loadedSprites;
            } finally {
                if (!spriteSheet.isRecycled()) spriteSheet.recycle();
            }
        }
    }

    private int getSourceColumn(int requestedColumn) {
        if (swapNativeSideFrames) {
            if (requestedColumn == GameConstants.Face_Dir.LEFT) {
                return GameConstants.Face_Dir.RIGHT;
            }
            if (requestedColumn == GameConstants.Face_Dir.RIGHT) {
                return GameConstants.Face_Dir.LEFT;
            }
        }
        if (mirrorSideFrames
                && (requestedColumn == GameConstants.Face_Dir.LEFT
                || requestedColumn == GameConstants.Face_Dir.RIGHT)) {
            // Some generated sheets only have one reliable horizontal pose.
            // Use it as the source and build the opposite direction by mirroring.
            return GameConstants.Face_Dir.LEFT;
        }
        return requestedColumn;
    }

    private boolean shouldMirrorFrame(int row, int requestedColumn) {
        if (!mirrorSideFrames || swapNativeSideFrames) return false;

        boolean mirrorLeft = mirrorLeftInsteadOfRight;
        if (mirrorLeft) {
            return requestedColumn == GameConstants.Face_Dir.LEFT;
        }
        return requestedColumn == GameConstants.Face_Dir.RIGHT;
    }

    private Bitmap mirrorHorizontally(Bitmap frame) {
        Bitmap mirrored = Bitmap.createBitmap(
                frame.getWidth(), frame.getHeight(), Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(mirrored);
        canvas.scale(-1f, 1f, frame.getWidth() / 2f, frame.getHeight() / 2f);
        canvas.drawBitmap(frame, 0f, 0f, null);
        return mirrored;
    }

    private boolean usesStableFrameScale() {
        return this == ADVENTURER
                || this == WARRIOR
                || this == ROGUE
                || this == GUARDIAN;
    }

    private Bitmap prepareFrame(Bitmap frame, float stableFrameScale) {
        // Giữ nguyên loader cũ cho các sheet 16x16 có sẵn.
        if (frame.getWidth() == GameConstants.Sprite.DEFAULT_SIZE
                && frame.getHeight() == GameConstants.Sprite.DEFAULT_SIZE) {
            return getScaledBitmap(frame);
        }

        // Generated sheets can contain small pieces from the previous row at
        // the top of a cell. Ignore that strip and use one sheet-wide scale so
        // idle, walk and attack poses never change the character's body size.
        if (usesStableFrameScale()) {
            int ignoredTop = frame.getHeight() / 12;
            Rect visible = findVisibleBounds(frame, ignoredTop);
            int canvasWidth = STABLE_CANVAS_WIDTH;
            int canvasHeight = GameConstants.Sprite.SIZE;
            Bitmap result = Bitmap.createBitmap(
                    canvasWidth,
                    canvasHeight,
                    Bitmap.Config.ARGB_8888
            );
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setFilterBitmap(false);

            float scale = stableFrameScale;
            float width = visible.width() * scale;
            float height = visible.height() * scale;
            float left = (canvasWidth - width) / 2f;
            float top = canvasHeight - height - 2f;
            canvas.drawBitmap(frame, visible,
                    new RectF(left, top, left + width, top + height), paint);
            return result;
        }

        Rect visible = findVisibleBounds(frame);
        Bitmap result = Bitmap.createBitmap(
                GameConstants.Sprite.SIZE,
                GameConstants.Sprite.SIZE,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setFilterBitmap(false);

        float maxSize = GameConstants.Sprite.SIZE - 4f;
        float scale = Math.min(
                maxSize / visible.width(),
                maxSize / visible.height()
        );
        float width = visible.width() * scale;
        float height = visible.height() * scale;
        float left = (GameConstants.Sprite.SIZE - width) / 2f;
        float top = GameConstants.Sprite.SIZE - height - 2f;
        canvas.drawBitmap(frame, visible,
                new RectF(left, top, left + width, top + height), paint);
        return result;
    }

    private float calculateStableFrameScale(Bitmap spriteSheet) {
        int sheetWidth = spriteSheet.getWidth();
        int sheetHeight = spriteSheet.getHeight();
        int[] pixels = new int[sheetWidth * sheetHeight];
        spriteSheet.getPixels(
                pixels, 0, sheetWidth, 0, 0, sheetWidth, sheetHeight
        );

        int maxVisibleWidth = 1;
        int maxVisibleHeight = 1;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int cellLeft = sheetWidth * col / cols;
                int cellRight = sheetWidth * (col + 1) / cols;
                int cellTop = sheetHeight * row / rows;
                int cellBottom = sheetHeight * (row + 1) / rows;
                int scanTop = cellTop + (cellBottom - cellTop) / 12;

                int visibleLeft = cellRight;
                int visibleTop = cellBottom;
                int visibleRight = -1;
                int visibleBottom = -1;
                for (int y = scanTop; y < cellBottom; y++) {
                    int rowOffset = y * sheetWidth;
                    for (int x = cellLeft; x < cellRight; x++) {
                        if ((pixels[rowOffset + x] >>> 24) <= 8) continue;
                        visibleLeft = Math.min(visibleLeft, x);
                        visibleTop = Math.min(visibleTop, y);
                        visibleRight = Math.max(visibleRight, x);
                        visibleBottom = Math.max(visibleBottom, y);
                    }
                }

                if (visibleRight >= visibleLeft && visibleBottom >= visibleTop) {
                    maxVisibleWidth = Math.max(
                            maxVisibleWidth, visibleRight - visibleLeft + 1
                    );
                    maxVisibleHeight = Math.max(
                            maxVisibleHeight, visibleBottom - visibleTop + 1
                    );
                }
            }
        }

        // One scale per character sheet prevents short/wide attack poses from
        // being enlarged compared with idle and walk frames.
        return Math.min(
                getStableTargetHeight() / maxVisibleHeight,
                STABLE_MAX_WIDTH / maxVisibleWidth
        );
    }

    private float getStableTargetHeight() {
        return this == ADVENTURER
                ? ADVENTURER_TARGET_HEIGHT
                : NEW_CHARACTER_TARGET_HEIGHT;
    }

    private Rect findVisibleBounds(Bitmap frame) {
        return findVisibleBounds(frame, 0);
    }

    private Rect findVisibleBounds(Bitmap frame, int startY) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        int[] pixels = new int[width * height];
        frame.getPixels(pixels, 0, width, 0, 0, width, height);

        int left = width;
        int top = height;
        int right = -1;
        int bottom = -1;
        for (int y = Math.max(0, startY); y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((pixels[y * width + x] >>> 24) <= 8) continue;
                left = Math.min(left, x);
                top = Math.min(top, y);
                right = Math.max(right, x);
                bottom = Math.max(bottom, y);
            }
        }

        if (right < left || bottom < top) return new Rect(0, 0, width, height);
        return new Rect(left, top, right + 1, bottom + 1);
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public float getDrawScale() { return drawScale; }

    public int getMovementFrame(int animationStep) {
        int normalizedStep = Math.floorMod(
                animationStep,
                GameConstants.Animation.AMOUNT
        );

        if (this == WARRIOR) {
            // Row 3 of the generated Warrior sheet is an attack-ready pose,
            // not a walking frame. A mirrored three-frame ping-pong cycle
            // keeps every direction continuous without entering that pose.
            return switch (normalizedStep) {
                case 0 -> 0;
                case 1, 3 -> 1;
                default -> 2;
            };
        }
        return normalizedStep;
    }

    public Bitmap getSprite(int yPos, int xPos) {
        ensureLoaded();
        return sprites[yPos][xPos];
    }


}
