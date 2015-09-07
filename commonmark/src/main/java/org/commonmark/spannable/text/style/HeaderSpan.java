package org.commonmark.spannable.text.style;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class HeaderSpan extends MetricAffectingSpan {
    private final int mTextSize;

    public HeaderSpan(int textSize) {
        mTextSize = textSize;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        apply(tp);
    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        apply(tp);
    }

    private void apply(TextPaint tp) {
        tp.setTextSize(mTextSize);
        tp.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
    }
}
