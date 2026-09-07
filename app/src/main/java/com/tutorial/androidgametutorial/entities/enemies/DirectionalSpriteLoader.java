package com.tutorial.androidgametutorial.entities.enemies;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.tutorial.androidgametutorial.main.MainActivity;

/** Táº£i sheet: má»—i hÃ ng lÃ  má»™t frame, 4 cá»™t lÃ  4 hÆ°á»›ng. */
final class DirectionalSpriteLoader {

    private static final int DIRECTIONS = 4;

    private DirectionalSpriteLoader() {
    }

    static Bitmap[][] load(int resourceId, int sheetRows,
                           int startRow, int frameCount) {
        return load(resourceId, sheetRows, startRow, frameCount, false);
    }

    static Bitmap[][] load(int resourceId, int sheetRows,
                           int startRow, int frameCount,
                           boolean cleanEdgeFragments) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap sheet = BitmapFactory.decodeResource(
                MainActivity.getGameContext().getResources(), resourceId, options
        );

        Bitmap[][] result = new Bitmap[DIRECTIONS][frameCount];
        for (int direction = 0; direction < DIRECTIONS; direction++) {
            int left = sheet.getWidth() * direction / DIRECTIONS;
            int right = sheet.getWidth() * (direction + 1) / DIRECTIONS;

            for (int frame = 0; frame < frameCount; frame++) {
                int row = startRow + frame;
                int top = sheet.getHeight() * row / sheetRows;
                int bottom = sheet.getHeight() * (row + 1) / sheetRows;
                Bitmap cell = Bitmap.createBitmap(
                        sheet, left, top, right - left, bottom - top
                );
                Bitmap transparent = removeBakedCheckerboard(cell);
                if (cleanEdgeFragments) {
                    removeSmallEdgeFragments(transparent);
                }
                result[direction][frame] = trimTransparentPixels(transparent);
            }
        }
        sheet.recycle();
        return result;
    }

    private static void removeSmallEdgeFragments(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int size = width * height;
        int[] pixels = new int[size];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        boolean[] visited = new boolean[size];
        int[] queue = new int[size];
        int edgeMargin = Math.max(5, Math.min(width, height) / 12);
        int fragmentLimit = Math.max(100, size / 45);

        for (int start = 0; start < size; start++) {
            if (visited[start] || (pixels[start] >>> 24) <= 8) continue;

            int head = 0;
            int tail = 0;
            queue[tail++] = start;
            visited[start] = true;
            int left = start % width;
            int right = left;
            int top = start / width;
            int bottom = top;

            while (head < tail) {
                int index = queue[head++];
                int x = index % width;
                int y = index / width;
                left = Math.min(left, x);
                right = Math.max(right, x);
                top = Math.min(top, y);
                bottom = Math.max(bottom, y);

                for (int offsetY = -1; offsetY <= 1; offsetY++) {
                    for (int offsetX = -1; offsetX <= 1; offsetX++) {
                        if (offsetX == 0 && offsetY == 0) continue;
                        int nextX = x + offsetX;
                        int nextY = y + offsetY;
                        if (nextX < 0 || nextX >= width
                                || nextY < 0 || nextY >= height) continue;
                        int next = nextY * width + nextX;
                        if (visited[next] || (pixels[next] >>> 24) <= 8) continue;
                        visited[next] = true;
                        queue[tail++] = next;
                    }
                }
            }

            boolean touchesEdge = left < edgeMargin || right >= width - edgeMargin
                    || top < edgeMargin || bottom >= height - edgeMargin;
            if (touchesEdge && tail < fragmentLimit) {
                for (int i = 0; i < tail; i++) {
                    pixels[queue[i]] = 0;
                }
            }
        }

        source.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    /**
     * ImageGen Ä‘Ã´i khi xuáº¥t ná»n Ã´ caro thÃ nh RGB thay vÃ¬ alpha.
     * Flood-fill tá»« viá»n chá»‰ xÃ³a vÃ¹ng ná»n sÃ¡ng liá»n nhau.
     */
    private static Bitmap removeBakedCheckerboard(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int size = width * height;
        int[] pixels = new int[size];
        source.getPixels(pixels, 0, width, 0, 0, width, height);

        boolean[] visited = new boolean[size];
        int[] queue = new int[size];
        int tail = 0;

        for (int x = 0; x < width; x++) {
            tail = addBackgroundPixel(x, 0, width, pixels, visited, queue, tail);
            tail = addBackgroundPixel(x, height - 1, width,
                    pixels, visited, queue, tail);
        }
        for (int y = 1; y < height - 1; y++) {
            tail = addBackgroundPixel(0, y, width, pixels, visited, queue, tail);
            tail = addBackgroundPixel(width - 1, y, width,
                    pixels, visited, queue, tail);
        }

        int head = 0;
        while (head < tail) {
            int index = queue[head++];
            pixels[index] = 0;
            int x = index % width;
            int y = index / width;

            if (x > 0) {
                tail = addBackgroundIndex(index - 1, pixels, visited, queue, tail);
            }
            if (x < width - 1) {
                tail = addBackgroundIndex(index + 1, pixels, visited, queue, tail);
            }
            if (y > 0) {
                tail = addBackgroundIndex(index - width, pixels, visited, queue, tail);
            }
            if (y < height - 1) {
                tail = addBackgroundIndex(index + width, pixels, visited, queue, tail);
            }
        }

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        source.recycle();
        return result;
    }

    private static int addBackgroundPixel(int x, int y, int width,
                                          int[] pixels, boolean[] visited,
                                          int[] queue, int tail) {
        return addBackgroundIndex(y * width + x, pixels, visited, queue, tail);
    }

    private static int addBackgroundIndex(int index, int[] pixels,
                                          boolean[] visited, int[] queue,
                                          int tail) {
        if (!visited[index] && isCheckerboardColor(pixels[index])) {
            visited[index] = true;
            queue[tail++] = index;
        }
        return tail;
    }

    private static boolean isCheckerboardColor(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int maximum = Math.max(red, Math.max(green, blue));
        int minimum = Math.min(red, Math.min(green, blue));
        return minimum >= 225 && maximum - minimum <= 12;
    }

    private static Bitmap trimTransparentPixels(Bitmap frame) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        int[] pixels = new int[width * height];
        frame.getPixels(pixels, 0, width, 0, 0, width, height);

        int left = width;
        int top = height;
        int right = -1;
        int bottom = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((pixels[y * width + x] >>> 24) <= 8) continue;
                left = Math.min(left, x);
                top = Math.min(top, y);
                right = Math.max(right, x);
                bottom = Math.max(bottom, y);
            }
        }

        if (right < left || bottom < top) return frame;
        Rect visible = new Rect(left, top, right + 1, bottom + 1);
        if (visible.left == 0 && visible.top == 0
                && visible.width() == width && visible.height() == height) {
            return frame;
        }
        Bitmap trimmed = Bitmap.createBitmap(
                frame, visible.left, visible.top, visible.width(), visible.height()
        );
        frame.recycle();
        return trimmed;
    }
}
