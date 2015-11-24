package org.commonmark.spannable.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineHeightSpan;

public abstract class ListItemSpan implements LeadingMarginSpan, LineHeightSpan {
    private final int mLineExtraSpace;
    private final int mLineLeading;
    private final int mMarkerLeftMargin;

    private int mTextDefaultTop;
    private int mTextDefaultAscent;

    public ListItemSpan(int leading, int extraHeight, int leftMargin) {
        mLineLeading = leading;
        mLineExtraSpace = extraHeight;
        mMarkerLeftMargin = leftMargin;
    }

    protected abstract void drawMarker(Canvas c, Paint p, int x, int baseline, int top, int bottom, int lineLeading,
                                       int markerLeftMargin, int lineExtraSpace);

    @Override
    public int getLeadingMargin(boolean first) {
        return mLineLeading;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom,
                                  CharSequence text, int start, int end, boolean first, Layout l) {
        if (((Spanned) text).getSpanStart(this) != start) {
            return;
        }

        drawMarker(c, p, x, baseline, top, bottom, mLineLeading, mMarkerLeftMargin, mLineExtraSpace);
    }

    @Override
    public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
        if (spanstartv == v) {
            mTextDefaultTop = fm.top;
            mTextDefaultAscent = fm.ascent;
            fm.top -= mLineExtraSpace;
            fm.ascent -= mLineExtraSpace;
        } else {
            fm.top = mTextDefaultTop;
            fm.ascent = mTextDefaultAscent;
        }
    }
}
