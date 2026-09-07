package com.tutorial.androidgametutorial.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Process-wide cache for immutable render assets.
 *
 * Cached bitmaps are owned by this class and must never be recycled by callers.
 * Temporary source sheets used while slicing animations are intentionally not
 * cached; their loaders recycle them as soon as the final frames are produced.
 */
public final class BitmapCache {

    private static final Map<Key, Bitmap> CACHE = new HashMap<>();

    private BitmapCache() {
    }

    public static Bitmap get(Context context, int resourceId) {
        return get(context, resourceId, null);
    }

    public static synchronized Bitmap get(Context context, int resourceId,
                                          Bitmap.Config preferredConfig) {
        Key key = Key.raw(resourceId, preferredConfig);
        Bitmap cached = CACHE.get(key);
        if (isUsable(cached)) return cached;

        BitmapFactory.Options options = createOptions(preferredConfig);
        Bitmap decoded = BitmapFactory.decodeResource(
                context.getApplicationContext().getResources(),
                resourceId,
                options
        );
        if (decoded != null) CACHE.put(key, decoded);
        return decoded;
    }

    public static synchronized Bitmap getScaled(Context context, int resourceId,
                                                 int width, int height,
                                                 boolean filter) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        Key key = Key.scaled(resourceId, safeWidth, safeHeight, filter);
        Bitmap cached = CACHE.get(key);
        if (isUsable(cached)) return cached;

        Bitmap source = decodeTemporary(context, resourceId, null);
        if (source == null) return null;

        Bitmap scaled = Bitmap.createScaledBitmap(
                source, safeWidth, safeHeight, filter
        );
        if (scaled != source) source.recycle();
        CACHE.put(key, scaled);
        return scaled;
    }

    public static synchronized Bitmap getScaled(Context context, int resourceId,
                                                 float scale, boolean filter) {
        int scaleBits = Float.floatToIntBits(scale);
        Key key = Key.factor(resourceId, scaleBits, filter);
        Bitmap cached = CACHE.get(key);
        if (isUsable(cached)) return cached;

        Bitmap source = decodeTemporary(context, resourceId, null);
        if (source == null) return null;

        int width = Math.max(1, Math.round(source.getWidth() * scale));
        int height = Math.max(1, Math.round(source.getHeight() * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(source, width, height, filter);
        if (scaled != source) source.recycle();
        CACHE.put(key, scaled);
        return scaled;
    }

    public static Bitmap decodeTemporary(Context context, int resourceId,
                                         Bitmap.Config preferredConfig) {
        return BitmapFactory.decodeResource(
                context.getApplicationContext().getResources(),
                resourceId,
                createOptions(preferredConfig)
        );
    }

    private static BitmapFactory.Options createOptions(
            Bitmap.Config preferredConfig) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        if (preferredConfig != null) options.inPreferredConfig = preferredConfig;
        return options;
    }

    private static boolean isUsable(Bitmap bitmap) {
        return bitmap != null && !bitmap.isRecycled();
    }

    private static final class Key {
        private static final int RAW = 0;
        private static final int SCALED = 1;
        private static final int FACTOR = 2;

        private final int resourceId;
        private final int variant;
        private final int first;
        private final int second;
        private final boolean filter;

        private Key(int resourceId, int variant, int first,
                    int second, boolean filter) {
            this.resourceId = resourceId;
            this.variant = variant;
            this.first = first;
            this.second = second;
            this.filter = filter;
        }

        private static Key raw(int resourceId, Bitmap.Config config) {
            return new Key(resourceId, RAW,
                    config == null ? -1 : config.ordinal(), 0, false);
        }

        private static Key scaled(int resourceId, int width,
                                  int height, boolean filter) {
            return new Key(resourceId, SCALED, width, height, filter);
        }

        private static Key factor(int resourceId, int scaleBits,
                                  boolean filter) {
            return new Key(resourceId, FACTOR, scaleBits, 0, filter);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Key key)) return false;
            return resourceId == key.resourceId
                    && variant == key.variant
                    && first == key.first
                    && second == key.second
                    && filter == key.filter;
        }

        @Override
        public int hashCode() {
            int result = resourceId;
            result = 31 * result + variant;
            result = 31 * result + first;
            result = 31 * result + second;
            result = 31 * result + (filter ? 1 : 0);
            return result;
        }
    }
}
