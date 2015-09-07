package org.commonmark.spannable.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;

public class UnorderedListItemSpan extends ListItemSpan {
    private final int mMarkerRadius;

    public UnorderedListItemSpan(int leading, int extraHeight, int leftMargin, int markerRadius) {
        super(leading, extraHeight, leftMargin);
        mMarkerRadius = markerRadius;
    }

    @Override
    protected void drawMarker(Canvas c, Paint p, int x, int baseline, int top, int bottom, int lineLeading,
                              int markerLeftMargin, int lineExtraSpace) {
        Paint.Style style = p.getStyle();
        p.setStyle(Paint.Style.FILL);

        c.drawCircle(x + markerLeftMargin + mMarkerRadius, lineExtraSpace / 2 + ((top + bottom) / 2.0f),
                mMarkerRadius, p);

        p.setStyle(style);
    }
}
