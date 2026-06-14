package dev.sixik.sdmshop2.libs.platform.render;

public class RenderHelpUtils {

    public static float getScaleText(int w, int h, int objectW, int objectH) {
        return getScaleText(w, h, objectW, objectH, 0, 0, 0, 0, 0, Float.MAX_VALUE);
    }

    public static float getScaleText(int w, int h, int objectW, int objectH,
                                     float minScale, float maxScale) {
        return getScaleText(w, h, objectW, objectH, 0, 0, 0, 0, minScale, maxScale);
    }

    public static float getScaleText(int w, int h, int objectW, int objectH, int padding) {
        return getScaleText(w, h, objectW, objectH, padding, padding, padding, padding, 0, Float.MAX_VALUE);
    }

    public static float getScaleText(int w, int h, int objectW, int objectH, int padding,
                                     float minScale, float maxScale) {
        return getScaleText(w, h, objectW, objectH, padding, padding, padding, padding, minScale, maxScale);
    }

    public static float getScaleText(int w, int h, int objectW, int objectH,
                                     int paddingLeft, int paddingRight,
                                     int paddingTop, int paddingBottom) {
        return getScaleText(w, h, objectW, objectH, paddingLeft, paddingRight,
                paddingTop, paddingBottom, 0, Float.MAX_VALUE);
    }

    public static float getScaleText(int w, int h, int objectW, int objectH,
                                     int paddingLeft, int paddingRight,
                                     int paddingTop, int paddingBottom,
                                     float minScale, float maxScale) {

        if (objectW <= 0 || objectH <= 0) {
            return Math.max(0f, minScale);
        }

        float availableW = Math.max(0f, w - paddingLeft - paddingRight);
        float availableH = Math.max(0f, h - paddingTop - paddingBottom);

        float scaleX = availableW / objectW;
        float scaleY = availableH / objectH;
        float scale = Math.min(scaleX, scaleY);

        float finalMinScale = Math.max(minScale, 0f);
        float finalMaxScale = Math.max(maxScale, finalMinScale);

        return Math.max(finalMinScale, Math.min(scale, finalMaxScale));
    }

    public static float[] getCenterObject(int w, int h, int objectW, int objectH, float scale) {
        return getCenterObject(w, h, objectW, objectH, 0, 0, 0, 0, scale);
    }

    public static float[] getCenterObject(int w, int h, int objectW, int objectH, int padding, float scale) {
        return getCenterObject(w, h, objectW, objectH, padding, padding, padding, padding, scale);
    }

    public static float[] getCenterObject(int w, int h, int objectW, int objectH,
                                          int paddingLeft, int paddingRight,
                                          int paddingTop, int paddingBottom,
                                          float scale) {
        float scaledW = objectW * scale;
        float scaledH = objectH * scale;

        float availableW = Math.max(0f, w - paddingLeft - paddingRight);
        float availableH = Math.max(0f, h - paddingTop - paddingBottom);

        float localX = (availableW - scaledW) / 2f;
        float localY = (availableH - scaledH) / 2f;

        float finalX = paddingLeft + localX;
        float finalY = paddingTop + localY;

        return new float[]{finalX, finalY};
    }
}