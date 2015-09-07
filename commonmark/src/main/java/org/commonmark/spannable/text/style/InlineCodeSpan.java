package org.commonmark.spannable.text.style;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class InlineCodeSpan extends MetricAffectingSpan {
    private final int mTextSize;
    private final int mBackgroundColor;

    public InlineCodeSpan(int color, int textSize) {
        mBackgroundColor = color;
        mTextSize = textSize;
    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        apply(tp);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        apply(tp);
    }

    private void apply(TextPaint tp) {
        tp.setTextSize(mTextSize);
        tp.setTypeface(Typeface.MONOSPACE);
        tp.bgColor = mBackgroundColor;
    }
}
