package org.commonmark.spannable.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;

public class OrderedListItemSpan extends ListItemSpan implements CountedSpan {
    private int mMarkerNumber;

    public OrderedListItemSpan(int leading, int extraHeight, int leftMargin) {
        super(leading, extraHeight, leftMargin);
    }

    @Override
    protected void drawMarker(Canvas c, Paint p, int x, int baseline, int top, int bottom, int lineLeading,
                              int markerLeftMargin, int lineExtraSpace) {
        String text = mMarkerNumber + ". ";
        float textSize = p.measureText(text);
        c.drawText(text, x + (lineLeading - textSize) / 2, baseline, p);
    }

    @Override
    public void setCount(int count) {
        mMarkerNumber = count;
    }
}
