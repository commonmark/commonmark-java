package org.commonmark.spannable.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.TextPaint;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.MetricAffectingSpan;

public class CodeBlockSpan extends MetricAffectingSpan implements LeadingMarginSpan, LineBackgroundSpan {
    private final int mColor;
    private final int mPadding;
    private final int mTextSize;

    private final RectF mRect = new RectF();

    public CodeBlockSpan(int color, int textSize, int padding) {
        mColor = color;
        mTextSize = textSize;
        mPadding = padding;
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint);
    }

    @Override
    public void updateDrawState(TextPaint paint) {
        apply(paint);
    }

    private void apply(TextPaint paint) {
        paint.setTextSize(mTextSize);
        paint.setTypeface(Typeface.MONOSPACE);
    }

    @Override
    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom,
                               CharSequence text, int start, int end, int lnum) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(mColor);

        mRect.set(left, top, right, bottom);
        c.drawRect(mRect, p);

        p.setColor(color);
        p.setStyle(style);
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return mPadding;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom,
                                  CharSequence text, int start, int end, boolean first, Layout layout) {
    }
}
