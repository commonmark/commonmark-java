package org.commonmark.spannable.text.style;

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class LineSeparatorSpan extends MetricAffectingSpan {
    private final int mPadding;

    public LineSeparatorSpan(int padding) {
        mPadding = padding;
    }

    @Override
    public void updateMeasureState(TextPaint p) {
        apply(p);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        apply(tp);
    }

    private void apply(TextPaint tp) {
        tp.setTextSize(mPadding);
    }
}
