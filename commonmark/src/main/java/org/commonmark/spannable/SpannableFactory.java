package org.commonmark.spannable;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface SpannableFactory {
    int TYPE_BOLD = 1;
    int TYPE_ITALIC = 2;
    int TYPE_HEADER = 3;
    int TYPE_QUOTE = 4;
    int TYPE_LINK = 5;
    int TYPE_INLINE_CODE = 6;
    int TYPE_CODE_BLOCK = 7;
    int TYPE_ORDERED_LIST_ITEM = 8;
    int TYPE_UNORDERED_LIST_ITEM = 9;
    int TYPE_PARAGRAPH_SEPARATOR = 10;

    @Nullable
    Object getSpan(int type);

    @Nullable
    Object getLinkSpan(String url);
}
